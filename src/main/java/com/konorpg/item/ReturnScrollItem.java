package com.konorpg.item;

import com.konorpg.data.WorldRpgData;
import com.konorpg.world.CityBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

/** Teleports the player back to the city plaza. */
public class ReturnScrollItem extends Item {
    public ReturnScrollItem() {
        super(new Item.Properties().stacksTo(8).rarity(Rarity.UNCOMMON));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) return InteractionResultHolder.success(stack);
        if (player instanceof ServerPlayer sp) {
            WorldRpgData w = WorldRpgData.get(sp.server);
            if (!w.cityBuilt) {
                sp.displayClientMessage(net.minecraft.network.chat.Component.literal("A cidade ainda não existe neste mundo."), true);
                return InteractionResultHolder.fail(stack);
            }
            CityBuilder.teleportToPlaza(sp);
            if (!sp.getAbilities().instabuild) stack.shrink(1);
        }
        return InteractionResultHolder.consume(stack);
    }
}
