package com.konorpg.item;

import com.konorpg.rpg.RpgRarity;
import com.konorpg.rpg.Stat;
import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class HunterBowItem extends BowItem implements RpgGear {
    private final RpgRarity rarity;
    private final Map<Stat, Integer> bonuses;

    public HunterBowItem(RpgRarity rarity, Map<Stat, Integer> bonuses, int durability) {
        super(new Item.Properties().stacksTo(1).durability(durability).rarity(rarity.vanilla));
        this.rarity = rarity;
        this.bonuses = bonuses;
    }

    @Override public RpgRarity rarity() { return rarity; }
    @Override public Map<Stat, Integer> bonuses() { return bonuses; }

    @Override
    public Component getName(ItemStack stack) { return super.getName(stack).copy().withStyle(rarity.color); }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext ctx, List<Component> lines, TooltipFlag flag) {
        RpgGear.tooltip(this, stack, lines);
        super.appendHoverText(stack, ctx, lines, flag);
    }
}
