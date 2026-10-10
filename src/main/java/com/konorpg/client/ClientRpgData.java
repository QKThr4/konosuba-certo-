package com.konorpg.client;

import com.konorpg.data.PlayerRpgData;
import com.konorpg.network.ActionPayload;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.network.PacketDistributor;

/** Client-side mirror of the player's RPG data (read-only; all changes go through server actions). */
public final class ClientRpgData {
    public static final PlayerRpgData DATA = new PlayerRpgData();
    public static final Map<String, CompoundTag> QINFO = new HashMap<>();
    public static int mainTotal = 10;
    public static long syncGameTime;
    public static long syncRealMs = System.currentTimeMillis();

    public static void apply(CompoundTag t, HolderLookup.Provider prov) {
        DATA.deserializeNBT(prov, t);
        mainTotal = t.getInt("mainTotal");
        syncGameTime = t.getLong("gt");
        syncRealMs = System.currentTimeMillis();
        QINFO.clear();
        ListTag l = t.getList("qinfo", Tag.TAG_COMPOUND);
        for (int i = 0; i < l.size(); i++) QINFO.put(l.getCompound(i).getString("id"), l.getCompound(i));
    }

    public static void send(String action) { send(action, new CompoundTag()); }

    public static void send(String action, CompoundTag args) {
        PacketDistributor.sendToServer(new ActionPayload(action, args));
    }

    private ClientRpgData() {}
}
