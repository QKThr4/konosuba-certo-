package com.konorpg.rpg;

public enum Rank {
    F(0), E(30), D(90), C(200), B(400), A(800), S(1500);

    public final int points;

    Rank(int p) { points = p; }

    public static Rank of(int pts) {
        Rank r = F;
        for (Rank x : values()) if (pts >= x.points) r = x;
        return r;
    }

    public static Rank byName(String s, Rank def) {
        for (Rank x : values()) if (x.name().equalsIgnoreCase(s)) return x;
        return def;
    }

    public Rank next() { return this == S ? null : values()[ordinal() + 1]; }
}
