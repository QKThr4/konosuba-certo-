package com.konorpg.client;

import com.konorpg.Config;
import com.konorpg.KonoRPG;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = KonoRPG.MOD_ID, value = Dist.CLIENT)
public final class ClientEvents {
    @SubscribeEvent
    public static void tick(ClientTickEvent.Post e) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;
        while (ClientSetup.HUB.consumeClick()) {
            CompoundTag t = new CompoundTag();
            t.putString("tab", "status");
            ClientRpgData.send("open_hub", t);
        }
        while (ClientSetup.HUD.consumeClick()) {
            Config.HUD_PROGRESS.set(!Config.HUD_PROGRESS.get());
            Config.CLIENT_SPEC.save();
        }
        for (int i = 0; i < 4; i++) {
            while (ClientSetup.SKILLS[i].consumeClick()) {
                CompoundTag t = new CompoundTag();
                t.putInt("slot", i);
                ClientRpgData.send("skill_cast", t);
            }
        }
    }

    private ClientEvents() {}
}
