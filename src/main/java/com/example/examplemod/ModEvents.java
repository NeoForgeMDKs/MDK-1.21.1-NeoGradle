package com.example.examplemod;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = ExampleMod.MODID)
public class ModEvents {

    @SubscribeEvent
    public static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel().isClientSide()) return;

        var pos = event.getPos();
        var level = (Level) event.getLevel();   // ← Вот исправление

        // Проверяем только если поставили глину или печь
        if (event.getState().is(Blocks.CLAY) || event.getState().is(Blocks.FURNACE)) {
            for (int x = -2; x <= 2; x++) {
                for (int y = -2; y <= 2; y++) {
                    for (int z = -2; z <= 2; z++) {
                        var checkPos = pos.offset(x, y, z);

                        if (level.getBlockState(checkPos).is(Blocks.FURNACE)) {
                            if (ModularFurnaceValidator.tryAssemble(level, checkPos)) {
                                event.getEntity().sendSystemMessage(
                                    net.minecraft.network.chat.Component.literal("§2✅ Мультиблок автоматически собран!")
                                );
                                return;
                            }
                        }
                    }
                }
            }
        }
    }
}