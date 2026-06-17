package com.example.examplemod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.HashSet;
import java.util.Set;

@EventBusSubscriber(modid = ExampleMod.MODID)
public class ModEvents {

    // Вспомогательный метод для поиска всех соединенных блоков печи (сборка мультиблока)
    private static void collectConnectedBlocks(Level level, BlockPos pos, Set<BlockPos> visited) {
        if (visited.size() > 27) return; // Ограничение на размер структуры 3х3х3 (или убери, если нужно выше)
        
        for (Direction dir : Direction.values()) {
            BlockPos next = pos.relative(dir);
            if (!visited.contains(next) && level.getBlockState(next).getBlock() instanceof UpgradedFurnaceBlock) {
                visited.add(next);
                collectConnectedBlocks(level, next, visited);
            }
        }
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel() instanceof Level level && !level.isClientSide()) {
            BlockPos pos = event.getPos();
            BlockState state = event.getPlacedBlock();

            if (state.getBlock() instanceof UpgradedFurnaceBlock) {
                Set<BlockPos> connected = new HashSet<>();
                connected.add(pos);
                collectConnectedBlocks(level, pos, connected);

                // Перебираем все найденные блоки конструкции и обновляем их
                for (BlockPos p : connected) {
                    if (level.getBlockEntity(p) instanceof UpgradedFurnaceEntity furnaceEntity) {
                        
                        // Получаем текущий стейт конкретного блока
                        BlockState blockState = level.getBlockState(p);
                        Direction facing = Direction.NORTH; // Дефолтное значение

                        // Извлекаем FACING, чтобы передать его в логику расчета углов
                        if (blockState.hasProperty(UpgradedFurnaceBlock.FACING)) {
                            facing = blockState.getValue(UpgradedFurnaceBlock.FACING);
                        }

                        // Вызываем обновленный метод с двумя аргументами
                        furnaceEntity.updateDynamicStructure(connected, facing);
                    }
                }
            }
        }
    }
}