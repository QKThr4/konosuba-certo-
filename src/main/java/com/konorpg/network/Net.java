package com.konorpg.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public final class Net {
    public static void openScreen(ServerPlayer p, String screen, CompoundTag tag) {
        PacketDistributor.sendToPlayer(p, new OpenScreenPayload(screen, tag));
    }

    public static void openHub(ServerPlayer p, String tab) {
        CompoundTag t = new CompoundTag();
        t.putString("tab", tab);
        openScreen(p, "hub", t);
    }

    private Net() {}
}
