package com.konorpg.rpg;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** Class registry. Add new classes with register(...) (also usable by other mods / future content). */
public final class RpgClasses {
    private static final Map<String, RpgClass> ALL = new LinkedHashMap<>();

    public static void register(RpgClass c) { ALL.put(c.id(), c); }

    public static RpgClass get(String id) { return ALL.getOrDefault(id, ALL.get("adventurer")); }

    public static boolean exists(String id) { return ALL.containsKey(id); }

    public static Collection<RpgClass> all() { return ALL.values(); }

    /** Skill groups a class may use: general + own id + every ancestor. */
    public static Set<String> lines(String classId) {
        Set<String> out = new HashSet<>();
        out.add("general");
        String cur = classId;
        int guard = 0;
        while (cur != null && !cur.isEmpty() && guard++ < 8) {
            out.add(cur);
            RpgClass c = ALL.get(cur);
            cur = c == null ? null : c.parent();
        }
        return out;
    }

    static {
        register(new RpgClass("adventurer", "Adventurer", 0, "", "Classe inicial. Fraca em tudo, boa em nada... mas pode aprender truques gerais.", 1, 0, Rank.F, Stat.LUCK, Stat.DEX));
        register(new RpgClass("warrior", "Warrior", 1, "adventurer", "Combatente corpo a corpo. Muita força e vida.", 1, 0, Rank.F, Stat.STR, Stat.VIT));
        register(new RpgClass("knight", "Knight", 1, "adventurer", "Defensor com escudo e carga. Resistente.", 1, 0, Rank.F, Stat.VIT, Stat.STR));
        register(new RpgClass("mage", "Mage", 1, "adventurer", "Conjurador de magias elementais.", 1, 0, Rank.F, Stat.INT, Stat.MAG));
        register(new RpgClass("priest", "Priest", 1, "adventurer", "Cura e bênçãos. Bom contra mortos-vivos.", 1, 0, Rank.F, Stat.MAG, Stat.INT));
        register(new RpgClass("archer", "Archer", 1, "adventurer", "Ataques precisos à distância.", 1, 0, Rank.F, Stat.DEX, Stat.LUCK));
        register(new RpgClass("thief", "Thief", 1, "adventurer", "Furtividade, roubo e fuga. Depende de sorte.", 1, 0, Rank.F, Stat.LUCK, Stat.DEX));

        register(new RpgClass("archwizard", "Arch Wizard", 2, "mage", "Mestre da Explosion. Poder absurdo, mana de sobra... por um tiro só.", 15, 5000, Rank.D, Stat.INT, Stat.MAG));
        register(new RpgClass("crusader", "Crusader", 2, "knight", "Aguenta tudo e atrai todos os inimigos (Decoy).", 15, 5000, Rank.D, Stat.VIT, Stat.STR));
        register(new RpgClass("swordmaster", "Sword Master", 2, "warrior", "Dança de lâminas e golpes devastadores.", 15, 5000, Rank.D, Stat.STR, Stat.DEX));
        register(new RpgClass("highpriest", "High Priest", 2, "priest", "Curas poderosas em área.", 15, 5000, Rank.D, Stat.MAG, Stat.INT));
        register(new RpgClass("assassin", "Assassin", 2, "thief", "Ataques pelas costas com dano crítico.", 15, 5000, Rank.D, Stat.DEX, Stat.LUCK));
        register(new RpgClass("necromancer", "Necromancer", 2, "mage", "Drena vida dos inimigos com magia sombria.", 18, 7500, Rank.C, Stat.INT, Stat.MAG));
        register(new RpgClass("battlemage", "Battle Mage", 2, "mage", "Mistura espada e magia.", 18, 7500, Rank.C, Stat.STR, Stat.INT));
        register(new RpgClass("paladin", "Paladin", 2, "knight", "Cavaleiro sagrado: luz contra trevas.", 20, 9000, Rank.C, Stat.VIT, Stat.MAG));
    }

    private RpgClasses() {}
}
