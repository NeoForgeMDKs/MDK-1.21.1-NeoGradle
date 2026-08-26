package com.rz.mixin;

import com.rz.mswsm.Main;
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
public abstract class ChunkMapDiagnosticMixin
{
    private static final Logger LOGGER = LogManager.getLogger("MSWSM-ChunkDiagnostic");
    private static final ConcurrentHashMap<Long, AtomicInteger> UNLOAD_COUNTS = new ConcurrentHashMap<>();

    @Inject(
            method = "scheduleUnload(JLnet/minecraft/server/level/ChunkHolder;)V",
            at = @At("HEAD")
    )
    private void mswsm$trackChunkUnload(
            long packedChunkPos,
            ChunkHolder holder,
            CallbackInfo ci
    ) {
        int count = UNLOAD_COUNTS
                .computeIfAbsent(packedChunkPos, ignored -> new AtomicInteger())
                .incrementAndGet();

        if (count == 10
                || count == 100
                || count == 1000
                || count == 10000
                || count % 100000 == 0) {

            ChunkPos pos = new ChunkPos(packedChunkPos);

            LOGGER.error(
                    "[CARNIVAL DIAGNOSTIC] Chunk x={}, z={} scheduled for unload {} times | packed={}",
                    pos.x,
                    pos.z,
                    count,
                    packedChunkPos
            );
        }
    }
}
