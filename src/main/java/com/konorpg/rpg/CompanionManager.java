package com.konorpg.rpg;

import com.konorpg.Config;
import com.konorpg.data.CompanionRecord;
import com.konorpg.data.PlayerRpgData;
import com.konorpg.entity.CompanionEntity;
import com.konorpg.registry.ModEntities;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

/** Materializes companions from player data next to their owner and handles companion events. */
public final class CompanionManager {
    private static final Map<String, UUID> ENTITIES = new HashMap<>();

    public static void tick(ServerPlayer p, PlayerRpgData d) {
        if (!Config.COMPANIONS_ENABLED.get() || !d.registered) return;
        if (d.flags.remove("aqua_pending")) {
            CompanionRecord r = new CompanionRecord();
            r.kind = "aqua";
            r.name = "Aqua";
            d.companions.add(r);
            RpgManager.msg(p, "Aqua, a deusa, agora é sua companheira! (Fale com ela clicando com o botão direito.)");
        }
        ServerLevel level = p.serverLevel();
        long gt = level.getGameTime();
        for (CompanionRecord rec : d.companions) {
            if (gt < rec.reviveAt) continue;
            UUID eid = ENTITIES.get(rec.uid);
            Entity e = eid == null ? null : level.getEntity(eid);
            if (e instanceof CompanionEntity ce && ce.isAlive() && ce.distanceToSqr(p) < 128 * 128) {
                ce.refresh(rec);
                continue;
            }
            discard(p.server, eid);
            CompanionEntity ce = ModEntities.COMPANION.get().create(level);
            if (ce == null) continue;
            ce.moveTo(p.getX() + 1.5, p.getY(), p.getZ() + 1.5, p.getYRot(), 0);
            ce.setup(rec, p);
            level.addFreshEntity(ce);
            ENTITIES.put(rec.uid, ce.getUUID());
            if (rec.kind.equals("aqua") && !d.flags.contains("aqua_welcomed")) {
                d.flags.add("aqua_welcomed");
                ce.speak("welcome");
            }
        }
    }

    private static void discard(MinecraftServer server, UUID id) {
        if (id == null) return;
        for (ServerLevel l : server.getAllLevels()) {
            Entity e = l.getEntity(id);
            if (e != null) e.discard();
        }
    }

    public static void onLogout(ServerPlayer p) {
        PlayerRpgData d = RpgManager.data(p);
        for (CompanionRecord r : d.companions) {
            discard(p.server, ENTITIES.remove(r.uid));
        }
    }

    public static void clearAll() { ENTITIES.clear(); }

    private static CompanionEntity active(ServerPlayer p, CompanionRecord r) {
        UUID id = ENTITIES.get(r.uid);
        Entity e = id == null ? null : p.serverLevel().getEntity(id);
        return e instanceof CompanionEntity ce && ce.isAlive() ? ce : null;
    }

    /** Companion comments + small friendship gains on gameplay events. */
    public static void react(ServerPlayer p, String trigger) {
        PlayerRpgData d = RpgManager.data(p);
        for (CompanionRecord r : d.companions) {
            CompanionEntity ce = active(p, r);
            if (ce == null) continue;
            if (trigger.equals("quest_done") || trigger.equals("rank") || trigger.equals("levelup")) r.friendship += 1;
            if (p.getRandom().nextInt(trigger.equals("kill") ? 6 : 1) == 0) ce.speak(trigger);
        }
    }

    public static void addFriendship(ServerPlayer p, String kind, int amount) {
        RpgManager.data(p).addFriend(kind, amount);
    }

    public static CompanionEntity entityOf(ServerPlayer p, String kind) {
        CompanionRecord r = RpgManager.data(p).companion(kind);
        return r == null ? null : active(p, r);
    }

    /** Aqua's resurrection. Returns true when the death was cancelled. */
    public static boolean tryResurrect(ServerPlayer p) {
        PlayerRpgData d = RpgManager.data(p);
        CompanionRecord r = d.companion("aqua");
        if (r == null) return false;
        CompanionEntity ce = active(p, r);
        if (ce == null || ce.distanceToSqr(p) > 40 * 40) return false;
        long gt = p.level().getGameTime();
        if (gt < r.abilityCdUntil) { ce.speak("resurrect_fail"); return false; }
        r.abilityCdUntil = gt + 6000;
        double chance = 0.70 + 0.05 * Friendship.level(r.friendship);
        if (p.getRandom().nextDouble() > chance) { ce.speak("resurrect_fail"); return false; }
        ce.speak("resurrect");
        return true;
    }

    private CompanionManager() {}
}
