package com.konorpg.event;

import com.konorpg.KonoRPG;
import com.konorpg.entity.CompanionEntity;
import com.konorpg.entity.Monsters;
import com.konorpg.entity.TownNpcEntity;
import com.konorpg.registry.ModEntities;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

/** Mod-bus (registration time) events shared by client and server. */
@EventBusSubscriber(modid = KonoRPG.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class ModEvents {
    @SubscribeEvent
    public static void attributes(EntityAttributeCreationEvent e) {
        e.put(ModEntities.GOBLIN.get(), Monsters.Goblin.attrs().build());
        e.put(ModEntities.KOBOLD.get(), Monsters.Kobold.attrs().build());
        e.put(ModEntities.ORC.get(), Monsters.Orc.attrs().build());
        e.put(ModEntities.LIZARDMAN.get(), Monsters.Lizardman.attrs().build());
        e.put(ModEntities.SKELETON_WARRIOR.get(), Monsters.SkeletonWarrior.attrs().build());
        e.put(ModEntities.GIANT_TOAD.get(), Monsters.GiantToad.attrs().build());
        e.put(ModEntities.TOWN_NPC.get(), TownNpcEntity.attrs().build());
        e.put(ModEntities.COMPANION.get(), CompanionEntity.attrs().build());
    }

    @SubscribeEvent
    public static void spawns(RegisterSpawnPlacementsEvent e) {
        reg(e, ModEntities.GOBLIN.get());
        reg(e, ModEntities.KOBOLD.get());
        reg(e, ModEntities.ORC.get());
        reg(e, ModEntities.LIZARDMAN.get());
        reg(e, ModEntities.SKELETON_WARRIOR.get());
        reg(e, ModEntities.GIANT_TOAD.get());
    }

    private static <T extends Monster> void reg(RegisterSpawnPlacementsEvent e, net.minecraft.world.entity.EntityType<T> type) {
        e.register(type, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (t, level, reason, pos, rand) -> !com.konorpg.world.CityInfo.inCity(pos) && Mob.checkMobSpawnRules(t, level, reason, pos, rand),
                RegisterSpawnPlacementsEvent.Operation.OR);
    }

    private ModEvents() {}
}
