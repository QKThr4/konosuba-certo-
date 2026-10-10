package com.konorpg.item;

import com.konorpg.rpg.RpgRarity;
import com.konorpg.rpg.Stat;
import com.konorpg.skill.SkillExecutor;
import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/** Magic staff: right-click fires an arcane bolt (costs mana, scales with INT/MAG). */
public class StaffItem extends Item implements RpgGear {
    private final RpgRarity rarity;
    private final Map<Stat, Integer> bonuses;

    public StaffItem(RpgRarity rarity, Map<Stat, Integer> bonuses, int durability) {
        super(new Item.Properties().stacksTo(1).durability(durability).rarity(rarity.vanilla));
        this.rarity = rarity;
        this.bonuses = bonuses;
    }

    @Override public RpgRarity rarity() { return rarity; }
    @Override public Map<Stat, Integer> bonuses() { return bonuses; }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            if (SkillExecutor.castStaff(sp)) {
                stack.hurtAndBreak(1, sp, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                player.getCooldowns().addCooldown(this, 14);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public Component getName(ItemStack stack) { return super.getName(stack).copy().withStyle(rarity.color); }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext ctx, List<Component> lines, TooltipFlag flag) {
        RpgGear.tooltip(this, stack, lines);
        lines.add(Component.literal("Botão direito: Projétil Arcano (mana)").withStyle(net.minecraft.ChatFormatting.GRAY));
        super.appendHoverText(stack, ctx, lines, flag);
    }
}
