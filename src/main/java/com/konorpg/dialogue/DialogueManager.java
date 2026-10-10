package com.konorpg.dialogue;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.konorpg.Config;
import com.konorpg.KonoRPG;
import com.konorpg.data.CompanionRecord;
import com.konorpg.data.PlayerRpgData;
import com.konorpg.entity.CompanionEntity;
import com.konorpg.entity.TownNpcEntity;
import com.konorpg.network.Actions;
import com.konorpg.network.Net;
import com.konorpg.quest.QuestManager;
import com.konorpg.rpg.Requirements;
import com.konorpg.rpg.RpgManager;
import com.konorpg.rpg.RpgStats;
import com.konorpg.shop.ShopManager;
import com.konorpg.util.J;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

/** JSON dialogue trees with requirements and effects. Sessions are tracked server-side. */
public final class DialogueManager extends SimpleJsonResourceReloadListener {
    public static final DialogueManager INSTANCE = new DialogueManager();

    private record Dialogue(String speaker, String start, List<JsonObject> starts, Map<String, JsonObject> nodes) {}

    private static final class Session {
        String dialogue, node;
        int entityId;
        List<Integer> map = new ArrayList<>();
        boolean opened;
    }

    private static final Map<String, Dialogue> DATA = new LinkedHashMap<>();
    private static final Map<UUID, Session> SESSIONS = new HashMap<>();

    private DialogueManager() { super(new Gson(), "konorpg_dialogues"); }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager rm, ProfilerFiller pf) {
        DATA.clear();
        for (var e : map.entrySet()) {
            try {
                JsonObject o = e.getValue().getAsJsonObject();
                Map<String, JsonObject> nodes = new LinkedHashMap<>();
                JsonObject n = J.obj(o, "nodes");
                if (n != null) for (var ne : n.entrySet()) nodes.put(ne.getKey(), ne.getValue().getAsJsonObject());
                List<JsonObject> starts = new ArrayList<>();
                for (JsonElement s : J.arr(o, "starts")) starts.add(s.getAsJsonObject());
                DATA.put(e.getKey().getPath(), new Dialogue(J.str(o, "speaker", "???"), J.str(o, "start", "start"), starts, nodes));
            } catch (Exception ex) {
                KonoRPG.LOGGER.error("Bad dialogue file {}", e.getKey(), ex);
            }
        }
        KonoRPG.LOGGER.info("Loaded {} dialogues", DATA.size());
    }

    public static void start(ServerPlayer p, Entity npc, String id) {
        Dialogue dlg = DATA.get(id);
        if (dlg == null || !Config.DIALOGUES_ENABLED.get()) {
            if (npc instanceof TownNpcEntity t) t.say("...");
            return;
        }
        PlayerRpgData d = RpgManager.data(p);
        if (npc instanceof TownNpcEntity t) QuestManager.onEvent(p, "talk:" + t.npcId(), 1);
        String node = dlg.start();
        for (JsonObject s : dlg.starts()) {
            if (Requirements.check(d, J.obj(s, "require"))) { node = J.str(s, "node", node); break; }
        }
        Session s = new Session();
        s.dialogue = id;
        s.entityId = npc.getId();
        s.node = node;
        SESSIONS.put(p.getUUID(), s);
        send(p, d, s, npc);
    }

    private static String fill(String text, ServerPlayer p, PlayerRpgData d, Entity npc) {
        return text.replace("{player}", p.getGameProfile().getName())
                .replace("{money}", String.valueOf(d.money))
                .replace("{level}", String.valueOf(d.level))
                .replace("{class}", d.classId)
                .replace("{rank}", d.rank().name())
                .replace("{fee}", String.valueOf(Config.REG_FEE.get()));
    }

    private static void send(ServerPlayer p, PlayerRpgData d, Session s, Entity npc) {
        Dialogue dlg = DATA.get(s.dialogue);
        JsonObject node = dlg.nodes().get(s.node);
        if (node == null) { close(p); return; }
        String text;
        JsonElement te = node.get("text");
        if (te != null && te.isJsonArray() && te.getAsJsonArray().size() > 0) {
            var arr = te.getAsJsonArray();
            text = arr.get(p.getRandom().nextInt(arr.size())).getAsString();
        } else text = te == null ? "..." : te.getAsString();
        CompoundTag t = new CompoundTag();
        String speaker = dlg.speaker();
        if (npc instanceof TownNpcEntity tn && !tn.npcName().isEmpty()) speaker = tn.npcName();
        if (npc instanceof CompanionEntity) speaker = "Aqua";
        t.putString("speaker", speaker);
        t.putString("text", fill(text, p, d, npc));
        ListTag choices = new ListTag();
        s.map.clear();
        var arr = J.arr(node, "choices");
        for (int i = 0; i < arr.size(); i++) {
            JsonObject c = arr.get(i).getAsJsonObject();
            if (!Requirements.check(d, J.obj(c, "require"))) continue;
            choices.add(StringTag.valueOf(fill(J.str(c, "text", "..."), p, d, npc)));
            s.map.add(i);
        }
        if (choices.isEmpty()) choices.add(StringTag.valueOf("(Sair)"));
        t.put("choices", choices);
        Net.openScreen(p, "dialogue", t);
    }

    public static void choose(ServerPlayer p, int idx) {
        Session s = SESSIONS.get(p.getUUID());
        if (s == null) return;
        Dialogue dlg = DATA.get(s.dialogue);
        JsonObject node = dlg == null ? null : dlg.nodes().get(s.node);
        if (node == null || idx < 0 || idx >= s.map.size()) { close(p); return; }
        JsonObject c = J.arr(node, "choices").get(s.map.get(idx)).getAsJsonObject();
        PlayerRpgData d = RpgManager.data(p);
        Entity npc = p.serverLevel().getEntity(s.entityId);
        if (npc == null || npc.distanceToSqr(p) > 100) { close(p); return; }
        s.opened = false;
        boolean ok = true;
        for (JsonElement e : J.arr(c, "effects")) {
            if (!runEffect(p, d, s, npc, e.getAsJsonObject())) { ok = false; break; }
        }
        RpgManager.sync(p);
        String next = ok ? J.str(c, "next", "") : J.str(c, "next_fail", J.str(c, "next", ""));
        if (!ok && !c.has("next_fail")) next = s.node;
        if (next.isEmpty()) {
            SESSIONS.remove(p.getUUID());
            if (!s.opened) Net.openScreen(p, "close", new CompoundTag());
            return;
        }
        s.node = next;
        if (!s.opened) send(p, d, s, npc);
        else SESSIONS.remove(p.getUUID());
    }

    public static void close(ServerPlayer p) {
        SESSIONS.remove(p.getUUID());
    }

    public static void forget(UUID id) { SESSIONS.remove(id); }

    private static boolean runEffect(ServerPlayer p, PlayerRpgData d, Session s, Entity npc, JsonObject e) {
        String type = J.str(e, "type", "");
        switch (type) {
            case "rep" -> RpgManager.addRep(p, J.str(e, "faction", "guild"), J.i(e, "amount", 0));
            case "money" -> {
                int a = J.i(e, "amount", 0);
                if (a >= 0) RpgManager.addMoney(p, a);
                else if (!RpgManager.spend(p, -a)) { RpgManager.msg(p, "Eris insuficientes (" + (-a) + " necessários)."); return false; }
            }
            case "xp" -> RpgManager.grantXp(p, J.i(e, "amount", 0));
            case "friendship" -> d.addFriend(J.str(e, "id", ""), J.i(e, "amount", 1));
            case "start_quest" -> QuestManager.accept(p, J.str(e, "id", ""), false);
            case "flag" -> d.flags.add(J.str(e, "name", ""));
            case "msg" -> RpgManager.msg(p, J.str(e, "text", ""));
            case "event" -> QuestManager.onEvent(p, J.str(e, "name", ""), 1);
            case "give" -> {
                var item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(J.str(e, "item", "minecraft:air")));
                com.konorpg.rpg.Perks.give(p, new ItemStack(item, J.i(e, "count", 1)));
            }
            case "action" -> {
                return action(p, d, s, npc, J.str(e, "action", ""));
            }
            default -> { }
        }
        return true;
    }

    private static boolean action(ServerPlayer p, PlayerRpgData d, Session s, Entity npc, String a) {
        switch (a) {
            case "open_shop" -> {
                if (npc instanceof TownNpcEntity t && !t.shopId().isEmpty()) { s.opened = true; ShopManager.open(p, t.shopId()); }
                else RpgManager.msg(p, "Este NPC não tem loja.");
            }
            case "open_guild" -> { s.opened = true; Net.openHub(p, "guild"); }
            case "open_board" -> { s.opened = true; QuestManager.openBoard(p); }
            case "register" -> { return Actions.register(p); }
            case "set_respawn" -> {
                p.setRespawnPosition(p.level().dimension(), p.blockPosition(), p.getYRot(), true, false);
                RpgManager.msg(p, "Seu ponto de retorno foi salvo aqui.");
            }
            case "rest" -> {
                if (!RpgManager.spend(p, 50)) { RpgManager.msg(p, "Descansar custa 50 Eris."); return false; }
                p.setHealth(p.getMaxHealth());
                d.mana = RpgStats.maxMana(p, d);
                p.setRespawnPosition(p.level().dimension(), p.blockPosition(), p.getYRot(), true, false);
                RpgManager.msg(p, "Você dormiu bem. HP e Mana restaurados; ponto de retorno salvo.");
            }
            case "companion_heal" -> {
                p.heal(8);
                RpgManager.msg(p, "Aqua usou Heal em você.");
            }
            case "companion_stay" -> {
                CompanionRecord r = d.companion("aqua");
                if (r != null) { r.stay = !r.stay; RpgManager.msg(p, r.stay ? "Aqua vai ficar parada aqui." : "Aqua vai te seguir."); }
            }
            default -> { }
        }
        return true;
    }

    public static void clearSessions() { SESSIONS.clear(); }
}
