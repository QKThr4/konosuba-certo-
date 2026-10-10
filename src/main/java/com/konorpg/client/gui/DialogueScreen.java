package com.konorpg.client.gui;

import com.konorpg.client.ClientRpgData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;

public class DialogueScreen extends Screen {
    private CompoundTag tag;

    public DialogueScreen(CompoundTag tag) {
        super(Component.literal("Diálogo"));
        this.tag = tag;
    }

    public void update(CompoundTag t) { this.tag = t; rebuildWidgets(); }

    @Override
    protected void init() {
        ListTag ch = tag.getList("choices", Tag.TAG_STRING);
        int w = Math.min(width - 20, 340), x = (width - w) / 2;
        int y = height - 14 - ch.size() * 22;
        for (int i = 0; i < ch.size(); i++) {
            final int idx = i;
            addRenderableWidget(Button.builder(Component.literal(ch.getString(i)), b -> {
                CompoundTag t = new CompoundTag();
                t.putInt("i", idx);
                ClientRpgData.send("dlg_choice", t);
            }).bounds(x, y + i * 22, w, 20).build());
        }
    }

    @Override public void renderBackground(GuiGraphics g, int mx, int my, float pt) { }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        int w = Math.min(width - 20, 340), x = (width - w) / 2;
        int n = tag.getList("choices", Tag.TAG_STRING).size();
        int boxH = 90, top = height - 14 - n * 22 - boxH - 6;
        Ui.panel(g, x, top, w, boxH);
        Ui.text(g, tag.getString("speaker"), x + 8, top + 7, Ui.GOLD);
        int ly = top + 22;
        for (var seq : font.split(Component.literal(tag.getString("text")), w - 16)) {
            g.drawString(font, seq, x + 8, ly, Ui.TEXT, false);
            ly += 10;
        }
        super.render(g, mx, my, pt);
    }

    @Override public boolean isPauseScreen() { return false; }

    @Override
    public void onClose() {
        ClientRpgData.send("dlg_close");
        super.onClose();
    }
}
