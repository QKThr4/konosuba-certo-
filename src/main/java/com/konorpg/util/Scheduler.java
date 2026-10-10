package com.konorpg.util;

import com.konorpg.KonoRPG;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Simple server-thread tick scheduler. */
public final class Scheduler {
    private static final class Task {
        int ticks;
        final Runnable run;
        Task(int t, Runnable r) { ticks = t; run = r; }
    }

    private static final List<Task> TASKS = new ArrayList<>();

    public static void later(int ticks, Runnable r) {
        TASKS.add(new Task(Math.max(1, ticks), r));
    }

    public static void tick() {
        if (TASKS.isEmpty()) return;
        List<Task> due = new ArrayList<>();
        Iterator<Task> it = TASKS.iterator();
        while (it.hasNext()) {
            Task t = it.next();
            if (--t.ticks <= 0) { due.add(t); it.remove(); }
        }
        for (Task t : due) {
            try { t.run.run(); } catch (Exception e) { KonoRPG.LOGGER.error("Scheduled task failed", e); }
        }
    }

    public static void clear() { TASKS.clear(); }

    private Scheduler() {}
}
