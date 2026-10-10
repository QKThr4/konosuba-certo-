package com.konorpg.shop;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.konorpg.KonoRPG;
import com.konorpg.data.PlayerRpgData;
import com.konorpg.network.Net;
import com.konorpg.rpg.Factions;
import com.konorpg.rpg.RpgManager;
import com.konorpg.util.J;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Data-driven shops (buy + sell). EM BREVE: blacksmith/alchemist crafting and services (repair, reinforce). */
public final class ShopManager extends SimpleJsonResourceReloadListener {
    public static final ShopManager INSTANCE = new ShopManager();

    private record Entry(String item, int price) {}
    private record Shop(String name, List<Entry> buy, boolean sell) {}

    private static final Map<String, Shop> SHOPS = new LinkedHashMap<>();
    private static final Map<UUID, String> SESSION = new HashMap<>();

    private ShopManager() { super(new Gson(), "konorpg_shops"); }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager rm, ProfilerFiller pf) {
        SHOPS.clear();
        for (var e : map.entrySet()) {
            try {
                JsonObject o = e.getValue().getAsJsonObject();
                List<Entry> buy = new ArrayList<>();
                for (JsonElement b : J.arr(o, "buy")) {
                    JsonObject bo = b.getAsJsonObject();
                    buy.add(new Entry(J.str(bo, "item", "minecraft:air"), J.i(bo, "price", 10)));
                }
                SHOPS.put(e.getKey().getPath(), new Shop(J.str(o, "name", "Loja"), buy, J.b(o, "sell", true)));
            } catch (Exception ex) {
                KonoRPG.LOGGER.error("Bad shop {}", e.getKey(), ex);
            }
        }
    }

    private static int discount(PlayerRpgData d) { return Factions.shopDiscount(d.rep("merchants")); }

    private static int price(int base, PlayerRpgData d) { return Math.max(1, base * (100 - discount(d)) / 100); }

    public static void open(ServerPlayer p, String shopId) {
        Shop s = SHOPS.get(shopId);
        if (s == null) { RpgManager.msg(p, "Esta loja ainda está em construção (EM BREVE)."); return; }
        SESSION.put(p.getUUID(), shopId);
        PlayerRpgData d = RpgManager.data(p);
        CompoundTag t = new CompoundTag();
        t.putString("name", s.name());
        t.putLong("money", d.money);
        t.putInt("discount", discount(d));
        ListTag buy = new ListTag();
        for (Entry e : s.buy()) {
            CompoundTag c = new CompoundTag();
            c.putString("item", e.item());
            c.putInt("price", price(e.price(), d));
            buy.add(c);
        }
        t.put("buy", buy);
        ListTag sell = new ListTag();
        if (s.sell()) {
            for (int i = 0; i < p.getInventory().items.size(); i++) {
                ItemStack st = p.getInventory().items.get(i);
                int unit = st.isEmpty() ? 0 : EconomyManager.sellPrice(st.getItem());
                if (unit <= 0) continue;
                CompoundTag c = new CompoundTag();
                c.putInt("slot", i);
                c.putString("item", BuiltInRegistries.ITEM.getKey(st.getItem()).toString());
                c.putInt("count", st.getCount());
                c.putInt("unit", unit);
                sell.add(c);
            }
        }
        t.put("sell", sell);
        Net.openScreen(p, "shop", t);
    }

    public static void buy(ServerPlayer p, int idx, int qty) {
        String id = SESSION.get(p.getUUID());
        Shop s = id == null ? null : SHOPS.get(id);
        if (s == null || idx < 0 || idx >= s.buy().size()) return;
        qty = Math.max(1, Math.min(qty, 64));
        Entry e = s.buy().get(idx);
        PlayerRpgData d = RpgManager.data(p);
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(e.item()));
        long cost = (long) price(e.price(), d) * qty;
        if (!RpgManager.spend(p, cost)) { RpgManager.msg(p, "Eris insuficientes!"); return; }
        com.konorpg.rpg.Perks.give(p, new ItemStack(item, qty));
        RpgManager.sync(p);
        open(p, id);
    }

    public static void sell(ServerPlayer p, int slot, boolean all) {
        String id = SESSION.get(p.getUUID());
        if (id == null || slot < 0 || slot >= p.getInventory().items.size()) return;
        ItemStack st = p.getInventory().items.get(slot);
        int unit = st.isEmpty() ? 0 : EconomyManager.sellPrice(st.getItem());
        if (unit <= 0) return;
        int n = all ? st.getCount() : 1;
        st.shrink(n);
        RpgManager.grantMoney(p, (long) unit * n);
        if (p.getRandom().nextInt(4) == 0) RpgManager.addRep(p, "merchants", 1);
        RpgManager.sync(p);
        open(p, id);
    }

    public static void close(ServerPlayer p) { SESSION.remove(p.getUUID()); }
}
