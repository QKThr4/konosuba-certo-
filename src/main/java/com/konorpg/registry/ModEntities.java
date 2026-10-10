package com.konorpg.registry;

import com.konorpg.KonoRPG;
import com.konorpg.entity.CompanionEntity;
import com.konorpg.entity.Monsters;
import com.konorpg.entity.TownNpcEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, KonoRPG.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<Monsters.Goblin>> GOBLIN = ENTITIES.register("goblin",
            () -> EntityType.Builder.of(Monsters.Goblin::new, MobCategory.MONSTER).sized(0.5f, 1.4f).clientTrackingRange(8).build("goblin"));
    public static final DeferredHolder<EntityType<?>, EntityType<Monsters.Kobold>> KOBOLD = ENTITIES.register("kobold",
            () -> EntityType.Builder.of(Monsters.Kobold::new, MobCategory.MONSTER).sized(0.5f, 1.3f).clientTrackingRange(8).build("kobold"));
    public static final DeferredHolder<EntityType<?>, EntityType<Monsters.Orc>> ORC = ENTITIES.register("orc",
            () -> EntityType.Builder.of(Monsters.Orc::new, MobCategory.MONSTER).sized(0.8f, 2.2f).clientTrackingRange(8).build("orc"));
    public static final DeferredHolder<EntityType<?>, EntityType<Monsters.Lizardman>> LIZARDMAN = ENTITIES.register("lizardman",
            () -> EntityType.Builder.of(Monsters.Lizardman::new, MobCategory.MONSTER).sized(0.6f, 1.9f).clientTrackingRange(8).build("lizardman"));
    public static final DeferredHolder<EntityType<?>, EntityType<Monsters.SkeletonWarrior>> SKELETON_WARRIOR = ENTITIES.register("skeleton_warrior",
            () -> EntityType.Builder.of(Monsters.SkeletonWarrior::new, MobCategory.MONSTER).sized(0.6f, 1.9f).clientTrackingRange(8).build("skeleton_warrior"));
    public static final DeferredHolder<EntityType<?>, EntityType<Monsters.GiantToad>> GIANT_TOAD = ENTITIES.register("giant_toad",
            () -> EntityType.Builder.of(Monsters.GiantToad::new, MobCategory.MONSTER).sized(0.7f, 1.6f).clientTrackingRange(8).build("giant_toad"));

    public static final DeferredHolder<EntityType<?>, EntityType<TownNpcEntity>> TOWN_NPC = ENTITIES.register("town_npc",
            () -> EntityType.Builder.of(TownNpcEntity::new, MobCategory.MISC).sized(0.6f, 1.95f).clientTrackingRange(10).build("town_npc"));
    public static final DeferredHolder<EntityType<?>, EntityType<CompanionEntity>> COMPANION = ENTITIES.register("companion",
            () -> EntityType.Builder.of(CompanionEntity::new, MobCategory.MISC).sized(0.6f, 1.95f).clientTrackingRange(10).build("companion"));

    private ModEntities() {}
}
