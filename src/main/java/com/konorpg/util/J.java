package com.konorpg.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/** Tiny lenient JSON helpers for data-driven content. */
public final class J {
    public static String str(JsonObject o, String k, String d) {
        return o != null && o.has(k) && o.get(k).isJsonPrimitive() ? o.get(k).getAsString() : d;
    }
    public static int i(JsonObject o, String k, int d) {
        try { return o != null && o.has(k) ? o.get(k).getAsInt() : d; } catch (Exception e) { return d; }
    }
    public static double dbl(JsonObject o, String k, double d) {
        try { return o != null && o.has(k) ? o.get(k).getAsDouble() : d; } catch (Exception e) { return d; }
    }
    public static boolean b(JsonObject o, String k, boolean d) {
        try { return o != null && o.has(k) ? o.get(k).getAsBoolean() : d; } catch (Exception e) { return d; }
    }
    public static JsonArray arr(JsonObject o, String k) {
        return o != null && o.has(k) && o.get(k).isJsonArray() ? o.getAsJsonArray(k) : new JsonArray();
    }
    public static JsonObject obj(JsonObject o, String k) {
        return o != null && o.has(k) && o.get(k).isJsonObject() ? o.getAsJsonObject(k) : null;
    }
    private J() {}
}
