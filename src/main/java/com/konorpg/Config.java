package com.konorpg;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class Config {
    public static final ModConfigSpec COMMON_SPEC;
    public static final ModConfigSpec CLIENT_SPEC;

    public static final ModConfigSpec.DoubleValue XP_MULT, MONEY_MULT, MONSTER_HP_MULT, MONSTER_DMG_MULT,
            SKILL_DMG_MULT, COOLDOWN_MULT, MANA_REGEN_MULT, EVENT_SPAWN_MULT, EVENT_FREQ_MULT;
    public static final ModConfigSpec.IntValue START_MONEY, REG_FEE, MAX_COMPANIONS, PARTY_MISSION_MINUTES,
            DEATH_MONEY_LOSS_PERCENT, CITY_DISTANCE;
    public static final ModConfigSpec.BooleanValue AUTO_INTRO, EXPLOSION_BREAKS_BLOCKS, QUESTS_ENABLED,
            COMPANIONS_ENABLED, EVENTS_ENABLED, PARTY_MISSIONS_ENABLED, DIALOGUES_ENABLED;

    public static final ModConfigSpec.BooleanValue HUD_STATS, HUD_PROGRESS, HUD_SKILLS, HUD_TRACKER;
    public static final ModConfigSpec.DoubleValue HUD_SCALE;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.push("gameplay");
        XP_MULT = b.comment("Multiplicador de XP do RPG").defineInRange("xpMultiplier", 1.0, 0.0, 100.0);
        MONEY_MULT = b.comment("Multiplicador de dinheiro (Eris) ganho").defineInRange("moneyMultiplier", 1.0, 0.0, 100.0);
        MONSTER_HP_MULT = b.comment("Multiplicador de vida dos monstros do mod (dificuldade)").defineInRange("monsterHealthMultiplier", 1.0, 0.1, 20.0);
        MONSTER_DMG_MULT = b.comment("Multiplicador de dano dos monstros do mod (dificuldade)").defineInRange("monsterDamageMultiplier", 1.0, 0.1, 20.0);
        SKILL_DMG_MULT = b.comment("Multiplicador de dano das habilidades").defineInRange("skillDamageMultiplier", 1.0, 0.1, 20.0);
        COOLDOWN_MULT = b.comment("Multiplicador de cooldown das habilidades").defineInRange("cooldownMultiplier", 1.0, 0.0, 20.0);
        MANA_REGEN_MULT = b.comment("Multiplicador de regeneracao de mana").defineInRange("manaRegenMultiplier", 1.0, 0.0, 20.0);
        START_MONEY = b.comment("Eris iniciais ao chegar no outro mundo").defineInRange("startingMoney", 1200, 0, 1_000_000);
        REG_FEE = b.comment("Taxa de inscricao na Guilda").defineInRange("registrationFee", 1000, 0, 1_000_000);
        MAX_COMPANIONS = b.comment("Maximo de companheiros por jogador").defineInRange("maxCompanions", 3, 0, 8);
        DEATH_MONEY_LOSS_PERCENT = b.comment("Porcentagem de Eris perdida ao morrer").defineInRange("deathMoneyLossPercent", 10, 0, 100);
        CITY_DISTANCE = b.comment("Distancia (blocos) do spawn ate a cidade inicial").defineInRange("cityDistance", 192, 64, 2000);
        AUTO_INTRO = b.comment("Mostrar a introducao Isekai automaticamente no primeiro login").define("autoIntro", true);
        EXPLOSION_BREAKS_BLOCKS = b.comment("A habilidade Explosion destroi blocos (cratera)").define("explosionBreaksBlocks", false);
        QUESTS_ENABLED = b.define("questsEnabled", true);
        DIALOGUES_ENABLED = b.define("dialoguesEnabled", true);
        COMPANIONS_ENABLED = b.define("companionsEnabled", true);
        b.pop();
        b.push("events");
        EVENTS_ENABLED = b.comment("Eventos aleatorios no mundo").define("randomEventsEnabled", true);
        EVENT_FREQ_MULT = b.comment("Multiplicador de frequencia (maior = mais eventos)").defineInRange("eventFrequencyMultiplier", 1.0, 0.1, 20.0);
        EVENT_SPAWN_MULT = b.comment("Multiplicador da quantidade de monstros nos eventos").defineInRange("eventMonsterMultiplier", 1.0, 0.0, 10.0);
        PARTY_MISSIONS_ENABLED = b.comment("Missoes periodicas do grupo da guilda").define("partyMissionsEnabled", true);
        PARTY_MISSION_MINUTES = b.defineInRange("partyMissionEveryMinutes", 25, 2, 600);
        b.pop();
        COMMON_SPEC = b.build();

        ModConfigSpec.Builder c = new ModConfigSpec.Builder();
        c.push("hud");
        HUD_STATS = c.comment("Barra de status (nivel, HP, mana, XP)").define("showStats", true);
        HUD_PROGRESS = c.comment("HUD de progresso da aventura").define("showProgress", false);
        HUD_SKILLS = c.comment("Barra de habilidades").define("showSkills", true);
        HUD_TRACKER = c.comment("Rastreador de quest ativa").define("showQuestTracker", true);
        HUD_SCALE = c.defineInRange("scale", 1.0, 0.5, 2.0);
        c.pop();
        CLIENT_SPEC = c.build();
    }

    private Config() {}
}
