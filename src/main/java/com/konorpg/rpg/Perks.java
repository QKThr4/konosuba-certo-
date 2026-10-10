package com.konorpg.rpg;

import com.konorpg.data.PlayerRpgData;
import com.konorpg.registry.ModItems;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** The single "isekai" gift the player picks. Permanent. */
public final class Perks {
    public record Perk(String id, String name, String desc) {}

    public static final Map<String, Perk> ALL = new LinkedHashMap<>();

    static {
        add("gram", "Espada Demoníaca Gram", "Uma lâmina lendária de poder absurdo. (Arma Lendária: +4 STR, +2 LUCK)");
        add("explosion", "Magia de Explosão", "Aprende EXPLOSION desde o início (+8 INT, +3 MAG). Um tiro e... cai no chão.");
        add("luck", "Bênção da Boa Sorte", "+12 de Sorte permanente: críticos, drops e eventos especiais.");
        add("wind", "Passos do Vento", "Mobilidade: aprende Dash e ganha +5 DEX.");
        add("aegis", "Barreira de Aegis", "Defesa: aprende Barrier e ganha +8 VIT.");
        add("artifact", "Artefato: Amuleto de Eris", "Colar Lendário (+5 INT, +5 MAG) e um frasco de mana.");
        add("aqua", "Aqua, a Deusa Inútil", "Leve a própria deusa! Ela se junta a você depois que se registrar na Guilda. Boa sorte.");
    }

    private static void add(String id, String name, String desc) { ALL.put(id, new Perk(id, name, desc)); }

    public static void apply(ServerPlayer p, PlayerRpgData d, String id) {
        d.perk = id;
        switch (id) {
            case "gram" -> give(p, new ItemStack(ModItems.GRAM_SWORD.get()));
            case "explosion" -> {
                d.stats[Stat.INT.ordinal()] += 8;
                d.stats[Stat.MAG.ordinal()] += 3;
                d.skills.add("explosion");
                d.flags.add("perk_skill:explosion");
                d.slots[0] = "explosion";
            }
            case "luck" -> d.stats[Stat.LUCK.ordinal()] += 12;
            case "wind" -> {
                d.stats[Stat.DEX.ordinal()] += 5;
                d.skills.add("dash");
                d.flags.add("perk_skill:dash");
                d.slots[0] = "dash";
            }
            case "aegis" -> {
                d.stats[Stat.VIT.ordinal()] += 8;
                d.skills.add("barrier");
                d.flags.add("perk_skill:barrier");
                d.slots[0] = "barrier";
            }
            case "artifact" -> {
                give(p, new ItemStack(ModItems.ERIS_AMULET.get()));
                give(p, new ItemStack(ModItems.GREATER_MANA_POTION.get(), 2));
            }
            case "aqua" -> d.flags.add("aqua_pending");
            default -> { }
        }
    }

    public static void give(ServerPlayer p, ItemStack s) {
        if (!p.getInventory().add(s)) p.drop(s, false);
    }

    private Perks() {}
}
