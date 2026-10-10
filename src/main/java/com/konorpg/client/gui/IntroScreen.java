package com.konorpg.client.gui;

import com.konorpg.client.ClientRpgData;
import com.konorpg.rpg.Perks;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

/** The "Isekai choice": pick ONE permanent gift to take to the other world. */
public class IntroScreen extends Screen {
    private String selected = "";

    public IntroScreen() { super(Component.literal("A Escolha")); }

    @Override
    protected void init() {
        int y = 62, w = Math.min(width - 20, 320), x = (width - w) / 2;
        List<Perks.Perk> list = new ArrayList<>(Perks.ALL.values());
        for (Perks.Perk p : list) {
            addRenderableWidget(Button.builder(Component.literal((selected.equals(p.id()) ? "> " : "") + p.name()), b -> { selected = p.id(); rebuildWidgets(); })
                    .bounds(x, y, w, 18).build());
            y += 20;
        }
        Button ok = Button.builder(Component.literal("CONFIRMAR (permanente)"), b -> {
            CompoundTag t = new CompoundTag();
            t.putString("id", selected);
            ClientRpgData.send("choose_perk", t);
            onClose();
        }).bounds(x, y + 8, w, 20).build();
        ok.active = !selected.isEmpty();
        addRenderableWidget(ok);
    }

    @Override public void renderBackground(GuiGraphics g, int mx, int my, float pt) { }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        g.fill(0, 0, width, height, 0xF0100A18);
        Ui.text(g, "Você morreu de forma... absurda.", width / 2 - 80, 10, Ui.GOLD);
        Ui.text(g, "Uma deusa oferece levar UM presente para outro mundo. Escolha com sabedoria.", Math.max(6, width / 2 - 170), 24, Ui.TEXT);
        Ui.text(g, "(A escolha é permanente.)", width / 2 - 55, 36, Ui.DIM);
        super.render(g, mx, my, pt);
        if (!selected.isEmpty()) {
            Perks.Perk p = Perks.ALL.get(selected);
            int bx = Math.max(6, (width - Math.min(width - 20, 320)) / 2);
            int ly = height - 52;
            for (var seq : font.split(Component.literal(p.desc()), Math.min(width - 20, 320))) {
                g.drawString(font, seq, bx, ly, Ui.TEXT, false);
                ly += 10;
            }
        }
    }

    @Override public boolean shouldCloseOnEsc() { return false; }
}
