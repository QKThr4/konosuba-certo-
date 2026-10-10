package com.konorpg.entity;

import com.konorpg.dialogue.DialogueManager;
import com.konorpg.rpg.RpgManager;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Townsfolk, guild staff, shopkeepers, guards and recruitable adventurers.
 * EM BREVE: full daily routines (sleep in beds, tavern visits), recruitable adventurer parties.
 */
public class TownNpcEntity extends PathfinderMob implements SkinProvider {
    private static final EntityDataAccessor<String> SKIN = SynchedEntityData.defineId(TownNpcEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Float> SCALE = SynchedEntityData.defineId(TownNpcEntity.class, EntityDataSerializers.FLOAT);

    private String npcId = "";
    private String npcName = "";
    private String role = "villager";
    private String dialogue = "villager";
    private String shop = "";
    private BlockPos home = BlockPos.ZERO;
    private int speechTicks;
    private int punchCount;

    public TownNpcEntity(EntityType<? extends TownNpcEntity> type, Level level) {
        super(type, level);
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder attrs() {
        return net.minecraft.world.entity.Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 30).add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 6).add(Attributes.FOLLOW_RANGE, 24);
    }

    public TownNpcEntity setup(String id, String name, String skin, float scale, String role, String dialogue, String shop, BlockPos home) {
        this.npcId = id; this.npcName = name; this.role = role; this.dialogue = dialogue; this.shop = shop; this.home = home;
        entityData.set(SKIN, skin);
        entityData.set(SCALE, scale);
        setCustomName(Component.literal(name));
        setCustomNameVisible(true);
        if (role.equals("guard")) { goalSelector.removeAllGoals(g -> true); targetSelector.removeAllGoals(g -> true); registerGoals(); }
        return this;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SKIN, "villager_a");
        builder.define(SCALE, 1.0f);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        if ("guard".equals(role)) {
            goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2, true));
            targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Monster.class, 10, true, false, e -> true));
        }
        goalSelector.addGoal(4, new StrollNearHomeGoal(this));
        goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 6.0f));
        goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    /** Wander in a small radius around the home point (the city keeps NPCs alive and in place). */
    private static final class StrollNearHomeGoal extends Goal {
        private final TownNpcEntity npc;
        private Vec3 target;

        StrollNearHomeGoal(TownNpcEntity npc) { this.npc = npc; setFlags(EnumSet.of(Flag.MOVE)); }

        @Override
        public boolean canUse() {
            if (npc.getRandom().nextInt(80) != 0 || npc.role.equals("guard")) return false;
            Vec3 v = DefaultRandomPos.getPos(npc, 6, 3);
            if (v == null) return false;
            if (npc.home != BlockPos.ZERO && v.distanceToSqr(npc.home.getX(), v.y, npc.home.getZ()) > 14 * 14) return false;
            target = v;
            return true;
        }

        @Override public boolean canContinueToUse() { return !npc.getNavigation().isDone(); }

        @Override public void start() { npc.getNavigation().moveTo(target.x, target.y, target.z, 0.8); }
    }

    public void say(String text) {
        setCustomName(Component.literal(npcName + ": " + text).withStyle(net.minecraft.ChatFormatting.YELLOW));
        speechTicks = 100;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && speechTicks > 0 && --speechTicks == 0) setCustomName(Component.literal(npcName));
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (!level().isClientSide && player instanceof ServerPlayer sp) {
            getNavigation().stop();
            getLookControl().setLookAt(player);
            DialogueManager.start(sp, this, dialogue);
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    /** NPCs cannot be killed by gameplay damage; hitting them costs reputation. */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return super.hurt(source, amount);
        if (!level().isClientSide && source.getEntity() instanceof ServerPlayer sp && speechTicks < 60) {
            say("Ai! Por que fez isso?!");
            RpgManager.addRep(sp, "city", -1);
            if (++punchCount >= 3 && role.equals("guard")) say("Última aviso, aventureiro!");
        }
        return false;
    }

    @Override public boolean removeWhenFarAway(double d) { return false; }

    @Override
    public void addAdditionalSaveData(CompoundTag t) {
        super.addAdditionalSaveData(t);
        t.putString("npcId", npcId);
        t.putString("npcName", npcName);
        t.putString("role", role);
        t.putString("dialogue", dialogue);
        t.putString("shop", shop);
        t.putString("skin", entityData.get(SKIN));
        t.putFloat("scale", entityData.get(SCALE));
        t.putLong("home", home.asLong());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag t) {
        super.readAdditionalSaveData(t);
        npcId = t.getString("npcId");
        npcName = t.getString("npcName");
        role = t.getString("role");
        dialogue = t.getString("dialogue");
        shop = t.getString("shop");
        entityData.set(SKIN, t.getString("skin").isEmpty() ? "villager_a" : t.getString("skin"));
        entityData.set(SCALE, t.contains("scale") ? t.getFloat("scale") : 1.0f);
        home = t.contains("home") ? BlockPos.of(t.getLong("home")) : BlockPos.ZERO;
        if (!npcName.isEmpty()) { setCustomName(Component.literal(npcName)); setCustomNameVisible(true); }
        // goals depend on role; refresh for guards after load
        if (role.equals("guard")) { goalSelector.removeAllGoals(g -> true); targetSelector.removeAllGoals(g -> true); registerGoals(); }
    }

    public String npcId() { return npcId; }
    public String npcName() { return npcName; }
    public String shopId() { return shop; }
    public String role() { return role; }

    @Override public String skinName() { return entityData.get(SKIN); }
    @Override public float modelScale() { return entityData.get(SCALE); }
}
