package com.konorpg.shop;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;

/** Sell prices (data/<ns>/konorpg_economy/*.json: {"modid:item": price}). */
public final class EconomyManager extends SimpleJsonResourceReloadListener {
    public static final EconomyManager INSTANCE = new EconomyManager();
    private static final Map<String, Integer> SELL = new HashMap<>();

    private EconomyManager() { super(new Gson(), "konorpg_economy"); }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager rm, ProfilerFiller pf) {
        SELL.clear();
        for (JsonElement el : map.values()) {
            if (!el.isJsonObject()) continue;
            JsonObject o = el.getAsJsonObject();
            for (var e : o.entrySet()) SELL.put(e.getKey(), e.getValue().getAsInt());
        }
    }

    public static int sellPrice(Item item) {
        return SELL.getOrDefault(BuiltInRegistries.ITEM.getKey(item).toString(), 0);
    }
}
