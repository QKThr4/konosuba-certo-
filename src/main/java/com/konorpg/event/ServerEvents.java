package com.konorpg.event;

import com.konorpg.Config;
import com.konorpg.KonoRPG;
import com.konorpg.data.CompanionRecord;
import com.konorpg.data.PlayerRpgData;
import com.konorpg.data.WorldRpgData;
import com.konorpg.dialogue.DialogueManager;
import com.konorpg.entity.CompanionEntity;
import com.konorpg.network.Net;
import com.konorpg.quest.QuestManager;
import com.konorpg.rpg.CompanionManager;
import com.konorpg.rpg.RpgManager;
import com.konorpg.rpg.RpgStats;
import com.konorpg.shop.EconomyManager;
import com.konorpg.shop.ShopManager;
import com.konorpg.util.Scheduler;
import com.konorpg.world.CityBuilder;
import com.konorpg.world.CityInfo;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Gameplay events on the server (game bus). */
@EventBusSubscriber(modid = KonoRPG.MOD_ID)
public final class ServerEvents {
    @SubscribeEvent
    public static void reload(AddReloadListenerEvent e) {
        e.addListener(QuestManager.INSTANCE);
        e.addListener(DialogueManager.INSTANCE);
        e.addListener(ShopManager.INSTANCE);
        e.addListener(EconomyManager.INSTANCE);
    }

    @SubscribeEvent
    public static void started(ServerStartedEvent e) {
        WorldRpgData w = WorldRpgData.get(e.getServer());
        CityInfo.set(w.city);
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent e) {
        CityInfo.set(null);
        Scheduler.clear();
        CompanionManager.clearAll();
        DialogueManager.clearSessions();
    }

    @SubscribeEvent
    public static void serverTick(ServerTickEvent.Post e) { Scheduler.tick(); }

    @SubscribeEvent
    public static void login(PlayerEvent.PlayerLoggedInEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer p)) return;
        PlayerRpgData d = RpgManager.data(p);
        RpgManager.refreshAttributes(p);
        RpgManager.sync(p);
        if (!d.introDone && Config.AUTO_INTRO.get()) {
            Scheduler.later(80, () -> { if (!p.hasDisconnected() && !RpgManager.data(p).introDone) Net.openScreen(p, "intro", new net.minecraft.nbt.CompoundTag()); });
        }
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent e) {
        if (e.getEntity() instanceof ServerPlayer p) {
            CompanionManager.onLogout(p);
            DialogueManager.forget(p.getUUID());
            ShopManager.close(p);
        }
    }

    @SubscribeEvent
    public static void respawn(PlayerEvent.PlayerRespawnEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer p)) return;
        PlayerRpgData d = RpgManager.data(p);
        RpgManager.refreshAttributes(p);
        if (d.registered && p.getRespawnPosition() == null && WorldRpgData.get(p.server).cityBuilt) {
            CityBuilder.teleportToPlaza(p);
            RpgManager.msg(p, "Você acordou na praça de Axel. (Eris ajudou, de novo.)");
        }
        RpgManager.sync(p);
    }

    @SubscribeEvent
    public static void dimension(PlayerEvent.PlayerChangedDimensionEvent e) {
        if (e.getEntity() instanceof ServerPlayer p) { RpgManager.refreshAttributes(p); RpgManager.sync(p); }
    }

    @SubscribeEvent
    public static void playerTick(PlayerTickEvent.Post e) {
        if (!(e.getEntity() instanceof ServerPlayer p)) return;
        PlayerRpgData d = RpgManager.data(p);
        if (p.tickCount % 20 == 0) {
            double max = RpgStats.maxMana(p, d);
            d.mana = Math.min(max, d.mana + RpgStats.manaRegen(p, d));
            QuestManager.tickPlayer(p, d);
        }
        if (p.tickCount % 40 == 0) CompanionManager.tick(p, d);
        if (p.tickCount % 100 == 0) { RpgManager.refreshAttributes(p); RpgManager.sync(p); }
    }

    @SubscribeEvent
    public static void attack(AttackEntityEvent e) {
        if (e.getEntity() instanceof ServerPlayer p && p.level().getGameTime() < RpgManager.data(p).incapacitatedUntil) e.setCanceled(true);
    }

    @SubscribeEvent
    public static void rightClick(PlayerInteractEvent.RightClickItem e) {
        if (e.getEntity() instanceof ServerPlayer p && p.level().getGameTime() < RpgManager.data(p).incapacitatedUntil) e.setCanceled(true);
    }

    /** Critical hits (LUCK/DEX) on melee damage. Magic and skill damage are not modified here. */
    @SubscribeEvent
    public static void damage(LivingIncomingDamageEvent e) {
        if (e.getSource().getEntity() instanceof ServerPlayer p && e.getSource().getDirectEntity() == p
                && !e.getSource().is(DamageTypes.INDIRECT_MAGIC) && !(e.getEntity() instanceof com.konorpg.entity.TownNpcEntity)) {
            PlayerRpgData d = RpgManager.data(p);
            if (p.getRandom().nextDouble() < RpgStats.critChance(p, d)) {
                e.setAmount(e.getAmount() * 1.6f);
                p.displayClientMessage(net.minecraft.network.chat.Component.literal("CRÍTICO!"), true);
            }
        }
    }

    @SubscribeEvent
    public static void death(LivingDeathEvent e) {
        LivingEntity dead = e.getEntity();
        if (dead.level().isClientSide) return;
        if (dead instanceof ServerPlayer p) {
            if (CompanionManager.tryResurrect(p)) {
                e.setCanceled(true);
                p.setHealth(p.getMaxHealth() * 0.5f);
                p.removeAllEffects();
                p.invulnerableTime = 60;
                RpgManager.title(p, "RESSUSCITADO!", "Aqua trouxe você de volta");
                return;
            }
            PlayerRpgData d = RpgManager.data(p);
            d.deaths++;
            long loss = d.money * Config.DEATH_MONEY_LOSS_PERCENT.get() / 100;
            if (loss > 0) {
                d.money -= loss;
                RpgManager.msg(p, "Você perdeu " + loss + " Eris ao morrer...");
            }
            return;
        }
        Entity src = e.getSource().getEntity();
        ServerPlayer killer = null;
        if (src instanceof ServerPlayer sp) killer = sp;
        else if (src instanceof CompanionEntity ce) killer = ce.owner();
        if (killer == null || !(dead instanceof Enemy)) return;
        PlayerRpgData d = RpgManager.data(killer);
        if (!d.registered) return;
        d.monstersKilled++;
        double hp = dead.getMaxHealth();
        long xp = Math.max(3, Math.round(hp * 0.9));
        long money = Math.max(2, Math.round(hp * 0.8));
        RpgManager.grantXp(killer, xp);
        RpgManager.grantMoney(killer, money);
        killer.displayClientMessage(net.minecraft.network.chat.Component.literal("+" + xp + " XP  +" + money + " Eris"), true);
        QuestManager.onKill(killer, dead.getType());
        CompanionManager.react(killer, "kill");
    }

    private ServerEvents() {}
}
