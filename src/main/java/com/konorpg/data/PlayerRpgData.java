package com.konorpg.data;

import com.konorpg.rpg.Friendship;
import com.konorpg.rpg.Rank;
import com.konorpg.rpg.Stat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;

/** All per-player RPG state. Stored as a NeoForge data attachment (survives death, relog, restart). */
public class PlayerRpgData implements INBTSerializable<CompoundTag> {
    public boolean introDone, registered;
    public String perk = "", classId = "adventurer", guild = "";
    public int level = 1, statPoints, skillPoints;
    public long xp, money;
    public int[] stats = {5, 5, 5, 5, 5, 5};
    public double mana = 40;
    public final Map<String, Integer> rep = new HashMap<>();
    public int questsCompleted, dungeonsCleared, bossesDefeated, monstersKilled, deaths, mainDone, partyStrikes;
    public final Set<String> skills = new LinkedHashSet<>();
    public final String[] slots = {"", "", "", ""};
    public final Map<String, Long> cooldowns = new HashMap<>();
    public final Map<String, QuestProgress> quests = new LinkedHashMap<>();
    public final Set<String> completed = new HashSet<>();
    public final Set<String> flags = new HashSet<>();
    public final Map<String, Integer> friends = new HashMap<>();
    public final List<CompanionRecord> companions = new ArrayList<>();
    public final ItemStack[] accessories = {ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY};
    public String pendingParty = "";
    public long pendingPartyDeadline, nextPartyMission, incapacitatedUntil, nextEventAt;

    public int stat(Stat s) { return stats[s.ordinal()]; }

    public int rep(String faction) { return rep.getOrDefault(faction, 0); }

    public int rankPoints() {
        return questsCompleted * 3 + bossesDefeated * 25 + dungeonsCleared * 15 + Math.max(0, rep("guild"));
    }

    public Rank rank() { return Rank.of(rankPoints()); }

    public CompanionRecord companion(String kind) {
        for (CompanionRecord r : companions) if (r.kind.equals(kind)) return r;
        return null;
    }

    public int friendPoints(String id) {
        CompanionRecord r = companion(id);
        return r != null ? r.friendship : friends.getOrDefault(id, 0);
    }

    public int friendLevel(String id) { return Friendship.level(friendPoints(id)); }

    public void addFriend(String id, int amount) {
        CompanionRecord r = companion(id);
        if (r != null) r.friendship = Math.max(0, r.friendship + amount);
        else friends.put(id, Math.max(0, friends.getOrDefault(id, 0) + amount));
    }

    public boolean hasQuestActive(String id) { return quests.containsKey(id); }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider prov) {
        CompoundTag t = new CompoundTag();
        t.putBoolean("intro", introDone);
        t.putBoolean("reg", registered);
        t.putString("perk", perk);
        t.putString("class", classId);
        t.putString("guild", guild);
        t.putInt("lvl", level);
        t.putInt("sp", statPoints);
        t.putInt("skp", skillPoints);
        t.putLong("xp", xp);
        t.putLong("money", money);
        t.putIntArray("stats", stats);
        t.putDouble("mana", mana);
        CompoundTag repT = new CompoundTag();
        rep.forEach(repT::putInt);
        t.put("rep", repT);
        t.putInt("qc", questsCompleted);
        t.putInt("dc", dungeonsCleared);
        t.putInt("bd", bossesDefeated);
        t.putInt("mk", monstersKilled);
        t.putInt("deaths", deaths);
        t.putInt("main", mainDone);
        t.putInt("strikes", partyStrikes);
        ListTag sk = new ListTag();
        skills.forEach(s -> sk.add(StringTag.valueOf(s)));
        t.put("skills", sk);
        ListTag sl = new ListTag();
        for (String s : slots) sl.add(StringTag.valueOf(s));
        t.put("slots", sl);
        CompoundTag cd = new CompoundTag();
        cooldowns.forEach(cd::putLong);
        t.put("cd", cd);
        CompoundTag qs = new CompoundTag();
        quests.forEach((k, v) -> qs.put(k, v.save()));
        t.put("quests", qs);
        ListTag done = new ListTag();
        completed.forEach(s -> done.add(StringTag.valueOf(s)));
        t.put("done", done);
        ListTag fl = new ListTag();
        flags.forEach(s -> fl.add(StringTag.valueOf(s)));
        t.put("flags", fl);
        CompoundTag fr = new CompoundTag();
        friends.forEach(fr::putInt);
        t.put("friends", fr);
        ListTag comp = new ListTag();
        for (CompanionRecord r : companions) comp.add(r.save(prov));
        t.put("comp", comp);
        ListTag acc = new ListTag();
        for (ItemStack s : accessories) acc.add(s.saveOptional(prov));
        t.put("acc", acc);
        t.putString("pparty", pendingParty);
        t.putLong("ppd", pendingPartyDeadline);
        t.putLong("npm", nextPartyMission);
        t.putLong("incap", incapacitatedUntil);
        t.putLong("nev", nextEventAt);
        return t;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider prov, CompoundTag t) {
        introDone = t.getBoolean("intro");
        registered = t.getBoolean("reg");
        perk = t.getString("perk");
        classId = t.contains("class") && !t.getString("class").isEmpty() ? t.getString("class") : "adventurer";
        guild = t.getString("guild");
        level = Math.max(1, t.getInt("lvl"));
        statPoints = t.getInt("sp");
        skillPoints = t.getInt("skp");
        xp = t.getLong("xp");
        money = t.getLong("money");
        int[] st = t.getIntArray("stats");
        stats = new int[6];
        for (int i = 0; i < 6; i++) stats[i] = i < st.length ? st[i] : 5;
        mana = t.contains("mana") ? t.getDouble("mana") : 40;
        rep.clear();
        CompoundTag repT = t.getCompound("rep");
        for (String k : repT.getAllKeys()) rep.put(k, repT.getInt(k));
        questsCompleted = t.getInt("qc");
        dungeonsCleared = t.getInt("dc");
        bossesDefeated = t.getInt("bd");
        monstersKilled = t.getInt("mk");
        deaths = t.getInt("deaths");
        mainDone = t.getInt("main");
        partyStrikes = t.getInt("strikes");
        skills.clear();
        for (Tag x : t.getList("skills", Tag.TAG_STRING)) skills.add(x.getAsString());
        ListTag sl = t.getList("slots", Tag.TAG_STRING);
        for (int i = 0; i < 4; i++) slots[i] = i < sl.size() ? sl.getString(i) : "";
        cooldowns.clear();
        CompoundTag cd = t.getCompound("cd");
        for (String k : cd.getAllKeys()) cooldowns.put(k, cd.getLong(k));
        quests.clear();
        CompoundTag qs = t.getCompound("quests");
        for (String k : qs.getAllKeys()) quests.put(k, QuestProgress.load(qs.getCompound(k)));
        completed.clear();
        for (Tag x : t.getList("done", Tag.TAG_STRING)) completed.add(x.getAsString());
        flags.clear();
        for (Tag x : t.getList("flags", Tag.TAG_STRING)) flags.add(x.getAsString());
        friends.clear();
        CompoundTag fr = t.getCompound("friends");
        for (String k : fr.getAllKeys()) friends.put(k, fr.getInt(k));
        companions.clear();
        ListTag comp = t.getList("comp", Tag.TAG_COMPOUND);
        for (int i = 0; i < comp.size(); i++) companions.add(CompanionRecord.load(comp.getCompound(i), prov));
        ListTag acc = t.getList("acc", Tag.TAG_COMPOUND);
        for (int i = 0; i < 3; i++) {
            accessories[i] = i < acc.size() ? ItemStack.parseOptional(prov, acc.getCompound(i)) : ItemStack.EMPTY;
        }
        pendingParty = t.getString("pparty");
        pendingPartyDeadline = t.getLong("ppd");
        nextPartyMission = t.getLong("npm");
        incapacitatedUntil = t.getLong("incap");
        nextEventAt = t.getLong("nev");
    }
}
