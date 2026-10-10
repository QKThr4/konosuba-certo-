package com.konorpg.entity;

import com.konorpg.Config;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/** Base for all humanoid fantasy monsters. Stats come from attributes (scaled by config at spawn). */
public abstract class RpgHumanoidMonster extends Monster implements SkinProvider {
    private final String skin;
    private final float scale;

    protected RpgHumanoidMonster(EntityType<? extends Monster> type, Level level, String skin, float scale) {
        super(type, level);
        this.skin = skin;
        this.scale = scale;
        this.xpReward = 5;
    }

    public static AttributeSupplier.Builder base(double hp, double dmg, double speed) {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, hp)
                .add(Attributes.ATTACK_DAMAGE, dmg)
                .add(Attributes.MOVEMENT_SPEED, speed)
                .add(Attributes.FOLLOW_RANGE, 26.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.1, false));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, CompanionEntity.class, true));
    }

    @Nullable
    protected Item weapon() { return null; }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance diff, MobSpawnType type, @Nullable SpawnGroupData data) {
        SpawnGroupData r = super.finalizeSpawn(level, diff, type, data);
        var hp = getAttribute(Attributes.MAX_HEALTH);
        if (hp != null) { hp.setBaseValue(hp.getBaseValue() * Config.MONSTER_HP_MULT.get()); setHealth(getMaxHealth()); }
        var dmg = getAttribute(Attributes.ATTACK_DAMAGE);
        if (dmg != null) dmg.setBaseValue(dmg.getBaseValue() * Config.MONSTER_DMG_MULT.get());
        Item w = weapon();
        if (w != null) {
            setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(w));
            setDropChance(EquipmentSlot.MAINHAND, 0.0f);
        }
        return r;
    }

    @Override public String skinName() { return skin; }

    @Override public float modelScale() { return scale; }
}
