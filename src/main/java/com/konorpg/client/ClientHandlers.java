package com.konorpg.client;

import com.konorpg.network.OpenScreenPayload;
import com.konorpg.network.SyncRpgPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ClientHandlers {
    public static void onSync(SyncRpgPayload p, IPayloadContext ctx) {
        ctx.enqueueWork(() -> ClientRpgData.apply(p.tag(), ctx.player().registryAccess()));
    }

    public static void onOpen(OpenScreenPayload p, IPayloadContext ctx) {
        ctx.enqueueWork(() -> ClientScreens.open(p.screen(), p.tag()));
    }

    private ClientHandlers() {}
}
