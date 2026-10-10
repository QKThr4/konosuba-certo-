package com.konorpg.rpg;

import com.konorpg.data.PlayerRpgData;
import com.konorpg.item.RpgGear;
import com.konorpg.skill.Skill;
import com.konorpg.skill.Skills;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Derived stats. Pure functions usable on both client (HUD) and server (gameplay). */
public final class RpgStats {
    public static int eff(Player p, PlayerRpgData d, Stat s) {
        int v = d.stat(s);
        for (ItemStack a : d.accessories) {
            if (a.getItem() instanceof RpgGear g) v += g.bonuses().getOrDefault(s, 0);
        }
        if (p != null && p.getMainHandItem().getItem() instanceof RpgGear g) v += g.bonuses().getOrDefault(s, 0);
        for (String id : d.skills) {
            Skill sk = Skills.get(id);
            if (sk != null && sk.type() == Skill.Type.PASSIVE && Skills.usable(d, sk)) v += sk.bonuses().getOrDefault(s, 0);
        }
        return v;
    }

    public static double maxMana(Player p, PlayerRpgData d) {
        return 20 + 5.0 * eff(p, d, Stat.INT) + 3.0 * eff(p, d, Stat.MAG) + 2.0 * d.level;
    }

    public static double manaRegen(Player p, PlayerRpgData d) {
        return (0.5 + 0.12 * eff(p, d, Stat.MAG) + 0.05 * eff(p, d, Stat.INT)) * com.konorpg.Config.MANA_REGEN_MULT.get();
    }

    public static double spellPower(Player p, PlayerRpgData d) {
        return 1.0 + eff(p, d, Stat.INT) * 0.05 + eff(p, d, Stat.MAG) * 0.03 + d.level * 0.02;
    }

    public static double physPower(Player p, PlayerRpgData d) {
        return 1.0 + eff(p, d, Stat.STR) * 0.04 + eff(p, d, Stat.DEX) * 0.01 + d.level * 0.01;
    }

    public static double critChance(Player p, PlayerRpgData d) {
        return Math.min(0.75, 0.03 + eff(p, d, Stat.LUCK) * 0.01 + eff(p, d, Stat.DEX) * 0.003);
    }

    public static long xpForNext(int level) {
        return 60L + 30L * level + 6L * level * level;
    }

    private RpgStats() {}
}
