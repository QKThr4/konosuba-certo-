package com.konorpg.client.gui;

import com.konorpg.Config;
import com.konorpg.client.ClientRpgData;
import com.konorpg.data.CompanionRecord;
import com.konorpg.data.PlayerRpgData;
import com.konorpg.rpg.Factions;
import com.konorpg.rpg.Friendship;
import com.konorpg.rpg.RpgClass;
import com.konorpg.rpg.RpgClasses;
import com.konorpg.rpg.RpgStats;
import com.konorpg.rpg.Stat;
import com.konorpg.skill.Skill;
import com.konorpg.skill.Skills;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * General hub: one screen with a button per panel (Status, Skills, Quests, Reputation, Companion, Card,
 * Equipment, Guild, Settings/HUD) plus a shortcut to the vanilla inventory.
 */
public class RpgHubScreen extends Screen {
    private static final String[][] TABS = {
            {"status", "STATUS"}, {"skills", "SKILLS"}, {"quests", "QUESTS"}, {"inventory", "INVENTORY"},
            {"companion", "COMPANION"}, {"reputation", "REPUTATION"}, {"card", "ADVENTURER CARD"},
            {"equip", "EQUIPAMENTO"}, {"guild", "GUILDA"}, {"settings", "HUD / CONFIG"}};

    private String tab;
    private int scroll;
    private int px, py, pw, ph, cx, cw;

    public RpgHubScreen(String tab) {
        super(Component.literal("Painel do Aventureiro"));
        this.tab = tab == null || tab.isEmpty() ? "status" : tab;
    }

    public void switchTab(String t) { tab = t; scroll = 0; rebuildWidgets(); }

    private PlayerRpgData d() { return ClientRpgData.DATA; }

    @Override
    protected void init() {
        pw = Math.min(width - 6, 470);
        ph = Math.min(height - 6, 250);
        px = (width - pw) / 2;
        py = (height - ph) / 2;
        cx = px + 98;
        cw = pw - 106;
        int y = py + 8;
        for (String[] t : TABS) {
            final String id = t[0];
            Button b = Button.builder(Component.literal(t[1]), x -> {
                if (id.equals("inventory")) { Minecraft.getInstance().setScreen(new InventoryScreen(Minecraft.getInstance().player)); return; }
                switchTab(id);
            }).bounds(px + 6, y, 86, 18).build();
            b.active = !id.equals(tab);
            addRenderableWidget(b);
            y += 20;
        }
        initTab();
    }

    private void send(String act, String key, String val) {
        CompoundTag t = new CompoundTag();
        t.putString(key, val);
        ClientRpgData.send(act, t);
    }

    private void initTab() {
        PlayerRpgData d = d();
        switch (tab) {
            case "status" -> {
                if (d.statPoints > 0) {
                    int y = py + 70;
                    for (Stat s : Stat.values()) {
                        addRenderableWidget(Button.builder(Component.literal("+"), b -> send("stat_add", "stat", s.name())).bounds(cx + 150, y - 2, 14, 12).build());
                        y += 14;
                    }
                }
            }
            case "skills" -> {
                List<Skill> list = skillList();
                int rows = Math.max(1, (ph - 56) / 22);
                for (int i = 0; i < rows && i + scroll < list.size(); i++) {
                    Skill s = list.get(i + scroll);
                    int y = py + 40 + i * 22;
                    if (d.skills.contains(s.id())) {
                        if (s.type() != Skill.Type.PASSIVE) {
                            int cur = -1;
                            for (int k = 0; k < 4; k++) if (d.slots[k].equals(s.id())) cur = k;
                            final int next = cur + 1;
                            addRenderableWidget(Button.builder(Component.literal(cur < 0 ? "Atribuir tecla" : "Tecla " + "ZXCV".charAt(cur)), b -> {
                                CompoundTag t = new CompoundTag();
                                t.putString("id", next > 3 ? "" : s.id());
                                t.putInt("slot", next > 3 ? 0 : next);
                                if (next > 3) { for (int k = 0; k < 4; k++) if (d.slots[k].equals(s.id())) t.putInt("slot", k); }
                                ClientRpgData.send("skill_bind", t);
                            }).bounds(cx + cw - 90, y, 86, 18).build());
                        }
                    } else {
                        Button b = Button.builder(Component.literal("Aprender (" + s.spCost() + " SP)"), x -> send("skill_learn", "id", s.id())).bounds(cx + cw - 110, y, 106, 18).build();
                        b.active = Skills.canLearn(d, s);
                        addRenderableWidget(b);
                    }
                }
            }
            case "quests" -> {
                addRenderableWidget(Button.builder(Component.literal("Abrir Mural"), b -> ClientRpgData.send("board_open")).bounds(cx + cw - 90, py + 8, 86, 16).build());
                int y = py + 60;
                for (String id : d.quests.keySet()) {
                    final String qid = id;
                    var qi = ClientRpgData.QINFO.get(id);
                    if (qi == null) continue;
                    addRenderableWidget(Button.builder(Component.literal("Receber"), b -> send("quest_claim", "id", qid)).bounds(cx + cw - 110, y, 54, 14).build());
                    Button ab = Button.builder(Component.literal("Abandonar"), b -> send("quest_abandon", "id", qid)).bounds(cx + cw - 54, y, 52, 14).build();
                    ab.active = !qi.getString("cat").equals("main");
                    addRenderableWidget(ab);
                    y += 30;
                    if (y > py + ph - 20) break;
                }
            }
            case "companion" -> {
                CompanionRecord r = d.companion("aqua");
                if (r != null) addRenderableWidget(Button.builder(Component.literal(r.stay ? "Mandar seguir" : "Mandar ficar"), b -> ClientRpgData.send("companion_stay")).bounds(cx + 4, py + 90, 100, 16).build());
            }
            case "equip" -> {
                for (int i = 0; i < 3; i++) {
                    final int slot = i;
                    addRenderableWidget(Button.builder(Component.literal("Remover"), b -> {
                        CompoundTag t = new CompoundTag();
                        t.putInt("slot", slot);
                        ClientRpgData.send("acc_unequip", t);
                    }).bounds(cx + cw - 60, py + 46 + i * 26, 54, 16).build());
                }
                addRenderableWidget(Button.builder(Component.literal("Equipar item da mão"), b -> ClientRpgData.send("acc_equip")).bounds(cx + 4, py + 130, 130, 16).build());
            }
            case "guild" -> {
                if (!d.registered) {
                    addRenderableWidget(Button.builder(Component.literal("Pagar taxa e registrar (" + Config.REG_FEE.get() + " Eris)"), b -> ClientRpgData.send("register")).bounds(cx + 4, py + 90, 200, 18).build());
                } else {
                    List<RpgClass> cl = new ArrayList<>();
                    for (RpgClass c : RpgClasses.all()) if (!c.id().equals("adventurer")) cl.add(c);
                    int rows = Math.max(1, (ph - 64) / 20);
                    for (int i = 0; i < rows && i + scroll < cl.size(); i++) {
                        RpgClass c = cl.get(i + scroll);
                        Button b = Button.builder(Component.literal(c.id().equals(d.classId) ? "Atual" : "Escolher"), x -> send("class_set", "id", c.id())).bounds(cx + cw - 64, py + 44 + i * 20, 60, 16).build();
                        b.active = !c.id().equals(d.classId);
                        addRenderableWidget(b);
                    }
                }
            }
            case "settings" -> {
                addToggle(py + 40, "Status (HP/Mana/XP)", Config.HUD_STATS);
                addToggle(py + 62, "Progresso da aventura", Config.HUD_PROGRESS);
                addToggle(py + 84, "Barra de habilidades", Config.HUD_SKILLS);
                addToggle(py + 106, "Rastreador de quest", Config.HUD_TRACKER);
            }
            default -> { }
        }
    }

    private void addToggle(int y, String label, net.neoforged.neoforge.common.ModConfigSpec.BooleanValue v) {
        addRenderableWidget(Button.builder(Component.literal(label + ": " + (v.get() ? "ON" : "OFF")), b -> {
            v.set(!v.get());
            Config.CLIENT_SPEC.save();
            rebuildWidgets();
        }).bounds(cx + 4, y, 200, 18).build());
    }

    private List<Skill> skillList() {
        List<Skill> out = new ArrayList<>();
        var lines = RpgClasses.lines(d().classId);
        for (Skill s : Skills.all()) {
            if (lines.contains(s.group()) || d().skills.contains(s.id())) out.add(s);
        }
        return out;
    }

    @Override public void renderBackground(GuiGraphics g, int mx, int my, float pt) { }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        g.fill(0, 0, width, height, 0x88000000);
        Ui.panel(g, px, py, pw, ph);
        g.fill(px + 94, py + 6, px + 95, py + ph - 6, Ui.GOLD);
        PlayerRpgData d = d();
        int y = py + 8;
        switch (tab) {
            case "status" -> {
                RpgClass c = RpgClasses.get(d.classId);
                var p = Minecraft.getInstance().player;
                Ui.text(g, p.getGameProfile().getName() + " - " + c.name(), cx, y, Ui.GOLD);
                Ui.text(g, "Nível " + d.level + "   Rank " + d.rank().name() + "   " + d.money + " Eris", cx, y + 12, Ui.TEXT);
                Ui.bar(g, cx, y + 26, 150, 8, (double) d.xp / RpgStats.xpForNext(d.level), 0xFF66BB44);
                Ui.text(g, "XP " + d.xp + "/" + RpgStats.xpForNext(d.level), cx + 156, y + 26, Ui.DIM);
                Ui.bar(g, cx, y + 38, 150, 8, p.getHealth() / p.getMaxHealth(), 0xFFCC2222);
                Ui.text(g, "HP " + (int) p.getHealth() + "/" + (int) p.getMaxHealth(), cx + 156, y + 38, Ui.DIM);
                double mm = RpgStats.maxMana(p, d);
                Ui.bar(g, cx, y + 50, 150, 8, d.mana / mm, 0xFF2266DD);
                Ui.text(g, "MP " + (int) d.mana + "/" + (int) mm, cx + 156, y + 50, Ui.DIM);
                int sy = py + 70;
                for (Stat s : Stat.values()) {
                    int eff = RpgStats.eff(p, d, s);
                    Ui.text(g, s.code + " (" + s.label + "): " + eff + (eff != d.stat(s) ? " (base " + d.stat(s) + ")" : ""), cx, sy, Ui.TEXT);
                    sy += 14;
                }
                Ui.text(g, "Pontos de atributo: " + d.statPoints + "   Pontos de habilidade: " + d.skillPoints, cx, sy + 4, Ui.GOLD);
                Ui.text(g, "Crítico: " + (int) (RpgStats.critChance(p, d) * 100) + "%   Poder mágico x" + String.format("%.2f", RpgStats.spellPower(p, d)), cx, sy + 18, Ui.DIM);
            }
            case "skills" -> {
                Ui.text(g, "HABILIDADES - " + RpgClasses.get(d.classId).name() + "   (SP: " + d.skillPoints + ")", cx, y, Ui.GOLD);
                Ui.text(g, "Teclas padrão: Z X C V (configuráveis)", cx, y + 12, Ui.DIM);
                List<Skill> list = skillList();
                int rows = Math.max(1, (ph - 56) / 22);
                for (int i = 0; i < rows && i + scroll < list.size(); i++) {
                    Skill s = list.get(i + scroll);
                    int sy = py + 40 + i * 22;
                    boolean learned = d.skills.contains(s.id());
                    Ui.text(g, (learned ? "* " : "  ") + s.name() + "  [" + s.type().name().toLowerCase() + ", Nv " + s.minLevel() + (s.mana() > 0 ? ", " + s.mana() + " MP" : "") + "]", cx, sy, learned ? 0xFFAAFFAA : Ui.TEXT);
                    String desc = s.desc().length() > 62 ? s.desc().substring(0, 62) + "..." : s.desc();
                    Ui.text(g, desc, cx, sy + 10, Ui.DIM);
                }
            }
            case "quests" -> {
                Ui.text(g, "MISSÕES ATIVAS", cx, y, Ui.GOLD);
                Ui.text(g, "Main Quest: " + d.mainDone + "/" + ClientRpgData.mainTotal + "   Concluídas: " + d.questsCompleted, cx, y + 12, Ui.TEXT);
                int qy = py + 60;
                if (d.quests.isEmpty()) Ui.text(g, "Nenhuma quest ativa. Visite o mural da Guilda (cidade).", cx, qy, Ui.DIM);
                for (var e : d.quests.entrySet()) {
                    var qi = ClientRpgData.QINFO.get(e.getKey());
                    if (qi == null) continue;
                    Ui.text(g, qi.getString("name") + (qi.getString("cat").equals("main") ? " [PRINCIPAL]" : ""), cx, qy - 12, Ui.GOLD);
                    Ui.text(g, Ui.objective(qi.getString("type"), qi.getString("target"), qi.getInt("amount")) + "  (" + e.getValue().progress + "/" + qi.getInt("amount") + ")", cx, qy + 1, Ui.TEXT);
                    qy += 30;
                    if (qy > py + ph - 20) break;
                }
            }
            case "reputation" -> {
                Ui.text(g, "REPUTAÇÃO", cx, y, Ui.GOLD);
                int ry = py + 30;
                for (String f : Factions.IDS) {
                    int v = d.rep(f);
                    Ui.text(g, Factions.name(f) + ": " + v + " (" + Factions.tier(v) + ")", cx, ry, Ui.TEXT);
                    Ui.bar(g, cx, ry + 11, 160, 6, (v + 20) / 240.0, v < 0 ? 0xFFCC2222 : 0xFFCCAA22);
                    Ui.text(g, Factions.perk(f), cx, ry + 20, Ui.DIM);
                    ry += 36;
                }
            }
            case "companion" -> {
                Ui.text(g, "COMPANHEIROS", cx, y, Ui.GOLD);
                if (d.companions.isEmpty()) {
                    Ui.text(g, d.perk.equals("aqua") ? "Aqua se junta a você após o registro na Guilda." : "Nenhum companheiro ainda.", cx, y + 16, Ui.DIM);
                }
                int cy = py + 28;
                for (CompanionRecord r : d.companions) {
                    Ui.text(g, r.name + "  Nv " + r.level + "   " + Friendship.name(r.friendship) + " (" + r.friendship + ")", cx, cy, Ui.TEXT);
                    Ui.text(g, r.stay ? "Modo: ficar parada" : "Modo: seguindo", cx, cy + 12, Ui.DIM);
                    cy += 70;
                }
                Ui.text(g, "EM BREVE: presentes, equipar armas, recrutar aventureiros.", cx, py + ph - 20, Ui.DIM);
            }
            case "card" -> {
                RpgClass c = RpgClasses.get(d.classId);
                var p = Minecraft.getInstance().player;
                g.fill(cx, py + 8, cx + cw, py + ph - 8, 0xFF3A2C10);
                g.renderOutline(cx + 2, py + 10, cw - 4, ph - 20, Ui.GOLD);
                Ui.text(g, "CARTÃO DE AVENTUREIRO", cx + 8, py + 16, Ui.GOLD);
                Ui.text(g, "Nome: " + p.getGameProfile().getName(), cx + 8, py + 32, Ui.TEXT);
                Ui.text(g, "Classe: " + c.name() + "   Nível: " + d.level, cx + 8, py + 44, Ui.TEXT);
                Ui.text(g, "XP: " + d.xp + "/" + RpgStats.xpForNext(d.level) + "   Rank: " + d.rank().name(), cx + 8, py + 56, Ui.TEXT);
                Ui.text(g, "Reputação (Guilda): " + d.rep("guild"), cx + 8, py + 68, Ui.TEXT);
                int sy = py + 84;
                for (Stat s : Stat.values()) { Ui.text(g, s.code + ": " + RpgStats.eff(p, d, s), cx + 8 + (s.ordinal() % 3) * 90, sy + (s.ordinal() / 3) * 12, Ui.TEXT); }
                StringBuilder sb = new StringBuilder("Habilidades: ");
                d.skills.forEach(k -> { Skill s = Skills.get(k); if (s != null) sb.append(s.name()).append(", "); });
                for (var seq : font.split(Component.literal(sb.toString()), cw - 16)) { g.drawString(font, seq, cx + 8, sy + 34, Ui.TEXT, false); sy += 10; }
                Ui.text(g, "Conquistas: Quests " + d.questsCompleted + " | Monstros " + d.monstersKilled + " | Chefes " + d.bossesDefeated + " | Mortes " + d.deaths, cx + 8, py + ph - 24, Ui.DIM);
                if (!d.registered) Ui.text(g, "(Não registrado - visite a Guilda)", cx + 8, py + ph - 36, 0xFFFF7777);
            }
            case "equip" -> {
                Ui.text(g, "ACESSÓRIOS (2 anéis, 1 colar)", cx, y, Ui.GOLD);
                String[] names = {"Anel 1", "Anel 2", "Colar"};
                for (int i = 0; i < 3; i++) {
                    int ey = py + 44 + i * 26;
                    ItemStack st = d.accessories[i];
                    Ui.text(g, names[i] + ":", cx, ey + 4, Ui.TEXT);
                    g.fill(cx + 46, ey - 1, cx + 64, ey + 17, 0xFF000000);
                    g.renderItem(st, cx + 48, ey);
                    Ui.text(g, st.isEmpty() ? "(vazio)" : st.getHoverName().getString(), cx + 70, ey + 4, st.isEmpty() ? Ui.DIM : Ui.TEXT);
                }
                Ui.text(g, "Segure um anel/colar e clique em 'Equipar item da mão'.", cx, py + 112, Ui.DIM);
                Ui.text(g, "Bônus de armas/acessórios somam nos seus atributos.", cx, py + 152, Ui.DIM);
                Ui.text(g, "EM BREVE: armaduras RPG, raridades de loot e encantamentos.", cx, py + ph - 20, Ui.DIM);
            }
            case "guild" -> {
                Ui.text(g, "GUILDA DOS AVENTUREIROS", cx, y, Ui.GOLD);
                if (!d.registered) {
                    Ui.text(g, "Você ainda não é um aventureiro registrado.", cx, py + 30, Ui.TEXT);
                    Ui.text(g, "Taxa de inscrição: " + Config.REG_FEE.get() + " Eris. Vá até a Guilda (cidade).", cx, py + 44, Ui.TEXT);
                    Ui.text(g, "Seu dinheiro: " + d.money + " Eris", cx, py + 58, Ui.DIM);
                } else {
                    Ui.text(g, "Escolha/avance sua classe (na Guilda). Classe atual: " + RpgClasses.get(d.classId).name(), cx, y + 12, Ui.TEXT);
                    List<RpgClass> cl = new ArrayList<>();
                    for (RpgClass c : RpgClasses.all()) if (!c.id().equals("adventurer")) cl.add(c);
                    int rows = Math.max(1, (ph - 64) / 20);
                    for (int i = 0; i < rows && i + scroll < cl.size(); i++) {
                        RpgClass c = cl.get(i + scroll);
                        String req = c.tier() == 1 ? "grátis (2000 Eris para trocar)" : "Nv " + c.minLevel() + ", Rank " + c.minRank().name() + ", " + c.cost() + " Eris, de " + RpgClasses.get(c.parent()).name();
                        Ui.text(g, c.name() + " - " + req, cx, py + 44 + i * 20, Ui.TEXT);
                        Ui.text(g, c.desc().length() > 58 ? c.desc().substring(0, 58) + "..." : c.desc(), cx, py + 54 + i * 20, Ui.DIM);
                    }
                    Ui.text(g, "EM BREVE: criar sua própria guilda e grupos de aventureiros.", cx, py + ph - 14, Ui.DIM);
                }
            }
            case "settings" -> {
                Ui.text(g, "HUD E CONFIGURAÇÕES", cx, y, Ui.GOLD);
                Ui.text(g, "Tecla K: este painel   J: progresso   Z X C V: habilidades", cx, py + 130, Ui.DIM);
                Ui.text(g, "Dificuldade/XP/dinheiro: config do mod (config/konorpg-common.toml)", cx, py + 142, Ui.DIM);
                Ui.text(g, "EM BREVE: dungeons, chefes, eventos aleatórios, profissões.", cx, py + 160, Ui.DIM);
            }
            default -> { }
        }
        super.render(g, mx, my, pt);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        if (tab.equals("skills") || tab.equals("guild")) {
            int size = tab.equals("skills") ? skillList().size() : RpgClasses.all().size() - 1;
            int ns = Math.max(0, Math.min(Math.max(0, size - 1), scroll - (int) Math.signum(sy)));
            if (ns != scroll) { scroll = ns; rebuildWidgets(); }
            return true;
        }
        return false;
    }
}
