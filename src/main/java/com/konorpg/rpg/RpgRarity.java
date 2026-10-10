package com.konorpg.rpg;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Rarity;

public enum RpgRarity {
    COMMON("Comum", ChatFormatting.WHITE, Rarity.COMMON),
    UNCOMMON("Incomum", ChatFormatting.GREEN, Rarity.UNCOMMON),
    RARE("Raro", ChatFormatting.AQUA, Rarity.RARE),
    EPIC("Épico", ChatFormatting.LIGHT_PURPLE, Rarity.EPIC),
    LEGENDARY("Lendário", ChatFormatting.GOLD, Rarity.EPIC),
    MYTHIC("Mítico", ChatFormatting.RED, Rarity.EPIC);

    public final String label;
    public final ChatFormatting color;
    public final Rarity vanilla;

    RpgRarity(String label, ChatFormatting color, Rarity vanilla) {
        this.label = label; this.color = color; this.vanilla = vanilla;
    }
}
