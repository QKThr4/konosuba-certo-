package com.konorpg.rpg;

import java.util.List;

public final class Factions {
    public static final List<String> IDS = List.of("guild", "city", "merchants", "axis");

    public static String name(String id) {
        return switch (id) {
            case "guild" -> "Guilda dos Aventureiros";
            case "city" -> "Cidade de Axel";
            case "merchants" -> "Comerciantes";
            case "axis" -> "Ordem Axis (Culto de Aqua)";
            default -> id;
        };
    }

    public static String perk(String id) {
        return switch (id) {
            case "guild" -> "Libera quests de rank maior e prestígio na Guilda";
            case "city" -> "Moradores mais simpáticos, guardas te respeitam";
            case "merchants" -> "Descontos nas lojas (até 30%)";
            case "axis" -> "Aqua fica mais animada (e a Ordem te dá presentes)";
            default -> "";
        };
    }

    public static String tier(int rep) {
        if (rep < -20) return "Odiado";
        if (rep < 0) return "Desconfiado";
        if (rep < 20) return "Neutro";
        if (rep < 60) return "Conhecido";
        if (rep < 120) return "Respeitado";
        if (rep < 200) return "Honrado";
        return "Reverenciado";
    }

    /** Discount (+) or surcharge (-) percent applied to shop prices. */
    public static int shopDiscount(int merchantsRep) {
        return Math.max(-20, Math.min(30, merchantsRep / 4));
    }

    private Factions() {}
}
