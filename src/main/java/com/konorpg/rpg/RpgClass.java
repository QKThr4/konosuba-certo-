package com.konorpg.rpg;

public record RpgClass(String id, String name, int tier, String parent, String desc, int minLevel, int cost,
                       Rank minRank, Stat primary, Stat secondary) {
}
