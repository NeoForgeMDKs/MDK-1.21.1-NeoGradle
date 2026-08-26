package com.rz.mswsm.mixin;

import com.rz.mswsm.Config;
import com.rz.mswsm.diagnostic.GenerationRefTracker;
import com.rz.mswsm.diagnostic.ShutdownTracker;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.level.ChunkPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Mixin(ChunkMap.class)
public abstract class ChunkMapDiagnosticMixin {

    private static final Logger LOGGER = LogManager.getLogger("MSWSM-ChunkDiagnostic");
    private static final ConcurrentHashMap<Long, AtomicInteger> UNLOAD_COUNTS = new ConcurrentHashMap<>();
    private static final AtomicInteger HISTORY_DUMPS = new AtomicInteger();
    private static final int MAX_HISTORY_DUMPS = 20;

    @Inject(
            method = "scheduleUnload(JLnet/minecraft/server/level/ChunkHolder;)V",
            at = @At("HEAD")
    )
    private void mswsm$trackChunkUnload(
            long packedChunkPos,
            ChunkHolder holder,
            CallbackInfo ci
    ) {
        if (!Config.chunkDiagnosticsEnabled() || !ShutdownTracker.isShuttingDown()) {
            return;
        }

        int count = UNLOAD_COUNTS
                .computeIfAbsent(packedChunkPos, ignored -> new AtomicInteger())
                .incrementAndGet();

        if (count == 10
                || count == 100
                || count == 1000
                || count == 10000
                || count == 100000) {

            ChunkPos pos = new ChunkPos(packedChunkPos);

            LOGGER.error(
                    """
                    [CARNIVAL DIAGNOSTIC]
                    Chunk x={}, z={}
                    unloadAttempts={}
                    packed={}
                    readyForSaving={}
                    saveSyncDone={}
                    generationRefCount={}
                    ticketLevel={}
                    latestStatus={}
                    fullStatus={}
                    """,
                    pos.x,
                    pos.z,
                    count,
                    packedChunkPos,
                    holder.isReadyForSaving(),
                    holder.getSaveSyncFuture().isDone(),
                    holder.getGenerationRefCount(),
                    holder.getTicketLevel(),
                    holder.getLatestStatus(),
                    holder.getFullStatus()
            );
        }

        if (count == 1000
                && holder.getGenerationRefCount() > 0
                && !GenerationRefTracker.describe(packedChunkPos)
                .startsWith("(No generation-reference")
                && HISTORY_DUMPS.getAndIncrement() < MAX_HISTORY_DUMPS) {

            ChunkPos pos = new ChunkPos(packedChunkPos);

            LOGGER.error(
                    """

                    ==================================================
                    [CARNIVAL GENERATION REF HISTORY]
                    STUCK CHUNK x={}, z={}
                    currentGenerationRefCount={}
                    readyForSaving={}
                    saveSyncDone={}

                    {}
                    ==================================================
                    """,
                    pos.x,
                    pos.z,
                    holder.getGenerationRefCount(),
                    holder.isReadyForSaving(),
                    holder.getSaveSyncFuture().isDone(),
                    GenerationRefTracker.describe(packedChunkPos)
            );
        }
    }
}
