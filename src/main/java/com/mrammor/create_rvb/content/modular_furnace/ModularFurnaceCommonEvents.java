package com.mrammor.create_rvb.content.modular_furnace;

import com.mrammor.create_rvb.CreateRebuildVanillaBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.*;

@EventBusSubscriber(modid = CreateRebuildVanillaBlocks.MODID)
public class ModularFurnaceCommonEvents {

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel() instanceof Level level && !level.isClientSide()) {
            BlockPos pos = event.getPos();
            BlockState state = event.getPlacedBlock();

            if (state.is(Blocks.FURNACE)) {
                // Пересчитываем структуру для всей области вокруг поставленного блока
                updateStructureInArea(level, pos);
            }
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof Level level && !level.isClientSide()) {
            BlockPos pos = event.getPos();
            if (level.getBlockState(pos).is(Blocks.FURNACE)) {
                // Вместо ожидания, мы сначала изолируем текущую структуру (так как она точно ломается)
                isolateConnectedBlocks(level, pos);
                
                // А затем заставляем все 6 соседних блоков проверить, могут ли они создать новые структуры поменьше
                for (Direction dir : Direction.values()) {
                    BlockPos neighborPos = pos.relative(dir);
                    if (level.getBlockState(neighborPos).is(Blocks.FURNACE)) {
                        updateStructureInArea(level, neighborPos);
                    }
                }
            }
        }
    }

    /**
     * Обновляет структуру мультиблока в текущей области. Назначает Мастера или изолирует блоки.
     */
    private static void updateStructureInArea(Level level, BlockPos triggerPos) {
        Set<BlockPos> structure = calculateTowerStructure(level, triggerPos);

        if (structure != null) {
            // Находим точку Мастера (блок с минимальными координатами X, Y, Z)
            BlockPos masterPos = structure.stream().min(Comparator.comparingLong(BlockPos::asLong)).orElse(triggerPos);

            // Связываем все блоки с этим мастером
            for (BlockPos p : structure) {
                if (level.getBlockEntity(p) instanceof ModularFurnaceBlockEntity be) {
                    be.setMaster(masterPos);
                    // Флаг 7 (1 + 2 + 4) принудительно обновляет блоки на сервере, клиенте и вызывает перерисовку модели
                    level.sendBlockUpdated(p, level.getBlockState(p), level.getBlockState(p), 7); 
                }
            }
        } else {
            // Если структура не прошла валидацию (стал неровный куб или лишний блок), изолируем всю группу
            isolateConnectedBlocks(level, triggerPos);
        }
    }

    /**
     * Сбрасывает Мастера у всех соединённых блоков, превращая их обратно в одиночные печи.
     */
    private static void isolateConnectedBlocks(Level level, BlockPos start) {
        Queue<BlockPos> queue = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();
        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            if (level.getBlockEntity(current) instanceof ModularFurnaceBlockEntity be) {
                be.setMaster(null); // Сбрасываем мастера
                level.sendBlockUpdated(current, level.getBlockState(current), level.getBlockState(current), 7);
            }
            for (Direction dir : Direction.values()) {
                BlockPos neighbor = current.relative(dir);
                if (!visited.contains(neighbor) && level.getBlockState(neighbor).is(Blocks.FURNACE)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }
    }

    /**
     * Алгоритм сканирования и валидации башни в стиле Create.
     * Проверяет основания 1x1, 2x2, 3x3 и высоту до 9 блоков.
     * Возвращает Set всех позиций блоков мультиблока, если структура верна, иначе null.
     */
    private static Set<BlockPos> calculateTowerStructure(Level level, BlockPos startPos) {
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();

        int minX = startPos.getX(), maxX = startPos.getX();
        int minY = startPos.getY(), maxY = startPos.getY();
        int minZ = startPos.getZ(), maxZ = startPos.getZ();

        queue.add(startPos);
        visited.add(startPos);

        // 1. Поиск всех связанных блоков печей во всех направлениях
        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();

            // Динамически расширяем виртуальные границы Bounding Box
            if (current.getX() < minX) minX = current.getX();
            if (current.getX() > maxX) maxX = current.getX();
            if (current.getY() < minY) minY = current.getY();
            if (current.getY() > maxY) maxY = current.getY();
            if (current.getZ() < minZ) minZ = current.getZ();
            if (current.getZ() > maxZ) maxZ = current.getZ();

            // Если выходим за рамки ограничений Create башни, сразу прерываемся
            if (maxX - minX + 1 > 3 || maxZ - minZ + 1 > 3 || maxY - minY + 1 > 9) {
                return null; 
            }

            for (Direction dir : Direction.values()) {
                BlockPos neighbor = current.relative(dir);
                if (!visited.contains(neighbor) && level.getBlockState(neighbor).is(Blocks.FURNACE)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        // 2. Итоговые размеры
        int sizeX = maxX - minX + 1;
        int sizeY = maxY - minY + 1;
        int sizeZ = maxZ - minZ + 1;

        // 3. Проверка пропорций основания (Должен быть строгий квадрат 1x1, 2x2 или 3x3)
        if (sizeX != sizeZ) {
            return null; 
        }

        // 4. Проверка ограничений высоты
        if (sizeY > 9) {
            return null; 
        }

        // 5. Проверка на монолитность (Объем заполненных блоков должен идеально совпадать с коробкой)
        int expectedVolume = sizeX * sizeZ * sizeY;
        if (visited.size() != expectedVolume) {
            return null; 
        }

        return visited;
    }
}