package com.konorpg.rpg;

public enum Stat {
    STR("STR", "Força"), INT("INT", "Inteligência"), DEX("DEX", "Agilidade"),
    VIT("VIT", "Vitalidade"), MAG("MAG", "Magia"), LUCK("LUCK", "Sorte");

    public final String code, label;

    Stat(String code, String label) { this.code = code; this.label = label; }

    public static Stat byName(String s) {
        for (Stat x : values()) if (x.name().equalsIgnoreCase(s)) return x;
        return null;
    }
}
