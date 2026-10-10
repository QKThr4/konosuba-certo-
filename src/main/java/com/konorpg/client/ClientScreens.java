package com.konorpg.client;

import com.konorpg.client.gui.BoardScreen;
import com.konorpg.client.gui.DialogueScreen;
import com.konorpg.client.gui.IntroScreen;
import com.konorpg.client.gui.RpgHubScreen;
import com.konorpg.client.gui.ShopScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;

public final class ClientScreens {
    public static void open(String screen, CompoundTag tag) {
        Minecraft mc = Minecraft.getInstance();
        Screen cur = mc.screen;
        switch (screen) {
            case "intro" -> mc.setScreen(new IntroScreen());
            case "hub" -> {
                String tab = tag.getString("tab");
                if (cur instanceof RpgHubScreen h) h.switchTab(tab);
                else mc.setScreen(new RpgHubScreen(tab));
            }
            case "board" -> {
                if (cur instanceof BoardScreen b) b.update(tag);
                else mc.setScreen(new BoardScreen(tag));
            }
            case "dialogue" -> {
                if (cur instanceof DialogueScreen d) d.update(tag);
                else mc.setScreen(new DialogueScreen(tag));
            }
            case "shop" -> {
                if (cur instanceof ShopScreen s) s.update(tag);
                else mc.setScreen(new ShopScreen(tag));
            }
            case "close" -> {
                if (cur instanceof DialogueScreen || cur instanceof ShopScreen || cur instanceof BoardScreen) mc.setScreen(null);
            }
            default -> { }
        }
    }

    private ClientScreens() {}
}
