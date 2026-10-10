package com.konorpg.network;

import com.konorpg.KonoRPG;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server -> client: open (or update) a screen with arbitrary data. */
public record OpenScreenPayload(String screen, CompoundTag tag) implements CustomPacketPayload {
    public static final Type<OpenScreenPayload> TYPE = new Type<>(KonoRPG.id("open_screen"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenScreenPayload> CODEC =
            StreamCodec.composite(ByteBufCodecs.STRING_UTF8, OpenScreenPayload::screen,
                    ByteBufCodecs.COMPOUND_TAG, OpenScreenPayload::tag, OpenScreenPayload::new);

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
