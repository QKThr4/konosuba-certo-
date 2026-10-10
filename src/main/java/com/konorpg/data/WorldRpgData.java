package com.konorpg.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

/** World-wide persistent state: city/dungeon locations and player-created guilds. */
public class WorldRpgData extends SavedData {
    public static final String NAME = "konorpg_world";

    public static class GuildInfo {
        public String name = "";
        public UUID leader;
        public final Map<UUID, String> members = new LinkedHashMap<>();
    }

    public boolean cityBuilt, dungeonBuilt;
    public BlockPos city, guildHall, board, gate, dungeon, bossRoom;
    public int cityY;
    public long lastBossAt;
    public final Map<String, GuildInfo> guilds = new LinkedHashMap<>();

    public static WorldRpgData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(WorldRpgData::new, WorldRpgData::load, null), NAME);
    }

    private static void putPos(CompoundTag t, String k, BlockPos p) {
        if (p != null) t.putLong(k, p.asLong());
    }

    private static BlockPos getPos(CompoundTag t, String k) {
        return t.contains(k) ? BlockPos.of(t.getLong(k)) : null;
    }

    public static WorldRpgData load(CompoundTag t, HolderLookup.Provider prov) {
        WorldRpgData d = new WorldRpgData();
        d.cityBuilt = t.getBoolean("cityBuilt");
        d.dungeonBuilt = t.getBoolean("dungeonBuilt");
        d.city = getPos(t, "city");
        d.guildHall = getPos(t, "guildHall");
        d.board = getPos(t, "board");
        d.gate = getPos(t, "gate");
        d.dungeon = getPos(t, "dungeon");
        d.bossRoom = getPos(t, "bossRoom");
        d.cityY = t.getInt("cityY");
        d.lastBossAt = t.getLong("lastBossAt");
        ListTag gl = t.getList("guilds", Tag.TAG_COMPOUND);
        for (int i = 0; i < gl.size(); i++) {
            CompoundTag g = gl.getCompound(i);
            GuildInfo gi = new GuildInfo();
            gi.name = g.getString("name");
            if (g.hasUUID("leader")) gi.leader = g.getUUID("leader");
            ListTag ml = g.getList("members", Tag.TAG_COMPOUND);
            for (int j = 0; j < ml.size(); j++) {
                CompoundTag m = ml.getCompound(j);
                if (m.hasUUID("id")) gi.members.put(m.getUUID("id"), m.getString("n"));
            }
            d.guilds.put(gi.name, gi);
        }
        return d;
    }

    @Override
    public CompoundTag save(CompoundTag t, HolderLookup.Provider prov) {
        t.putBoolean("cityBuilt", cityBuilt);
        t.putBoolean("dungeonBuilt", dungeonBuilt);
        putPos(t, "city", city);
        putPos(t, "guildHall", guildHall);
        putPos(t, "board", board);
        putPos(t, "gate", gate);
        putPos(t, "dungeon", dungeon);
        putPos(t, "bossRoom", bossRoom);
        t.putInt("cityY", cityY);
        t.putLong("lastBossAt", lastBossAt);
        ListTag gl = new ListTag();
        for (GuildInfo gi : guilds.values()) {
            CompoundTag g = new CompoundTag();
            g.putString("name", gi.name);
            if (gi.leader != null) g.putUUID("leader", gi.leader);
            ListTag ml = new ListTag();
            gi.members.forEach((id, n) -> {
                CompoundTag m = new CompoundTag();
                m.putUUID("id", id);
                m.putString("n", n);
                ml.add(m);
            });
            g.put("members", ml);
            gl.add(g);
        }
        t.put("guilds", gl);
        return t;
    }

    public List<GuildInfo> guildList() { return new ArrayList<>(guilds.values()); }
}
