package com.konorpg;

import com.konorpg.registry.ModAttachments;
import com.konorpg.registry.ModBlocks;
import com.konorpg.registry.ModEntities;
import com.konorpg.registry.ModItems;
import com.konorpg.registry.ModTabs;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(KonoRPG.MOD_ID)
public class KonoRPG {
    public static final String MOD_ID = "konorpg";
    public static final Logger LOGGER = LogUtils.getLogger();

    public KonoRPG(IEventBus modBus, ModContainer container) {
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModEntities.ENTITIES.register(modBus);
        ModAttachments.ATTACHMENTS.register(modBus);
        ModTabs.TABS.register(modBus);
        container.registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_SPEC);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
