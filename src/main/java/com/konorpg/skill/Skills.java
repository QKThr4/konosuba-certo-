package com.konorpg.skill;

import com.konorpg.Config;
import com.konorpg.data.PlayerRpgData;
import com.konorpg.rpg.RpgClasses;
import com.konorpg.rpg.RpgManager;
import com.konorpg.rpg.RpgStats;
import com.konorpg.rpg.Stat;
import com.konorpg.util.Scheduler;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Skill registry. New skills can be added with active()/passive(). Entries marked EM BREVE are planned. */
public final class Skills {
    private static final Map<String, Skill> ALL = new LinkedHashMap<>();

    public static Skill get(String id) { return ALL.get(id); }

    public static Collection<Skill> all() { return ALL.values(); }

    public static boolean usable(PlayerRpgData d, Skill s) {
        return d.flags.contains("perk_skill:" + s.id()) || RpgClasses.lines(d.classId).contains(s.group());
    }

    public static boolean canLearn(PlayerRpgData d, Skill s) {
        if (d.skills.contains(s.id())) return false;
        if (d.level < s.minLevel() || d.skillPoints < s.spCost()) return false;
        if (!RpgClasses.lines(d.classId).contains(s.group())) return false;
        return s.parent() == null || s.parent().isEmpty() || d.skills.contains(s.parent());
    }

    private static void reg(Skill s) { ALL.put(s.id(), s); }

    private static void active(String id, String name, String desc, String group, int lvl, int sp, String parent,
                               int mana, int cd, Skill.Action a) {
        reg(new Skill(id, name, desc, group, Skill.Type.ACTIVE, lvl, sp, parent, mana, cd, Map.of(), a));
    }

    private static void passive(String id, String name, String desc, String group, int lvl, int sp, Map<Stat, Integer> b) {
        reg(new Skill(id, name, desc, group, Skill.Type.PASSIVE, lvl, sp, "", 0, 0, b, null));
    }

    private static void effect(LivingEntity e, net.minecraft.core.Holder<MobEffect> eff, int ticks, int amp) {
        e.addEffect(new MobEffectInstance(eff, ticks, amp));
    }

    static {
        // ---- general ----
        active("create_water", "Create Water", "A habilidade mais inútil do mundo. Molha o alvo (lentidão) ou apaga o fogo.", "general", 1, 1, null, 2, 20, c -> {
            SkillCtx.Hit h = c.ray(6);
            if (h.target() != null) {
                effect(h.target(), MobEffects.MOVEMENT_SLOWDOWN, 60, 0);
                h.target().clearFire();
                c.particles(ParticleTypes.SPLASH, h.target().position().add(0, 1, 0), 25, 0.4, 0.1);
            } else {
                c.p.clearFire();
                c.particles(ParticleTypes.SPLASH, c.p.position().add(0, 1, 0), 25, 0.4, 0.1);
            }
            c.sound(SoundEvents.BUCKET_EMPTY, 1.2f);
            return true;
        });
        // ---- warrior ----
        active("power_strike", "Power Strike", "Golpe pesado corpo a corpo.", "warrior", 1, 1, null, 6, 80, c -> {
            SkillCtx.Hit h = c.ray(4.5);
            if (h.target() == null) { c.say("Nenhum alvo ao alcance."); return false; }
            c.physical(h.target(), (float) (6 + 4 * c.phys()));
            Vec3 l = c.p.getLookAngle();
            h.target().knockback(0.9, -l.x, -l.z);
            c.particles(ParticleTypes.CRIT, h.target().position().add(0, 1, 0), 15, 0.3, 0.2);
            return true;
        });
        active("shield_bash", "Shield Bash", "Atordoa o alvo (lentidão e fraqueza).", "warrior", 4, 2, null, 8, 160, c -> {
            SkillCtx.Hit h = c.ray(4);
            if (h.target() == null) { c.say("Nenhum alvo ao alcance."); return false; }
            c.physical(h.target(), (float) (3 + 2 * c.phys()));
            effect(h.target(), MobEffects.MOVEMENT_SLOWDOWN, 80, 3);
            effect(h.target(), MobEffects.WEAKNESS, 100, 1);
            return true;
        });
        active("berserk", "Berserk", "Ultimate: força e velocidade por 15s, depois exaustão.", "warrior", 10, 4, "power_strike", 25, 1200, c -> {
            effect(c.p, MobEffects.DAMAGE_BOOST, 300, 1);
            effect(c.p, MobEffects.MOVEMENT_SPEED, 300, 0);
            effect(c.p, MobEffects.DAMAGE_RESISTANCE, 300, 0);
            c.sound(SoundEvents.RAVAGER_ROAR, 1.0f);
            ServerPlayer p = c.p;
            Scheduler.later(300, () -> { if (p.isAlive()) { effect(p, MobEffects.MOVEMENT_SLOWDOWN, 120, 1); p.displayClientMessage(Component.literal("Exaustão pós-Berserk..."), true); } });
            return true;
        });
        passive("toughness", "Toughness", "+3 VIT.", "warrior", 3, 2, Map.of(Stat.VIT, 3));
        // ---- knight ----
        active("guard_stance", "Guard Stance", "Postura defensiva: resistência e absorção.", "knight", 1, 1, null, 10, 300, c -> {
            effect(c.p, MobEffects.DAMAGE_RESISTANCE, 160, 1);
            effect(c.p, MobEffects.ABSORPTION, 160, 1);
            return true;
        });
        // ---- mage ----
        active("fireball", "Fireball", "Bola de fogo em linha reta com dano em área.", "mage", 1, 1, null, 10, 40, c -> {
            SkillCtx.Hit h = c.ray(24);
            Vec3 from = c.p.getEyePosition().add(c.p.getLookAngle().scale(0.8));
            c.beam(from, h.end(), ParticleTypes.FLAME);
            for (LivingEntity e : c.foesNear(h.end(), 2.5)) { c.magic(e, (float) (5 + 4 * c.spell())); e.igniteForSeconds(4); }
            c.particles(ParticleTypes.EXPLOSION, h.end(), 3, 0.5, 0);
            c.sound(SoundEvents.BLAZE_SHOOT, 1.0f);
            return true;
        });
        active("ice_lance", "Ice Lance", "Lança de gelo que congela e desacelera.", "mage", 3, 2, null, 12, 50, c -> {
            SkillCtx.Hit h = c.ray(26);
            Vec3 from = c.p.getEyePosition().add(c.p.getLookAngle().scale(0.8));
            c.beam(from, h.end(), ParticleTypes.SNOWFLAKE);
            if (h.target() != null) {
                c.magic(h.target(), (float) (5 + 3 * c.spell()));
                effect(h.target(), MobEffects.MOVEMENT_SLOWDOWN, 80, 2);
                h.target().setTicksFrozen(140);
            }
            c.sound(SoundEvents.GLASS_BREAK, 1.3f);
            return true;
        });
        active("lightning", "Lightning", "Raio que cai sobre os inimigos.", "mage", 6, 3, "fireball", 18, 100, c -> {
            SkillCtx.Hit h = c.ray(28);
            net.minecraft.world.entity.LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(c.level);
            if (bolt != null) { bolt.moveTo(h.end()); bolt.setVisualOnly(true); c.level.addFreshEntity(bolt); }
            for (LivingEntity e : c.foesNear(h.end(), 3.5)) c.magic(e, (float) (10 + 5 * c.spell()));
            return true;
        });
        active("barrier", "Barrier", "Barreira mágica: absorção e resistência.", "mage", 5, 2, null, 15, 400, c -> {
            effect(c.p, MobEffects.ABSORPTION, 200, 2);
            effect(c.p, MobEffects.DAMAGE_RESISTANCE, 100, 1);
            c.particles(ParticleTypes.ENCHANT, c.p.position().add(0, 1, 0), 40, 0.6, 0.5);
            return true;
        });
        passive("mana_focus", "Mana Focus", "+4 MAG.", "mage", 4, 2, Map.of(Stat.MAG, 4));
        // ---- arch wizard ----
        reg(new Skill("explosion", "EXPLOSION!", "Consome TODA a mana (mín. 45). Dano e área enormes. Você fica incapacitado depois.",
                "archwizard", Skill.Type.ULTIMATE, 15, 8, "", 0, 6000, Map.of(), Skills::explosion));
        // ---- priest ----
        active("heal", "Heal", "Cura você e aliados próximos.", "priest", 1, 1, null, 10, 60, c -> {
            float amt = (float) (6 + 3 * c.spell());
            for (LivingEntity e : c.allies(6)) { e.heal(amt); c.particles(ParticleTypes.HEART, e.position().add(0, 1.5, 0), 5, 0.3, 0); }
            c.sound(SoundEvents.PLAYER_LEVELUP, 1.6f);
            return true;
        });
        active("blessing", "Blessing", "Regeneração e força para aliados.", "priest", 4, 2, "heal", 20, 400, c -> {
            for (LivingEntity e : c.allies(8)) { effect(e, MobEffects.REGENERATION, 400, 0); effect(e, MobEffects.DAMAGE_BOOST, 400, 0); }
            return true;
        });
        active("purification", "Purification", "Remove efeitos ruins e fere mortos-vivos.", "priest", 6, 3, "heal", 18, 120, c -> {
            for (LivingEntity e : c.allies(6)) {
                for (MobEffectInstance ei : List.copyOf(e.getActiveEffects()))
                    if (ei.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) e.removeEffect(ei.getEffect());
            }
            for (LivingEntity e : c.foesNear(c.p.position(), 8)) {
                if (e.getType().is(net.minecraft.tags.EntityTypeTags.UNDEAD)) c.magic(e, (float) (10 + 6 * c.spell()));
            }
            c.particles(ParticleTypes.END_ROD, c.p.position().add(0, 1, 0), 50, 1.5, 0.1);
            return true;
        });
        // ---- archer ----
        active("snipe", "Snipe", "Flecha precisa de longo alcance.", "archer", 1, 1, null, 5, 40, c -> {
            SkillCtx.Hit h = c.ray(40);
            Vec3 from = c.p.getEyePosition().add(c.p.getLookAngle().scale(0.8));
            c.beam(from, h.end(), ParticleTypes.CRIT);
            if (h.target() != null) c.physical(h.target(), (float) (6 + 3 * c.phys()));
            c.sound(SoundEvents.ARROW_SHOOT, 1.0f);
            return true;
        });
        // ---- thief ----
        active("steal", "Steal", "Tenta roubar Eris de um monstro. Depende da Sorte.", "thief", 2, 2, null, 8, 100, c -> {
            SkillCtx.Hit h = c.ray(5);
            if (h.target() == null) { c.say("Nenhum alvo na mira."); return false; }
            double chance = Math.min(0.85, 0.30 + RpgStats.eff(c.p, c.d, Stat.LUCK) * 0.015);
            if (c.p.getRandom().nextDouble() < chance) {
                long g = (long) ((20 + c.p.getRandom().nextInt(60)) * (1 + c.d.level / 8.0));
                RpgManager.addMoney(c.p, g);
                c.say("Roubou " + g + " Eris!");
            } else {
                if (h.target() instanceof Mob m) m.setTarget(c.p);
                c.say("Falhou! O monstro ficou furioso.");
            }
            return true;
        });
        active("stealth", "Stealth", "Fica invisível e veloz por 8s.", "thief", 4, 2, null, 12, 300, c -> {
            effect(c.p, MobEffects.INVISIBILITY, 160, 0);
            effect(c.p, MobEffects.MOVEMENT_SPEED, 160, 0);
            return true;
        });
        active("dash", "Dash", "Impulso rápido para frente.", "general", 1, 1, null, 6, 60, c -> {
            Vec3 l = c.p.getLookAngle();
            c.p.setDeltaMovement(l.x * 1.8, 0.25, l.z * 1.8);
            c.p.hurtMarked = true;
            return true;
        });
        // EM BREVE: Crusader (Decoy), Sword Master (Sword Dance), High Priest, Assassin, Necromancer, Battle Mage, Paladin skills.
    }

    private static boolean explosion(SkillCtx c) {
        if (c.d.mana < 45) { c.say("Mana insuficiente (mínimo 45)."); return false; }
        double spent = c.d.mana;
        c.d.mana = 0;
        ServerPlayer p = c.p;
        Vec3 target = c.ray(40).end();
        c.d.incapacitatedUntil = p.level().getGameTime() + 50 + 300;
        effect(p, MobEffects.MOVEMENT_SLOWDOWN, 50, 9);
        p.sendSystemMessage(Component.literal("Escuridão mais negra que o negro...").withStyle(net.minecraft.ChatFormatting.DARK_PURPLE));
        Scheduler.later(15, () -> p.sendSystemMessage(Component.literal("Carmesim que brilha no abismo...").withStyle(net.minecraft.ChatFormatting.DARK_RED)));
        Scheduler.later(30, () -> p.sendSystemMessage(Component.literal("Desperte, poder do colapso!").withStyle(net.minecraft.ChatFormatting.RED)));
        double radius = Math.min(18, 6 + spent / 12);
        double damage = (20 + spent * 0.9) * c.spell();
        Scheduler.later(50, () -> {
            if (!p.isAlive()) return;
            net.minecraft.server.level.ServerLevel lvl = p.serverLevel();
            p.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket(Component.literal("EXPLOSION!!!").withStyle(net.minecraft.ChatFormatting.RED)));
            lvl.playSound(null, target.x, target.y, target.z, SoundEvents.GENERIC_EXPLODE.value(), net.minecraft.sounds.SoundSource.PLAYERS, 4.0f, 0.6f);
            for (int i = 0; i < 6; i++) lvl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, target.x + (i - 3) * radius / 5, target.y + 1, target.z + (i % 2) * 2, 1, 0, 0, 0, 0);
            lvl.sendParticles(ParticleTypes.FLAME, target.x, target.y + 1, target.z, 200, radius / 3, 1.5, radius / 3, 0.1);
            for (LivingEntity e : lvl.getEntitiesOfClass(LivingEntity.class, new net.minecraft.world.phys.AABB(target, target).inflate(radius))) {
                if (!c.isFoe(e) || e.position().distanceTo(target) > radius) continue;
                double fall = 1.0 - 0.5 * (e.position().distanceTo(target) / radius);
                c.magic(e, (float) (damage * fall));
            }
            if (Config.EXPLOSION_BREAKS_BLOCKS.get()) lvl.explode(p, target.x, target.y, target.z, 5.0f, Level.ExplosionInteraction.TNT);
            effect(p, MobEffects.MOVEMENT_SLOWDOWN, 300, 9);
            effect(p, MobEffects.WEAKNESS, 300, 2);
            p.displayClientMessage(Component.literal("Você usou toda a sua mana... e não consegue se mexer."), true);
            RpgManager.sync(p);
        });
        return true;
    }

    private Skills() {}
}
