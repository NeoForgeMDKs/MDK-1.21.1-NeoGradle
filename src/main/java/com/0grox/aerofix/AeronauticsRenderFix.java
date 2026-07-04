package com.ogstudios.aerofix;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = AeronauticsRenderFix.MODID)
public class AeronauticsRenderFix {
    public static final String MODID = "aerofix";
    private int tickCounter = 0;
    
    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        
        AeronauticsRenderFix instance = getInstance();
        instance.tickCounter++;
        if (instance.tickCounter < 40) return; // Перевірка кожні 2 секунди (40 тіків)
        instance.tickCounter = 0;

        for (ServerPlayer player : level.players()) {
            BlockPos playerPos = player.blockPosition();
            int radius = 48; // Радіус оновлення блоків навколо гравця
            
            BlockPos.betweenClosedStream(
                playerPos.offset(-radius, -10, -radius),
                playerPos.offset(radius, 10, radius)
            ).filter(pos -> !level.isEmptyBlock(pos))
             .limit(20) // Оновлюємо по 20 блоків за раз, щоб уникнути лагів
             .forEach(pos -> player.connection.send(new ClientboundBlockUpdatePacket(level, pos)));
        }
    }

    private static AeronauticsRenderFix INSTANCE;
    private static AeronauticsRenderFix getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AeronauticsRenderFix();
        }
        return INSTANCE;
    }
}