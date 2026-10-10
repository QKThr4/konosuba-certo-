package com.konorpg.client;

import com.konorpg.KonoRPG;
import com.konorpg.client.render.HumanoidNpcRenderer;
import com.konorpg.registry.ModEntities;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = KonoRPG.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {
    public static final String CAT = "key.categories.konorpg";
    public static final KeyMapping HUB = new KeyMapping("key.konorpg.hub", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, CAT);
    public static final KeyMapping HUD = new KeyMapping("key.konorpg.hud", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, CAT);
    public static final KeyMapping[] SKILLS = {
            new KeyMapping("key.konorpg.skill1", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, CAT),
            new KeyMapping("key.konorpg.skill2", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_X, CAT),
            new KeyMapping("key.konorpg.skill3", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, CAT),
            new KeyMapping("key.konorpg.skill4", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, CAT)};

    @SubscribeEvent
    public static void keys(RegisterKeyMappingsEvent e) {
        e.register(HUB);
        e.register(HUD);
        for (KeyMapping k : SKILLS) e.register(k);
    }

    @SubscribeEvent
    public static void renderers(EntityRenderersEvent.RegisterRenderers e) {
        e.registerEntityRenderer(ModEntities.GOBLIN.get(), HumanoidNpcRenderer::new);
        e.registerEntityRenderer(ModEntities.KOBOLD.get(), HumanoidNpcRenderer::new);
        e.registerEntityRenderer(ModEntities.ORC.get(), HumanoidNpcRenderer::new);
        e.registerEntityRenderer(ModEntities.LIZARDMAN.get(), HumanoidNpcRenderer::new);
        e.registerEntityRenderer(ModEntities.SKELETON_WARRIOR.get(), HumanoidNpcRenderer::new);
        e.registerEntityRenderer(ModEntities.GIANT_TOAD.get(), HumanoidNpcRenderer::new);
        e.registerEntityRenderer(ModEntities.TOWN_NPC.get(), HumanoidNpcRenderer::new);
        e.registerEntityRenderer(ModEntities.COMPANION.get(), HumanoidNpcRenderer::new);
    }

    @SubscribeEvent
    public static void layers(RegisterGuiLayersEvent e) {
        e.registerAboveAll(KonoRPG.id("rpg_hud"), new HudLayer());
    }

    private ClientSetup() {}
}
