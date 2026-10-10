package com.konorpg.network;

import com.konorpg.KonoRPG;
import com.konorpg.client.ClientHandlers;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = KonoRPG.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class ModNetwork {
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar r = event.registrar("1");
        r.playToClient(SyncRpgPayload.TYPE, SyncRpgPayload.CODEC, ClientHandlers::onSync);
        r.playToClient(OpenScreenPayload.TYPE, OpenScreenPayload.CODEC, ClientHandlers::onOpen);
        r.playToServer(ActionPayload.TYPE, ActionPayload.CODEC, (payload, ctx) -> ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer sp) Actions.handle(sp, payload.action(), payload.args());
        }));
    }

    private ModNetwork() {}
}
