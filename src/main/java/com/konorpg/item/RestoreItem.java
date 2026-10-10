package com.konorpg.item;

import com.konorpg.data.PlayerRpgData;
import com.konorpg.rpg.RpgManager;
import com.konorpg.rpg.RpgStats;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Instant-use potion restoring HP and/or mana. */
public class RestoreItem extends Item {
    private final float hp;
    private final double mana;

    public RestoreItem(float hp, double mana, net.minecraft.world.item.Rarity rarity) {
        super(new Item.Properties().stacksTo(16).rarity(rarity));
        this.hp = hp;
        this.mana = mana;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) return InteractionResultHolder.success(stack);
        if (player instanceof ServerPlayer sp) {
            PlayerRpgData d = RpgManager.data(sp);
            boolean needHp = hp > 0 && sp.getHealth() < sp.getMaxHealth();
            boolean needMana = mana > 0 && d.mana < RpgStats.maxMana(sp, d);
            if (!needHp && !needMana) return InteractionResultHolder.fail(stack);
            if (hp > 0) sp.heal(hp);
            if (mana > 0) d.mana = Math.min(RpgStats.maxMana(sp, d), d.mana + mana);
            if (!sp.getAbilities().instabuild) stack.shrink(1);
            sp.level().playSound(null, sp.blockPosition(), net.minecraft.sounds.SoundEvents.GENERIC_DRINK,
                    net.minecraft.sounds.SoundSource.PLAYERS, 1f, 1f);
            RpgManager.sync(sp);
        }
        return InteractionResultHolder.consume(stack);
    }
}
