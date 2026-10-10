package com.konorpg.item;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

public enum RpgTier implements Tier {
    ADVENTURER(250, 6.0f, 0.0f, 12),
    SOLDIER(500, 6.0f, 1.0f, 14),
    KNIGHT(900, 7.0f, 2.0f, 15),
    LEGEND(2200, 8.0f, 4.0f, 22);

    private final int uses, ench;
    private final float speed, bonus;

    RpgTier(int uses, float speed, float bonus, int ench) {
        this.uses = uses; this.speed = speed; this.bonus = bonus; this.ench = ench;
    }

    @Override public int getUses() { return uses; }
    @Override public float getSpeed() { return speed; }
    @Override public float getAttackDamageBonus() { return bonus; }
    @Override public TagKey<Block> getIncorrectBlocksForDrops() { return BlockTags.INCORRECT_FOR_IRON_TOOL; }
    @Override public int getEnchantmentValue() { return ench; }
    @Override public Ingredient getRepairIngredient() { return Ingredient.of(Items.IRON_INGOT); }
}
