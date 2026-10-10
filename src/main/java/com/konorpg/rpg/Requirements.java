package com.konorpg.rpg;

import com.google.gson.JsonObject;
import com.konorpg.data.PlayerRpgData;
import com.konorpg.util.J;

/** JSON-driven requirement checks shared by quests and dialogues. */
public final class Requirements {
    public static boolean check(PlayerRpgData d, JsonObject r) {
        if (r == null) return true;
        if (r.has("registered") && d.registered != r.get("registered").getAsBoolean()) return false;
        if (r.has("min_level") && d.level < r.get("min_level").getAsInt()) return false;
        if (r.has("class") && !d.classId.equals(r.get("class").getAsString())) return false;
        if (r.has("min_rank") && d.rank().ordinal() < Rank.byName(r.get("min_rank").getAsString(), Rank.F).ordinal()) return false;
        if (r.has("money") && d.money < r.get("money").getAsLong()) return false;
        if (r.has("quest_active") && !d.quests.containsKey(r.get("quest_active").getAsString())) return false;
        if (r.has("quest_done") && !d.completed.contains(r.get("quest_done").getAsString())) return false;
        if (r.has("flag") && !d.flags.contains(r.get("flag").getAsString())) return false;
        if (r.has("no_flag") && d.flags.contains(r.get("no_flag").getAsString())) return false;
        if (r.has("companion") && d.companion(r.get("companion").getAsString()) == null) return false;
        if (r.has("perk") && !d.perk.equals(r.get("perk").getAsString())) return false;
        JsonObject rep = J.obj(r, "rep");
        if (rep != null) for (var e : rep.entrySet()) if (d.rep(e.getKey()) < e.getValue().getAsInt()) return false;
        JsonObject fr = J.obj(r, "friendship");
        if (fr != null && d.friendLevel(J.str(fr, "id", "")) < J.i(fr, "min", 0)) return false;
        return true;
    }

    private Requirements() {}
}
