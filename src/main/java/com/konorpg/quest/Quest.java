package com.konorpg.quest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.konorpg.rpg.Rank;
import com.konorpg.util.J;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Data-driven quest definition (data/<ns>/konorpg_quests/*.json). */
public class Quest {
    public String id, name, desc, location, category, type, target, humor;
    public int amount, xp, money, difficulty, minLevel, timeLimitMin;
    public Rank minRank;
    public boolean repeatable;
    public final Map<String, Integer> rep = new LinkedHashMap<>();
    public final List<String> requires = new ArrayList<>(), unlocks = new ArrayList<>();
    public final List<String[]> items = new ArrayList<>(); // {itemId, count}
    public JsonObject require;

    public static Quest parse(String id, JsonObject o) {
        Quest q = new Quest();
        q.id = id;
        q.name = J.str(o, "name", id);
        q.desc = J.str(o, "description", "");
        q.location = J.str(o, "location", "Qualquer lugar");
        q.category = J.str(o, "category", "board");
        q.type = J.str(o, "type", "kill");
        q.target = J.str(o, "target", "");
        q.humor = J.str(o, "humor", "");
        q.amount = Math.max(1, J.i(o, "amount", 1));
        q.xp = J.i(o, "xp", 0);
        q.money = J.i(o, "money", 0);
        q.difficulty = J.i(o, "difficulty", 1);
        q.minLevel = J.i(o, "minLevel", 1);
        q.timeLimitMin = J.i(o, "timeLimitMinutes", 0);
        q.minRank = Rank.byName(J.str(o, "minRank", "F"), Rank.F);
        q.repeatable = J.b(o, "repeatable", false);
        JsonObject rep = J.obj(o, "reputation");
        if (rep != null) for (var e : rep.entrySet()) q.rep.put(e.getKey(), e.getValue().getAsInt());
        for (JsonElement e : J.arr(o, "requires")) q.requires.add(e.getAsString());
        for (JsonElement e : J.arr(o, "unlocks")) q.unlocks.add(e.getAsString());
        for (JsonElement e : J.arr(o, "rewardItems")) {
            JsonObject it = e.getAsJsonObject();
            q.items.add(new String[]{J.str(it, "item", "minecraft:air"), String.valueOf(J.i(it, "count", 1))});
        }
        q.require = J.obj(o, "require");
        return q;
    }
}
