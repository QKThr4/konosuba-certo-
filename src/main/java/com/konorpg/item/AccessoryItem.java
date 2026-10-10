package com.konorpg.item;

import com.konorpg.rpg.Accessories;
import com.konorpg.rpg.RpgRarity;
import com.konorpg.rpg.Stat;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/** Rings and necklaces. Right-click equips them in the RPG accessory slots. */
public class AccessoryItem extends Item implements RpgGear {
    public enum Kind { RING, NECKLACE }

    private final Kind kind;
    private final RpgRarity rarity;
    private final Map<Stat, Integer> bonuses;

    public AccessoryItem(Kind kind, RpgRarity rarity, Map<Stat, Integer> bonuses) {
        super(new Item.Properties().stacksTo(1).rarity(rarity.vanilla));
        this.kind = kind;
        this.rarity = rarity;
        this.bonuses = bonuses;
    }

    public Kind kind() { return kind; }
    @Override public RpgRarity rarity() { return rarity; }
    @Override public Map<Stat, Integer> bonuses() { return bonuses; }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            if (Accessories.equipFrom(sp, hand)) return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), false);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public Component getName(ItemStack stack) { return super.getName(stack).copy().withStyle(rarity.color); }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext ctx, List<Component> lines, TooltipFlag flag) {
        RpgGear.tooltip(this, stack, lines);
        lines.add(Component.literal(kind == Kind.RING ? "Anel (2 espaços)" : "Colar (1 espaço)").withStyle(ChatFormatting.GRAY));
        lines.add(Component.literal("Botão direito para equipar").withStyle(ChatFormatting.DARK_GRAY));
        super.appendHoverText(stack, ctx, lines, flag);
    }
}
