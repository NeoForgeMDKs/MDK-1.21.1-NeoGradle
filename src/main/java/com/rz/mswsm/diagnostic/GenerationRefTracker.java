package com.rz.mswsm.diagnostic;

import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.world.level.ChunkPos;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class GenerationRefTracker {

    /*
     * Keep the most recent reference-changing events for every holder.
     * Eight is enough to expose repeated callers without retaining a
     * ridiculous amount of data.
     */
    private static final int MAX_HISTORY = 12;

    private static final Map<Long, History> HISTORIES =
            new ConcurrentHashMap<>();

    private GenerationRefTracker() {
    }

    public static void recordIncrease(
            GenerationChunkHolder holder,
            int before,
            int after
    ) {
        ChunkPos pos = holder.getPos();
        long packed = pos.toLong();

        History history = HISTORIES.computeIfAbsent(
                packed,
                ignored -> new History()
        );

        synchronized (history) {
            history.add(
                    "INCREASE",
                    before,
                    after,
                    Thread.currentThread().getName(),
                    captureStack()
            );
        }
    }

    public static void recordDecrease(
            GenerationChunkHolder holder,
            int before,
            int after
    ) {
        ChunkPos pos = holder.getPos();
        long packed = pos.toLong();

        History history = HISTORIES.computeIfAbsent(
                packed,
                ignored -> new History()
        );

        synchronized (history) {
            /*
             * We don't need a full stack for every decrement.
             * Record the transition and thread so we know refs are
             * being released without doubling stack-trace overhead.
             */
            history.add(
                    "DECREASE",
                    before,
                    after,
                    Thread.currentThread().getName(),
                    "(stack omitted for decrement)\n"
            );

            /*
             * Once the count reaches zero, the previous references
             * were successfully balanced. Anything that happens
             * after this is a fresh reference epoch.
             */
            if (after == 0) {
                history.markCleanBoundary();
            }
        }
    }

    public static String describe(long packedChunkPos) {
        History history = HISTORIES.get(packedChunkPos);

        if (history == null) {
            return "(No generation-reference history captured)";
        }

        synchronized (history) {
            return history.describe();
        }
    }

    private static String captureStack() {
        StackTraceElement[] stack =
                Thread.currentThread().getStackTrace();

        StringBuilder builder = new StringBuilder();

        int usefulFrames = 0;

        for (StackTraceElement frame : stack) {
            String cls = frame.getClassName();

            if (cls.equals(Thread.class.getName())
                    || cls.startsWith("com.rz.mswsm.diagnostic")
                    || cls.startsWith("com.rz.mswsm.mixin")) {
                continue;
            }

            builder.append("    at ")
                    .append(frame)
                    .append('\n');

            usefulFrames++;

            if (usefulFrames >= 80) {
                break;
            }
        }

        return builder.toString();
    }

    private static final class History {

        private final Deque<Event> events =
                new ArrayDeque<>();

        private long epoch = 0;

        void add(
                String type,
                int before,
                int after,
                String thread,
                String stack
        ) {
            while (events.size() >= MAX_HISTORY) {
                events.removeFirst();
            }

            events.addLast(
                    new Event(
                            epoch,
                            type,
                            before,
                            after,
                            thread,
                            System.currentTimeMillis(),
                            stack
                    )
            );
        }

        void markCleanBoundary() {
            epoch++;

            /*
             * Clear old balanced history. From this point forward,
             * anything left in the ring belongs to the reference
             * epoch that might eventually become stuck.
             */
            events.clear();
        }

        String describe() {
            StringBuilder builder = new StringBuilder();

            builder.append("Tracked epoch=")
                    .append(epoch)
                    .append('\n');

            for (Event event : events) {
                builder.append("\n--- ")
                        .append(event.type)
                        .append(' ')
                        .append(event.before)
                        .append(" -> ")
                        .append(event.after)
                        .append(" | epoch=")
                        .append(event.epoch)
                        .append(" | thread=")
                        .append(event.thread)
                        .append(" | time=")
                        .append(event.timeMillis)
                        .append(" ---\n")
                        .append(event.stack);
            }

            return builder.toString();
        }
    }

    private record Event(
            long epoch,
            String type,
            int before,
            int after,
            String thread,
            long timeMillis,
            String stack
    ) {
    }
}