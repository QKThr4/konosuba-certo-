package com.konorpg.network;

import com.konorpg.KonoRPG;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client -> server: a named action with arguments. Every action is validated server-side. */
public record ActionPayload(String action, CompoundTag args) implements CustomPacketPayload {
    public static final Type<ActionPayload> TYPE = new Type<>(KonoRPG.id("action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ActionPayload> CODEC =
            StreamCodec.composite(ByteBufCodecs.STRING_UTF8, ActionPayload::action,
                    ByteBufCodecs.COMPOUND_TAG, ActionPayload::args, ActionPayload::new);

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
