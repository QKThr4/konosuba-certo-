package com.konorpg.rpg;

import com.konorpg.Config;
import com.konorpg.KonoRPG;
import com.konorpg.data.CompanionRecord;
import com.konorpg.data.PlayerRpgData;
import com.konorpg.data.WorldRpgData;
import com.konorpg.network.SyncRpgPayload;
import com.konorpg.quest.QuestManager;
import com.konorpg.registry.ModAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

/** Central server-side RPG logic: XP, levels, money, reputation, attributes, sync. */
public final class RpgManager {
    public static final int MAX_LEVEL = 99;
    private static final ResourceLocation ID_HP = KonoRPG.id("rpg_hp");
    private static final ResourceLocation ID_ATK = KonoRPG.id("rpg_atk");
    private static final ResourceLocation ID_SPD = KonoRPG.id("rpg_spd");
    private static final ResourceLocation ID_ASPD = KonoRPG.id("rpg_aspd");
    private static final ResourceLocation ID_LUCK = KonoRPG.id("rpg_luck");

    public static PlayerRpgData data(Player p) { return p.getData(ModAttachments.RPG); }

    public static void sync(ServerPlayer p) {
        CompoundTag t = data(p).serializeNBT(p.registryAccess());
        t.putInt("mainTotal", QuestManager.mainTotal());
        t.put("qinfo", QuestManager.activeInfo(data(p)));
        WorldRpgData w = WorldRpgData.get(p.server);
        if (w.city != null) t.putLong("cityPos", w.city.asLong());
        t.putLong("gt", p.level().getGameTime());
        PacketDistributor.sendToPlayer(p, new SyncRpgPayload(t));
    }

    public static void msg(ServerPlayer p, String text) {
        p.sendSystemMessage(Component.literal(text).withStyle(ChatFormatting.YELLOW));
    }

    public static void title(ServerPlayer p, String main, String sub) {
        p.connection.send(new ClientboundSetTitlesAnimationPacket(10, 50, 15));
        p.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal(sub).withStyle(ChatFormatting.GRAY)));
        p.connection.send(new ClientboundSetTitleTextPacket(Component.literal(main).withStyle(ChatFormatting.GOLD)));
    }

    public static boolean nearCity(ServerPlayer p, double radius) {
        WorldRpgData w = WorldRpgData.get(p.server);
        if (w.city == null || p.level().dimension() != net.minecraft.world.level.Level.OVERWORLD) return false;
        BlockPos c = w.city;
        double dx = p.getX() - c.getX(), dz = p.getZ() - c.getZ();
        return dx * dx + dz * dz <= radius * radius;
    }

    // ---------------- XP / levels ----------------

    public static void grantXp(ServerPlayer p, long amount) {
        PlayerRpgData d = data(p);
        long gain = Math.max(0, Math.round(amount * Config.XP_MULT.get()));
        if (gain <= 0) return;
        d.xp += gain;
        boolean leveled = false;
        while (d.level < MAX_LEVEL && d.xp >= RpgStats.xpForNext(d.level)) {
            d.xp -= RpgStats.xpForNext(d.level);
            levelUp(p, d);
            leveled = true;
        }
        if (d.level >= MAX_LEVEL) d.xp = 0;
        for (CompanionRecord r : d.companions) {
            r.xp += gain / 2;
            while (r.level < MAX_LEVEL && r.xp >= RpgStats.xpForNext(r.level) / 2) {
                r.xp -= RpgStats.xpForNext(r.level) / 2;
                r.level++;
            }
        }
        if (leveled) refreshAttributes(p);
        sync(p);
    }

    private static void levelUp(ServerPlayer p, PlayerRpgData d) {
        d.level++;
        d.statPoints += 3;
        d.skillPoints += 1;
        RpgClass c = RpgClasses.get(d.classId);
        d.stats[c.primary().ordinal()]++;
        d.stats[c.secondary().ordinal()]++;
        p.setHealth(p.getMaxHealth());
        d.mana = Math.max(d.mana, RpgStats.maxMana(p, d));
        title(p, "LEVEL UP!", "Nível " + d.level + " - +3 pontos de atributo, +1 ponto de habilidade");
        if (d.level == 15) msg(p, "Você alcançou o nível 15! Classes avançadas estão disponíveis na Guilda (Rank D necessário).");
        CompanionManager.react(p, "levelup");
        QuestManager.onEvent(p, "level:" + d.level, 1);
    }

    // ---------------- money / reputation ----------------

    public static void addMoney(ServerPlayer p, long amount) {
        PlayerRpgData d = data(p);
        d.money = Math.max(0, d.money + amount);
    }

    public static void grantMoney(ServerPlayer p, long amount) {
        addMoney(p, Math.round(amount * Config.MONEY_MULT.get()));
    }

    public static boolean spend(ServerPlayer p, long amount) {
        PlayerRpgData d = data(p);
        if (d.money < amount) return false;
        d.money -= amount;
        return true;
    }

    public static void addRep(ServerPlayer p, String faction, int amount) {
        if (amount == 0) return;
        PlayerRpgData d = data(p);
        d.rep.merge(faction, amount, Integer::sum);
        p.displayClientMessage(Component.literal("Reputação (" + Factions.name(faction) + "): " + (amount > 0 ? "+" : "") + amount)
                .withStyle(amount > 0 ? ChatFormatting.GREEN : ChatFormatting.RED), true);
        if (faction.equals("guild")) refreshRank(p);
    }

    public static void refreshRank(ServerPlayer p) {
        PlayerRpgData d = data(p);
        Rank now = d.rank();
        for (Rank r : Rank.values()) {
            if (r.ordinal() > now.ordinal()) break;
            String flag = "rank_" + r.name();
            if (d.flags.add(flag) && r != Rank.F) {
                title(p, "RANK " + r.name(), "Você subiu de rank na Guilda!");
                CompanionManager.react(p, "rank");
                QuestManager.onEvent(p, "rank:" + r.name(), 1);
            }
        }
    }

    // ---------------- attributes ----------------

    private static void mod(ServerPlayer p, Holder<Attribute> attr, ResourceLocation id, double amount, AttributeModifier.Operation op) {
        AttributeInstance inst = p.getAttribute(attr);
        if (inst == null) return;
        inst.addOrUpdateTransientModifier(new AttributeModifier(id, amount, op));
    }

    public static void refreshAttributes(ServerPlayer p) {
        PlayerRpgData d = data(p);
        int str = RpgStats.eff(p, d, Stat.STR), dex = RpgStats.eff(p, d, Stat.DEX),
                vit = RpgStats.eff(p, d, Stat.VIT), luck = RpgStats.eff(p, d, Stat.LUCK);
        mod(p, Attributes.MAX_HEALTH, ID_HP, (vit - 5) * 1.5 + (d.level - 1), AttributeModifier.Operation.ADD_VALUE);
        mod(p, Attributes.ATTACK_DAMAGE, ID_ATK, Math.max(0, (str - 5) * 0.12), AttributeModifier.Operation.ADD_VALUE);
        mod(p, Attributes.MOVEMENT_SPEED, ID_SPD, Math.max(0, (dex - 5) * 0.004), AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        mod(p, Attributes.ATTACK_SPEED, ID_ASPD, Math.max(0, (dex - 5) * 0.02), AttributeModifier.Operation.ADD_VALUE);
        mod(p, Attributes.LUCK, ID_LUCK, (luck - 5) * 0.2, AttributeModifier.Operation.ADD_VALUE);
        if (p.getHealth() > p.getMaxHealth()) p.setHealth(p.getMaxHealth());
    }

    private RpgManager() {}
}
