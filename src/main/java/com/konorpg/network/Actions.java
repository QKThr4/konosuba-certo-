package com.konorpg.network;

import com.konorpg.Config;
import com.konorpg.data.CompanionRecord;
import com.konorpg.data.PlayerRpgData;
import com.konorpg.dialogue.DialogueManager;
import com.konorpg.item.CardItem;
import com.konorpg.quest.QuestManager;
import com.konorpg.rpg.Accessories;
import com.konorpg.rpg.Perks;
import com.konorpg.rpg.Rank;
import com.konorpg.rpg.RpgClass;
import com.konorpg.rpg.RpgClasses;
import com.konorpg.rpg.RpgManager;
import com.konorpg.rpg.Stat;
import com.konorpg.shop.ShopManager;
import com.konorpg.skill.Skill;
import com.konorpg.skill.SkillExecutor;
import com.konorpg.skill.Skills;
import com.konorpg.world.CityBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

/** Server-side handler of every client request. Everything is validated here. */
public final class Actions {
    public static void handle(ServerPlayer p, String action, CompoundTag a) {
        PlayerRpgData d = RpgManager.data(p);
        switch (action) {
            case "request_sync" -> RpgManager.sync(p);
            case "open_hub" -> Net.openHub(p, a.getString("tab"));
            case "choose_perk" -> choosePerk(p, d, a.getString("id"));
            case "stat_add" -> {
                Stat s = Stat.byName(a.getString("stat"));
                if (s != null && d.statPoints > 0) {
                    d.stats[s.ordinal()]++;
                    d.statPoints--;
                    RpgManager.refreshAttributes(p);
                    RpgManager.sync(p);
                }
            }
            case "skill_learn" -> {
                Skill s = Skills.get(a.getString("id"));
                if (s != null && Skills.canLearn(d, s)) {
                    d.skillPoints -= s.spCost();
                    d.skills.add(s.id());
                    if (s.type() != Skill.Type.PASSIVE) {
                        for (int i = 0; i < 4; i++) if (d.slots[i].isEmpty()) { d.slots[i] = s.id(); break; }
                    }
                    RpgManager.refreshAttributes(p);
                    RpgManager.msg(p, "Habilidade aprendida: " + s.name());
                    RpgManager.sync(p);
                }
            }
            case "skill_bind" -> {
                String id = a.getString("id");
                int slot = a.getInt("slot");
                if (slot < 0 || slot > 3) return;
                Skill s = Skills.get(id);
                if (id.isEmpty()) d.slots[slot] = "";
                else if (s != null && d.skills.contains(id) && s.type() != Skill.Type.PASSIVE) {
                    for (int i = 0; i < 4; i++) if (d.slots[i].equals(id)) d.slots[i] = "";
                    d.slots[slot] = id;
                }
                RpgManager.sync(p);
            }
            case "skill_cast" -> {
                int slot = a.getInt("slot");
                if (slot >= 0 && slot < 4 && !d.slots[slot].isEmpty()) SkillExecutor.cast(p, d.slots[slot]);
            }
            case "quest_accept" -> QuestManager.accept(p, a.getString("id"), false);
            case "quest_abandon" -> QuestManager.abandon(p, a.getString("id"));
            case "quest_claim" -> QuestManager.claim(p, a.getString("id"));
            case "board_open" -> QuestManager.openBoard(p);
            case "register" -> register(p);
            case "class_set" -> setClass(p, d, a.getString("id"));
            case "dlg_choice" -> DialogueManager.choose(p, a.getInt("i"));
            case "dlg_close" -> DialogueManager.close(p);
            case "shop_buy" -> ShopManager.buy(p, a.getInt("idx"), a.getInt("qty"));
            case "shop_sell" -> ShopManager.sell(p, a.getInt("slot"), a.getBoolean("all"));
            case "shop_close" -> ShopManager.close(p);
            case "acc_equip" -> Accessories.equipHeld(p);
            case "acc_unequip" -> Accessories.unequip(p, a.getInt("slot"));
            case "companion_stay" -> {
                CompanionRecord r = d.companion("aqua");
                if (r != null) { r.stay = !r.stay; RpgManager.sync(p); }
            }
            default -> { }
        }
    }

    private static void choosePerk(ServerPlayer p, PlayerRpgData d, String id) {
        if (d.introDone || !Perks.ALL.containsKey(id)) return;
        Perks.apply(p, d, id);
        d.introDone = true;
        d.money = Config.START_MONEY.get();
        d.mana = 60;
        RpgManager.refreshAttributes(p);
        CityBuilder.ensureCity(p.server);
        CityBuilder.teleportToPlaza(p);
        QuestManager.accept(p, "main_01_guild", true);
        RpgManager.title(p, "Bem-vindo a Axel", "Procure a Guilda dos Aventureiros (prédio ao norte da praça)");
        RpgManager.msg(p, "Você renasceu em outro mundo! Vá até a Guilda, fale com a recepcionista e registre-se. Pressione K para abrir o seu painel de aventureiro.");
        RpgManager.sync(p);
    }

    public static boolean register(ServerPlayer p) {
        PlayerRpgData d = RpgManager.data(p);
        if (d.registered) { RpgManager.msg(p, "Você já é um aventureiro registrado."); return false; }
        if (!RpgManager.nearCity(p, 90)) { RpgManager.msg(p, "Vá até a Guilda, na cidade."); return false; }
        int fee = Config.REG_FEE.get();
        if (!RpgManager.spend(p, fee)) { RpgManager.msg(p, "A taxa de inscrição é " + fee + " Eris. Você não tem o suficiente!"); return false; }
        d.registered = true;
        d.skillPoints += 1;
        Perks.give(p, CardItem.create(p));
        RpgManager.title(p, "ADVENTURER CARD", "Bem-vindo à Guilda, " + p.getGameProfile().getName() + "!");
        RpgManager.msg(p, "Registro concluído! Taxa paga: " + fee + " Eris. Escolha sua classe no painel (K > GUILD).");
        RpgManager.addRep(p, "guild", 1);
        QuestManager.onEvent(p, "register_guild", 1);
        RpgManager.sync(p);
        return true;
    }

    private static void setClass(ServerPlayer p, PlayerRpgData d, String id) {
        if (!d.registered) { RpgManager.msg(p, "Registre-se na Guilda primeiro."); return; }
        if (!RpgClasses.exists(id) || id.equals(d.classId) || id.equals("adventurer")) return;
        if (!RpgManager.nearCity(p, 90)) { RpgManager.msg(p, "Mudança de classe só na Guilda (cidade)."); return; }
        RpgClass c = RpgClasses.get(id);
        long cost = c.cost();
        if (c.tier() == 1) {
            cost = d.classId.equals("adventurer") ? 0 : 2000;
        } else {
            if (!d.classId.equals(c.parent())) { RpgManager.msg(p, "Requer ser " + RpgClasses.get(c.parent()).name() + "."); return; }
            if (d.level < c.minLevel()) { RpgManager.msg(p, "Requer nível " + c.minLevel() + "."); return; }
            if (d.rank().ordinal() < c.minRank().ordinal()) { RpgManager.msg(p, "Requer rank " + c.minRank().name() + "."); return; }
        }
        if (!RpgManager.spend(p, cost)) { RpgManager.msg(p, "Custa " + cost + " Eris."); return; }
        d.classId = id;
        RpgManager.refreshAttributes(p);
        RpgManager.title(p, c.name().toUpperCase(), "Nova classe!");
        QuestManager.onEvent(p, "choose_class", 1);
        RpgManager.sync(p);
    }

    private Actions() {}
}
