package com.konorpg.client.gui;

import com.konorpg.client.ClientRpgData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Buy / sell screen. EM BREVE: Craft and Services (repair / reinforce) tabs. */
public class ShopScreen extends Screen {
    private CompoundTag tag;
    private boolean sellTab;
    private int scroll;

    public ShopScreen(CompoundTag tag) { super(Component.literal("Loja")); this.tag = tag; }

    public void update(CompoundTag t) { tag = t; rebuildWidgets(); }

    private int px() { return (width - Math.min(width - 10, 360)) / 2; }

    @Override
    protected void init() {
        int pw = Math.min(width - 10, 360), x = px(), py = 10;
        addRenderableWidget(Button.builder(Component.literal("Comprar"), b -> { sellTab = false; scroll = 0; rebuildWidgets(); }).bounds(x + 8, py + 24, 70, 16).build());
        addRenderableWidget(Button.builder(Component.literal("Vender"), b -> { sellTab = true; scroll = 0; rebuildWidgets(); }).bounds(x + 82, py + 24, 70, 16).build());
        Button soon = Button.builder(Component.literal("Craft/Serviços: EM BREVE"), b -> { }).bounds(x + 156, py + 24, 130, 16).build();
        soon.active = false;
        addRenderableWidget(soon);
        addRenderableWidget(Button.builder(Component.literal("X"), b -> onClose()).bounds(x + pw - 24, py + 4, 18, 16).build());
        ListTag list = tag.getList(sellTab ? "sell" : "buy", Tag.TAG_COMPOUND);
        int rows = Math.max(1, (height - 90) / 22);
        for (int i = 0; i < rows && i + scroll < list.size(); i++) {
            CompoundTag c = list.getCompound(i + scroll);
            int y = py + 46 + i * 22;
            if (sellTab) {
                final int slot = c.getInt("slot");
                int unit = c.getInt("unit"), cnt = c.getInt("count");
                addRenderableWidget(Button.builder(Component.literal("1 (" + unit + ")"), b -> sell(slot, false)).bounds(x + pw - 150, y, 66, 18).build());
                addRenderableWidget(Button.builder(Component.literal("Tudo (" + unit * cnt + ")"), b -> sell(slot, true)).bounds(x + pw - 82, y, 74, 18).build());
            } else {
                final int idx = i + scroll;
                int price = c.getInt("price");
                addRenderableWidget(Button.builder(Component.literal("x1 " + price), b -> buy(idx, 1)).bounds(x + pw - 150, y, 66, 18).build());
                addRenderableWidget(Button.builder(Component.literal("x5 " + price * 5), b -> buy(idx, 5)).bounds(x + pw - 82, y, 74, 18).build());
            }
        }
    }

    private void buy(int idx, int qty) {
        CompoundTag t = new CompoundTag();
        t.putInt("idx", idx);
        t.putInt("qty", qty);
        ClientRpgData.send("shop_buy", t);
    }

    private void sell(int slot, boolean all) {
        CompoundTag t = new CompoundTag();
        t.putInt("slot", slot);
        t.putBoolean("all", all);
        ClientRpgData.send("shop_sell", t);
    }

    @Override public void renderBackground(GuiGraphics g, int mx, int my, float pt) { }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        int pw = Math.min(width - 10, 360), x = px();
        Ui.panel(g, x, 10, pw, height - 20);
        Ui.text(g, tag.getString("name"), x + 8, 14, Ui.GOLD);
        Ui.text(g, tag.getLong("money") + " Eris   (desconto: " + tag.getInt("discount") + "%)", x + pw - 150, 14, Ui.TEXT);
        ListTag list = tag.getList(sellTab ? "sell" : "buy", Tag.TAG_COMPOUND);
        int rows = Math.max(1, (height - 90) / 22);
        if (list.isEmpty()) Ui.text(g, sellTab ? "Nada para vender (itens de monstros valem Eris)." : "Sem estoque.", x + 12, 60, Ui.DIM);
        for (int i = 0; i < rows && i + scroll < list.size(); i++) {
            CompoundTag c = list.getCompound(i + scroll);
            ItemStack st = Ui.stack(c.getString("item"), Math.max(1, c.getInt("count")));
            int y = 56 + i * 22 - 6;
            g.renderItem(st, x + 10, y);
            g.renderItemDecorations(font, st, x + 10, y);
            Ui.text(g, st.getHoverName().getString(), x + 32, y + 4, Ui.TEXT);
        }
        super.render(g, mx, my, pt);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        int size = tag.getList(sellTab ? "sell" : "buy", Tag.TAG_COMPOUND).size();
        int ns = Math.max(0, Math.min(Math.max(0, size - 1), scroll - (int) Math.signum(sy)));
        if (ns != scroll) { scroll = ns; rebuildWidgets(); }
        return true;
    }

    @Override
    public void onClose() {
        ClientRpgData.send("shop_close");
        super.onClose();
    }
}
