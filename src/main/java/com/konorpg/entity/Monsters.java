package com.konorpg.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Fantasy monsters. All are humanoid-model creatures with their own skin and size.
 * EM BREVE: Giant Toad custom model, Mimic (disguise), Giant Spider, Golem, Demon, flying monsters, Dragon.
 */
public final class Monsters {
    public static class Goblin extends RpgHumanoidMonster {
        public Goblin(EntityType<? extends Goblin> t, Level l) { super(t, l, "goblin", 0.8f); xpReward = 6; }
        public static AttributeSupplier.Builder attrs() { return base(14, 3, 0.30); }
        @Override protected Item weapon() { return Items.WOODEN_SWORD; }
    }

    public static class Kobold extends RpgHumanoidMonster {
        public Kobold(EntityType<? extends Kobold> t, Level l) { super(t, l, "kobold", 0.7f); xpReward = 5; }
        public static AttributeSupplier.Builder attrs() { return base(10, 2.5, 0.33); }
        @Override protected Item weapon() { return Items.STONE_SWORD; }
    }

    public static class Orc extends RpgHumanoidMonster {
        public Orc(EntityType<? extends Orc> t, Level l) { super(t, l, "orc", 1.2f); xpReward = 15; }
        public static AttributeSupplier.Builder attrs() { return base(40, 7, 0.25); }
        @Override protected Item weapon() { return Items.IRON_AXE; }
    }

    public static class Lizardman extends RpgHumanoidMonster {
        public Lizardman(EntityType<? extends Lizardman> t, Level l) { super(t, l, "lizardman", 1.0f); xpReward = 12; }
        public static AttributeSupplier.Builder attrs() { return base(28, 5, 0.28); }
        @Override protected Item weapon() { return Items.IRON_SWORD; }
    }

    public static class SkeletonWarrior extends RpgHumanoidMonster {
        public SkeletonWarrior(EntityType<? extends SkeletonWarrior> t, Level l) { super(t, l, "skeleton_warrior", 1.0f); xpReward = 10; }
        public static AttributeSupplier.Builder attrs() { return base(22, 5, 0.27); }
        @Override protected Item weapon() { return Items.IRON_SWORD; }
        @Override protected boolean isSunBurnTick() { return false; }
    }

    /** Placeholder body until the custom toad model arrives (EM BREVE). */
    public static class GiantToad extends RpgHumanoidMonster {
        public GiantToad(EntityType<? extends GiantToad> t, Level l) { super(t, l, "giant_toad", 1.1f); xpReward = 8; }
        public static AttributeSupplier.Builder attrs() { return base(20, 4, 0.30); }
    }

    private Monsters() {}
}
