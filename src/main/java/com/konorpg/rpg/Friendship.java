package com.konorpg.rpg;

public final class Friendship {
    public static final String[] NAMES = {"Stranger", "Acquaintance", "Friend", "Close Friend", "Trusted", "Best Friend"};
    public static final int[] THRESHOLDS = {0, 10, 30, 70, 130, 220};

    public static int level(int points) {
        int l = 0;
        for (int i = 0; i < THRESHOLDS.length; i++) if (points >= THRESHOLDS[i]) l = i;
        return l;
    }

    public static String name(int points) { return NAMES[level(points)]; }

    private Friendship() {}
}
