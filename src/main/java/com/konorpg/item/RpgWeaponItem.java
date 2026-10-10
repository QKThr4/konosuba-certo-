package com.konorpg.item;

import com.konorpg.rpg.RpgRarity;
import com.konorpg.rpg.Stat;
import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;

public class RpgWeaponItem extends SwordItem implements RpgGear {
    private final RpgRarity rarity;
    private final Map<Stat, Integer> bonuses;

    public RpgWeaponItem(Tier tier, int damage, float speed, RpgRarity rarity, Map<Stat, Integer> bonuses) {
        super(tier, new Item.Properties().attributes(SwordItem.createAttributes(tier, damage, speed)).rarity(rarity.vanilla));
        this.rarity = rarity;
        this.bonuses = bonuses;
    }

    @Override public RpgRarity rarity() { return rarity; }
    @Override public Map<Stat, Integer> bonuses() { return bonuses; }

    @Override
    public Component getName(ItemStack stack) {
        int plus = RpgGear.reinforce(stack);
        Component base = super.getName(stack).copy().withStyle(rarity.color);
        return plus > 0 ? Component.empty().append(base).append(Component.literal(" +" + plus)) : base;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext ctx, List<Component> lines, TooltipFlag flag) {
        RpgGear.tooltip(this, stack, lines);
        super.appendHoverText(stack, ctx, lines, flag);
    }
}
