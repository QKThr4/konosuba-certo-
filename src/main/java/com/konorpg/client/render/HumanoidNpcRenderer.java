package com.konorpg.client.render;

import com.konorpg.KonoRPG;
import com.konorpg.entity.SkinProvider;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

/** Renders any SkinProvider entity with the player model, its own skin and its own scale (tall/small variants). */
public class HumanoidNpcRenderer<T extends Mob & SkinProvider> extends HumanoidMobRenderer<T, HumanoidModel<T>> {
    private static final Map<String, ResourceLocation> CACHE = new HashMap<>();

    public HumanoidNpcRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new HumanoidModel<>(ctx.bakeLayer(ModelLayers.PLAYER)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return CACHE.computeIfAbsent(entity.skinName(), s -> KonoRPG.id("textures/entity/" + s + ".png"));
    }

    @Override
    protected void scale(T entity, PoseStack poseStack, float partialTick) {
        float s = entity.modelScale();
        poseStack.scale(s, s, s);
    }
}
