package com.mrammor.create_rvb;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@EventBusSubscriber(modid = CreateRebuildVanillaBlocks.MODID)
public class ModEvents {

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel() instanceof Level level && !level.isClientSide()) {
            BlockPos pos = event.getPos();
            BlockState state = event.getPlacedBlock();

            if (state.is(Blocks.FURNACE)) {
                // Рассчитываем структуру мультиблока при установке новой печи
                Set<BlockPos> connected = calculateTankStructure(level, pos);
                
                // В будущем здесь можно уведомлять Мастер-блок о том, что структура изменилась:
                // updateMasterBlockEntity(level, connected);
            }
        }
    }

    // Безопасный не-рекурсивный метод сканирования горизонтального слоя печей
    private static void scanHorizontalLayer(Level level, BlockPos startPos, Set<BlockPos> layer) {
        List<BlockPos> queue = new ArrayList<>();
        queue.add(startPos);
        layer.add(startPos);

        int index = 0;
        while (index < queue.size()) {
            BlockPos current = queue.get(index++);
            if (layer.size() > 9) return; // Ограничение на размер слоя (макс 3х3 = 9 блоков)

            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockPos next = current.relative(dir);
                if (!layer.contains(next) && level.getBlockState(next).is(Blocks.FURNACE)) {
                    layer.add(next);
                    queue.add(next);
                }
            }
        }
    }

    private static boolean validateLayerLimits(Set<BlockPos> layer) {
        if (layer.isEmpty() || layer.size() > 9) return false;
        
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
        
        for (BlockPos p : layer) {
            if (p.getX() < minX) minX = p.getX();
            if (p.getX() > maxX) maxX = p.getX();
            if (p.getZ() < minZ) minZ = p.getZ();
            if (p.getZ() > maxZ) maxZ = p.getZ();
        }
        
        return (maxX - minX < 3) && (maxZ - minZ < 3);
    }

    private static boolean isLayerMatchingPattern(Set<BlockPos> baseLayer, Set<BlockPos> currentLayer, int yOffset) {
        if (baseLayer.size() != currentLayer.size()) return false;
        for (BlockPos basePos : baseLayer) {
            if (!currentLayer.contains(basePos.above(yOffset))) {
                return false;
            }
        }
        return true;
    }

    private static Set<BlockPos> calculateTankStructure(Level level, BlockPos start) {
        BlockPos bottomPos = start;
        while (level.getBlockState(bottomPos.below()).is(Blocks.FURNACE)) {
            bottomPos = bottomPos.below();
        }
        
        Set<BlockPos> baseLayer = new HashSet<>();
        scanHorizontalLayer(level, bottomPos, baseLayer);
        
        Set<BlockPos> total = new HashSet<>();
        if (!validateLayerLimits(baseLayer)) {
            total.add(start);
            return total;
        }

        for (int y = 0; y < 7; y++) {
            Set<BlockPos> currentLayer = new HashSet<>();
            scanHorizontalLayer(level, bottomPos.above(y), currentLayer);
            if (isLayerMatchingPattern(baseLayer, currentLayer, y)) {
                total.addAll(currentLayer);
            } else {
                break;
            }
        }
        return total;
    }
}