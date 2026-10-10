package com.konorpg.entity;

import com.konorpg.data.CompanionRecord;
import com.konorpg.rpg.CompanionManager;
import com.konorpg.dialogue.DialogueManager;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * A materialized companion (currently Aqua). Data lives in the owner's CompanionRecord; this entity is
 * created next to the owner and discarded on logout. EM BREVE: more kinds (recruited adventurers, Megumin, Darkness).
 */
public class CompanionEntity extends PathfinderMob implements SkinProvider {
    private static final EntityDataAccessor<String> SKIN = SynchedEntityData.defineId(CompanionEntity.class, EntityDataSerializers.STRING);
    private static final String[] AQUA_IDLE = {
            "Ei, você sabe que eu sou uma DEUSA, né? Podia me tratar melhor!",
            "Estou com sede... não tem uma taverna por aqui?",
            "Aquela cidade devia ter uma estátua minha. Maior que a da Eris.",
            "Eu poderia purificar este lugar inteiro, sabia? Quando eu estiver com vontade.",
            "Vamos voltar pra Guilda? Estou entediada e meus pés doem.",
            "Se aparecer um sapo gigante, você vai na frente, tá?"};
    private static final String[] AQUA_FRIEND = {
            "Sabe... até que você não é tão ruim, aventureiro.",
            "Se você morrer eu ressuscito você. De graça! ...Dessa vez."};
    private static final String[] AQUA_BEST = {
            "Você é o meu melhor amigo... não conta pra Eris!",
            "Obrigada por me levar junto. De verdade."};

    private String uid = "";
    private String kind = "aqua";
    private String displayName = "Aqua";
    private UUID ownerUuid;
    private int level = 1;
    public boolean stay;
    private long lastSpeak, nextHeal, nextPurify, nextIdle;
    private int speechTicks;

    public CompanionEntity(EntityType<? extends CompanionEntity> type, Level level) {
        super(type, level);
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder attrs() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 30).add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 2).add(Attributes.FOLLOW_RANGE, 32);
    }

    public void setup(CompanionRecord rec, ServerPlayer owner) {
        this.uid = rec.uid;
        this.kind = rec.kind;
        this.displayName = rec.name;
        this.ownerUuid = owner.getUUID();
        this.stay = rec.stay;
        refresh(rec);
        entityData.set(SKIN, kind.equals("aqua") ? "aqua" : "villager_b");
        setCustomName(Component.literal(displayName));
        setCustomNameVisible(true);
        setHealth(getMaxHealth());
    }

    public void refresh(CompanionRecord rec) {
        this.level = rec.level;
        this.stay = rec.stay;
        var hp = getAttribute(Attributes.MAX_HEALTH);
        if (hp != null) hp.setBaseValue(30 + rec.level * 2.0);
        var atk = getAttribute(Attributes.ATTACK_DAMAGE);
        if (atk != null) atk.setBaseValue(2 + rec.level * 0.4);
        if (!rec.weapon.isEmpty()) setItemSlot(EquipmentSlot.MAINHAND, rec.weapon.copy());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SKIN, "aqua");
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, true));
        goalSelector.addGoal(4, new FollowOwnerGoal(this));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    private static final class FollowOwnerGoal extends Goal {
        private final CompanionEntity c;
        private int recalc;

        FollowOwnerGoal(CompanionEntity c) { this.c = c; setFlags(EnumSet.of(Flag.MOVE)); }

        @Override public boolean canUse() {
            ServerPlayer o = c.owner();
            return o != null && !c.stay && c.getTarget() == null && c.distanceToSqr(o) > 36;
        }

        @Override public boolean canContinueToUse() {
            ServerPlayer o = c.owner();
            return o != null && !c.stay && c.getTarget() == null && c.distanceToSqr(o) > 9;
        }

        @Override public void tick() {
            ServerPlayer o = c.owner();
            if (o == null || --recalc > 0) return;
            recalc = 10;
            if (c.distanceToSqr(o) > 24 * 24 && o.onGround()) {
                c.getNavigation().stop();
                c.moveTo(o.getX() + 1.0, o.getY(), o.getZ() + 1.0, c.getYRot(), c.getXRot());
            } else {
                c.getNavigation().moveTo(o, 1.25);
            }
        }
    }

    public ServerPlayer owner() {
        if (ownerUuid == null) return null;
        Player p = level().getPlayerByUUID(ownerUuid);
        return p instanceof ServerPlayer sp ? sp : null;
    }

    public boolean isOwnedBy(Player p) { return ownerUuid != null && ownerUuid.equals(p.getUUID()); }

    public String uid() { return uid; }

    public String kind() { return kind; }

    // ---------------- speech ----------------

    public void say(String line) {
        ServerPlayer o = owner();
        if (o != null) o.sendSystemMessage(Component.literal("<" + displayName + "> ").withStyle(ChatFormatting.AQUA).append(Component.literal(line).withStyle(ChatFormatting.WHITE)));
        setCustomName(Component.literal(displayName + ": " + line).withStyle(ChatFormatting.AQUA));
        speechTicks = 100;
    }

    /** Context-aware comments, personality inspired by the original goddess. */
    public void speak(String trigger) {
        long gt = level().getGameTime();
        if (gt - lastSpeak < 100) return;
        lastSpeak = gt;
        int fl = 0;
        ServerPlayer o = owner();
        if (o != null) fl = com.konorpg.rpg.RpgManager.data(o).friendLevel(kind);
        String line = switch (trigger) {
            case "idle" -> fl >= 5 ? pick(AQUA_BEST) : fl >= 3 ? pick(AQUA_FRIEND) : pick(AQUA_IDLE);
            case "kill" -> pick(new String[]{"Boa! Isso foi graças à minha bênção, claro.", "Eu ajudei! Viu? Eu ajudei!", "Mais um! A deusa aprova."});
            case "hurt" -> pick(new String[]{"Cuidado!! Se você morrer, quem vai pagar minha bebida?!", "Aaaah! Se cure logo, eu estou... aqui por perto!"});
            case "levelup" -> "Subiu de nível? Obviamente graças a mim. Me agradeça com sake.";
            case "rank" -> "Rank novo! Eu mereço metade do crédito.";
            case "quest_accept" -> "Uma quest? Fácil! Eu cuido de tudo... você vai na frente.";
            case "quest_done" -> "Recompensa! Vamos gastar tudo em sake! ...Tá bom, em comida.";
            case "night" -> "Está escuro... eu NÃO estou com medo. É só que mortos-vivos fedem.";
            case "rain" -> "Chuva? Eu sou a deusa da água! Isso me deixa de bom humor.";
            case "heal" -> "Heal! Pode agradecer depois.";
            case "heal_fail" -> "Ops! Curei o monstro em vez de você... Foi sem querer!";
            case "purify" -> "Turn Undead!! Sinta o poder de uma deusa!";
            case "resurrect" -> "Eu sou uma deusa! Ressuscitar você é moleza... Sacred Resurrection!";
            case "resurrect_fail" -> "Eh... a magia falhou?! Perdão! Ainda está em recarga, eu juro!";
            case "hit" -> "Você me bateu?! Vou contar pra Eris!";
            case "welcome" -> "Finalmente você virou aventureiro! Agora eu posso ajudar de verdade. Provavelmente.";
            default -> null;
        };
        if (line != null) say(line);
    }

    private String pick(String[] a) { return a[getRandom().nextInt(a.length)]; }

    // ---------------- behaviour ----------------

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;
        if (ownerUuid == null) { discard(); return; } // loaded from disk: manager already created a fresh one
        if (speechTicks > 0 && --speechTicks == 0) setCustomName(Component.literal(displayName));
        if (tickCount % 20 == 0 && getHealth() < getMaxHealth()) heal(0.5f);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        ServerPlayer o = owner();
        if (o == null || o.level() != level()) return;
        long gt = level().getGameTime();
        if (tickCount % 10 == 0) {
            LivingEntity t = getTarget();
            if (t == null || !t.isAlive()) {
                t = o.getLastHurtByMob();
                if (t == null || !t.isAlive() || t.distanceToSqr(this) > 400 || t instanceof Player) t = nearestEnemy(12);
                if (!stay || (t != null && t.distanceToSqr(this) < 36)) setTarget(t);
            }
            if (kind.equals("aqua")) aquaAbilities(o, gt);
            if (gt >= nextIdle) {
                nextIdle = gt + 1200 + getRandom().nextInt(1800);
                speak("idle");
            }
        }
        if (tickCount % 400 == 0 && getRandom().nextInt(3) == 0) {
            if (level().isNight()) speak("night");
            else if (level().isRaining()) speak("rain");
        }
    }

    private LivingEntity nearestEnemy(double r) {
        List<Monster> list = level().getEntitiesOfClass(Monster.class, getBoundingBox().inflate(r), Monster::isAlive);
        Monster best = null;
        for (Monster m : list) if (best == null || m.distanceToSqr(this) < best.distanceToSqr(this)) best = m;
        return best;
    }

    private void aquaAbilities(ServerPlayer o, long gt) {
        if (gt >= nextHeal && o.getHealth() < o.getMaxHealth() * 0.6f && distanceToSqr(o) < 144) {
            nextHeal = gt + 160;
            if (getRandom().nextFloat() < 0.12f) {
                speak("heal_fail");
            } else {
                o.heal(6 + level * 0.5f);
                ((ServerLevel) level()).sendParticles(ParticleTypes.HEART, o.getX(), o.getY() + 1.8, o.getZ(), 6, 0.3, 0.3, 0.3, 0);
                speak("heal");
            }
        }
        if (gt >= nextPurify) {
            List<Monster> undead = level().getEntitiesOfClass(Monster.class, getBoundingBox().inflate(8), m -> m.isAlive() && m.getType().is(EntityTypeTags.UNDEAD));
            if (!undead.isEmpty()) {
                nextPurify = gt + 200;
                for (Monster m : undead) m.hurt(level().damageSources().magic(), 8 + level);
                ((ServerLevel) level()).sendParticles(ParticleTypes.END_ROD, getX(), getY() + 1, getZ(), 40, 2, 0.5, 2, 0.05);
                speak("purify");
            }
        }
    }

    @Override
    public boolean hurt(DamageSource s, float amount) {
        if (s.getEntity() instanceof Player) {
            if (!level().isClientSide) { speak("hit"); ServerPlayer o = owner(); if (o != null) CompanionManager.addFriendship(o, kind, -3); }
            return false;
        }
        if (!s.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && getHealth() - amount <= 2.0f) {
            setHealth(Math.max(getHealth(), 4.0f));
            if (!level().isClientSide) speak("hurt");
            return false;
        }
        return super.hurt(s, amount);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (!level().isClientSide && player instanceof ServerPlayer sp) {
            if (isOwnedBy(sp)) DialogueManager.start(sp, this, kind);
            else say("Eu pertenço a outro aventureiro!");
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Override public boolean removeWhenFarAway(double d) { return false; }

    @Override public void addAdditionalSaveData(CompoundTag t) { super.addAdditionalSaveData(t); }

    @Override public String skinName() { return entityData.get(SKIN); }

    @Override public float modelScale() { return 1.0f; }
}
