package com.konorpg.skill;

import com.konorpg.Config;
import com.konorpg.data.PlayerRpgData;
import com.konorpg.rpg.RpgManager;
import com.konorpg.rpg.RpgStats;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;

public final class SkillExecutor {
    public static void cast(ServerPlayer p, String id) {
        PlayerRpgData d = RpgManager.data(p);
        Skill s = Skills.get(id);
        if (s == null || s.action() == null || !d.skills.contains(id)) return;
        if (!Skills.usable(d, s)) {
            p.displayClientMessage(Component.literal("Sua classe atual não pode usar " + s.name() + "."), true);
            return;
        }
        long gt = p.level().getGameTime();
        if (gt < d.incapacitatedUntil) {
            p.displayClientMessage(Component.literal("Você está incapacitado!"), true);
            return;
        }
        long cd = d.cooldowns.getOrDefault(id, 0L);
        if (cd > gt) {
            p.displayClientMessage(Component.literal(s.name() + " recarregando: " + ((cd - gt) / 20 + 1) + "s"), true);
            return;
        }
        if (d.mana < s.mana()) {
            p.displayClientMessage(Component.literal("Mana insuficiente (" + s.mana() + ")"), true);
            return;
        }
        if (!s.action().cast(new SkillCtx(p, d))) return;
        d.mana = Math.max(0, d.mana - s.mana());
        d.cooldowns.put(id, gt + (long) (s.cooldown() * Config.COOLDOWN_MULT.get()));
        RpgManager.sync(p);
    }

    /** Basic staff bolt. Returns true if it was cast. */
    public static boolean castStaff(ServerPlayer p) {
        PlayerRpgData d = RpgManager.data(p);
        if (p.level().getGameTime() < d.incapacitatedUntil) return false;
        if (d.mana < 4) {
            p.displayClientMessage(Component.literal("Mana insuficiente"), true);
            return false;
        }
        SkillCtx c = new SkillCtx(p, d);
        SkillCtx.Hit h = c.ray(24);
        Vec3 from = p.getEyePosition().add(p.getLookAngle().scale(0.8)).add(0, -0.2, 0);
        c.beam(from, h.end(), ParticleTypes.END_ROD);
        if (h.target() != null) c.magic(h.target(), (float) (3 + 2.5 * c.spell()));
        c.sound(SoundEvents.EVOKER_CAST_SPELL, 1.4f);
        d.mana -= 4;
        RpgManager.sync(p);
        return true;
    }

    private SkillExecutor() {}
}
