package com.konorpg.skill;

import com.konorpg.data.PlayerRpgData;
import com.konorpg.entity.CompanionEntity;
import com.konorpg.entity.TownNpcEntity;
import com.konorpg.rpg.RpgStats;
import com.konorpg.Config;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Helper bundle handed to every skill implementation. */
public final class SkillCtx {
    public final ServerPlayer p;
    public final ServerLevel level;
    public final PlayerRpgData d;

    public SkillCtx(ServerPlayer p, PlayerRpgData d) {
        this.p = p;
        this.d = d;
        this.level = p.serverLevel();
    }

    public record Hit(LivingEntity target, Vec3 end) {}

    public double spell() { return RpgStats.spellPower(p, d) * Config.SKILL_DMG_MULT.get(); }

    public double phys() { return RpgStats.physPower(p, d) * Config.SKILL_DMG_MULT.get(); }

    public boolean isFoe(Entity e) {
        return e instanceof LivingEntity le && le.isAlive() && e != p && !(e instanceof Player)
                && !(e instanceof TownNpcEntity) && !(e instanceof CompanionEntity);
    }

    public Hit ray(double range) {
        Vec3 start = p.getEyePosition();
        Vec3 dir = p.getLookAngle();
        Vec3 end = start.add(dir.scale(range));
        BlockHitResult bhr = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p));
        if (bhr.getType() != HitResult.Type.MISS) end = bhr.getLocation();
        AABB box = p.getBoundingBox().expandTowards(dir.scale(range)).inflate(1.0);
        EntityHitResult ehr = ProjectileUtil.getEntityHitResult(level, p, start, end, box, e -> isFoe(e));
        if (ehr != null && ehr.getEntity() instanceof LivingEntity le) return new Hit(le, ehr.getLocation());
        return new Hit(null, end);
    }

    public List<LivingEntity> foesNear(Vec3 c, double r) {
        List<LivingEntity> out = new ArrayList<>();
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new AABB(c, c).inflate(r))) {
            if (isFoe(e) && e instanceof Enemy && e.position().distanceToSqr(c) <= r * r) out.add(e);
        }
        return out;
    }

    public List<LivingEntity> allies(double r) {
        List<LivingEntity> out = new ArrayList<>();
        out.add(p);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(r))) {
            if (e == p) continue;
            if (e instanceof Player) out.add(e);
            else if (e instanceof CompanionEntity ce && ce.isOwnedBy(p)) out.add(e);
        }
        return out;
    }

    public void magic(LivingEntity t, float amount) {
        t.hurt(level.damageSources().indirectMagic(p, p), amount);
        if (t instanceof net.minecraft.world.entity.Mob m && m.getTarget() == null) m.setTarget(p);
    }

    public void physical(LivingEntity t, float amount) {
        t.hurt(level.damageSources().playerAttack(p), amount);
    }

    public void say(String s) { p.displayClientMessage(Component.literal(s), true); }

    public void particles(ParticleOptions o, Vec3 pos, int n, double spread, double speed) {
        level.sendParticles(o, pos.x, pos.y, pos.z, n, spread, spread, spread, speed);
    }

    public void beam(Vec3 from, Vec3 to, ParticleOptions o) {
        Vec3 delta = to.subtract(from);
        double len = delta.length();
        if (len < 0.1) return;
        Vec3 step = delta.normalize().scale(0.6);
        Vec3 cur = from;
        for (double i = 0; i < len; i += 0.6) {
            level.sendParticles(o, cur.x, cur.y, cur.z, 1, 0.02, 0.02, 0.02, 0.0);
            cur = cur.add(step);
        }
    }

    public void sound(SoundEvent s, float pitch) {
        level.playSound(null, p.getX(), p.getY(), p.getZ(), s, SoundSource.PLAYERS, 1.0f, pitch);
    }
}
