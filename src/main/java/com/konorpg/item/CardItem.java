package com.konorpg.item;

import com.konorpg.network.Net;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

/** The Adventurer Card: right-click to open the RPG hub on the Card tab. */
public class CardItem extends Item {
    public CardItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    public static ItemStack create(Player owner) {
        ItemStack s = new ItemStack(com.konorpg.registry.ModItems.ADVENTURER_CARD.get());
        CompoundTag t = new CompoundTag();
        t.putString("owner", owner.getGameProfile().getName());
        s.set(DataComponents.CUSTOM_DATA, CustomData.of(t));
        return s;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer sp) Net.openHub(sp, "card");
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext ctx, List<Component> lines, TooltipFlag flag) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        if (cd != null && cd.copyTag().contains("owner")) {
            lines.add(Component.literal("Aventureiro: " + cd.copyTag().getString("owner")).withStyle(ChatFormatting.GOLD));
        }
        lines.add(Component.literal("Botão direito: abrir o Cartão").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, ctx, lines, flag);
    }
}
