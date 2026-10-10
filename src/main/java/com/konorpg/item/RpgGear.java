package com.konorpg.item;

import com.konorpg.rpg.RpgRarity;
import com.konorpg.rpg.Stat;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Marker for RPG equipment that has a rarity and gives stat bonuses while equipped / held. */
public interface RpgGear {
    RpgRarity rarity();

    Map<Stat, Integer> bonuses();

    static void tooltip(RpgGear g, ItemStack stack, List<Component> lines) {
        lines.add(Component.literal(g.rarity().label).withStyle(g.rarity().color));
        g.bonuses().forEach((s, v) -> lines.add(Component.literal((v >= 0 ? "+" : "") + v + " " + s.label)
                .withStyle(v >= 0 ? ChatFormatting.BLUE : ChatFormatting.RED)));
    }

    static int reinforce(ItemStack stack) {
        var cd = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        return cd == null ? 0 : cd.copyTag().getInt("konorpg_plus");
    }
}
