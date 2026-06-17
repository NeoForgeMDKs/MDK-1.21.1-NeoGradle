package com.example.examplemod;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = ExampleMod.MODID)
public class ModEvents {

    @SubscribeEvent
    public static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel().isClientSide()) return;

        var pos = event.getPos();
        var level = (Level) event.getLevel();

        // Проверяем, что поставили блок нашей модульной печи
        if (event.getState().is(ExampleMod.UPGRADED_FURNACE.get())) {
            var connected = ModularFurnaceValidator.findConnectedBlocks(level, pos);
            
            // Оповещаем все блоки структуры о новом размере
            for (BlockPos blockPos : connected) {
                var be = level.getBlockEntity(blockPos);
                if (be instanceof UpgradedFurnaceEntity furnaceEntity) {
                    furnaceEntity.updateDynamicStructure(connected);
                }
            }
        }
    }
    
}