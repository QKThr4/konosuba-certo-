package com.konorpg.data;

import net.minecraft.nbt.CompoundTag;

public class QuestProgress {
    public int progress;
    public long deadline; // absolute game time, 0 = none

    public QuestProgress() {}

    public QuestProgress(int progress, long deadline) {
        this.progress = progress;
        this.deadline = deadline;
    }

    public CompoundTag save() {
        CompoundTag t = new CompoundTag();
        t.putInt("p", progress);
        t.putLong("d", deadline);
        return t;
    }

    public static QuestProgress load(CompoundTag t) {
        return new QuestProgress(t.getInt("p"), t.getLong("d"));
    }
}
