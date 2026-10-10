package com.konorpg.data;

import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/** Persistent record of a companion; the entity is materialized next to the owner from this data. */
public class CompanionRecord {
    public String uid = UUID.randomUUID().toString();
    public String kind = "aqua";
    public String name = "Aqua";
    public int level = 1;
    public long xp;
    public int friendship;
    public ItemStack weapon = ItemStack.EMPTY;
    public long reviveAt;
    public long abilityCdUntil; // resurrection / big ability cooldown (game time)
    public boolean stay;

    public CompoundTag save(HolderLookup.Provider prov) {
        CompoundTag t = new CompoundTag();
        t.putString("uid", uid);
        t.putString("kind", kind);
        t.putString("name", name);
        t.putInt("lvl", level);
        t.putLong("xp", xp);
        t.putInt("fr", friendship);
        t.put("weapon", weapon.saveOptional(prov));
        t.putLong("rev", reviveAt);
        t.putLong("cd", abilityCdUntil);
        t.putBoolean("stay", stay);
        return t;
    }

    public static CompanionRecord load(CompoundTag t, HolderLookup.Provider prov) {
        CompanionRecord r = new CompanionRecord();
        r.uid = t.getString("uid");
        if (r.uid.isEmpty()) r.uid = UUID.randomUUID().toString();
        r.kind = t.getString("kind");
        r.name = t.getString("name");
        r.level = Math.max(1, t.getInt("lvl"));
        r.xp = t.getLong("xp");
        r.friendship = t.getInt("fr");
        r.weapon = ItemStack.parseOptional(prov, t.getCompound("weapon"));
        r.reviveAt = t.getLong("rev");
        r.abilityCdUntil = t.getLong("cd");
        r.stay = t.getBoolean("stay");
        return r;
    }
}
