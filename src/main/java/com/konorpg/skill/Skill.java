package com.konorpg.skill;

import com.konorpg.rpg.Stat;
import java.util.Map;

public record Skill(String id, String name, String desc, String group, Type type, int minLevel, int spCost,
                    String parent, int mana, int cooldown, Map<Stat, Integer> bonuses, Action action) {

    public enum Type { ACTIVE, PASSIVE, ULTIMATE }

    @FunctionalInterface
    public interface Action {
        boolean cast(SkillCtx c);
    }
}
