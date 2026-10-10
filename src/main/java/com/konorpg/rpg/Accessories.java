package com.konorpg.rpg;

import com.konorpg.data.PlayerRpgData;
import com.konorpg.item.AccessoryItem;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

/** RPG accessory slots: 0,1 = rings, 2 = necklace. Stored in the player's persistent data. */
public final class Accessories {
    public static boolean equipFrom(ServerPlayer p, InteractionHand hand) {
        ItemStack held = p.getItemInHand(hand);
        if (!(held.getItem() instanceof AccessoryItem acc)) return false;
        PlayerRpgData d = RpgManager.data(p);
        int[] candidates = acc.kind() == AccessoryItem.Kind.RING ? new int[]{0, 1} : new int[]{2};
        int slot = -1;
        for (int i : candidates) if (d.accessories[i].isEmpty()) { slot = i; break; }
        if (slot < 0) slot = candidates[0];
        ItemStack old = d.accessories[slot];
        d.accessories[slot] = held.copyWithCount(1);
        held.shrink(1);
        if (!old.isEmpty() && !p.getInventory().add(old)) p.drop(old, false);
        p.displayClientMessage(Component.literal("Equipado: ").append(d.accessories[slot].getHoverName()), true);
        RpgManager.refreshAttributes(p);
        RpgManager.sync(p);
        return true;
    }

    public static void equipHeld(ServerPlayer p) {
        equipFrom(p, InteractionHand.MAIN_HAND);
    }

    public static void unequip(ServerPlayer p, int slot) {
        if (slot < 0 || slot > 2) return;
        PlayerRpgData d = RpgManager.data(p);
        ItemStack s = d.accessories[slot];
        if (s.isEmpty()) return;
        d.accessories[slot] = ItemStack.EMPTY;
        if (!p.getInventory().add(s)) p.drop(s, false);
        RpgManager.refreshAttributes(p);
        RpgManager.sync(p);
    }

    private Accessories() {}
}
