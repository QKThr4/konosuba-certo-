package com.konorpg.registry;

import com.konorpg.KonoRPG;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, KonoRPG.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.konorpg"))
                    .icon(() -> new ItemStack(ModItems.ADVENTURER_CARD.get()))
                    .displayItems((params, out) -> ModItems.ITEMS.getEntries().forEach(e -> out.accept(e.get())))
                    .build());

    private ModTabs() {}
}
