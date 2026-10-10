package com.konorpg.network;

import com.konorpg.KonoRPG;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SyncRpgPayload(CompoundTag tag) implements CustomPacketPayload {
    public static final Type<SyncRpgPayload> TYPE = new Type<>(KonoRPG.id("sync_rpg"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncRpgPayload> CODEC =
            StreamCodec.composite(ByteBufCodecs.COMPOUND_TAG, SyncRpgPayload::tag, SyncRpgPayload::new);

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
