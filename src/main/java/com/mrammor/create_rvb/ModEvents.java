package com.mrammor.create_rvb;

import com.mrammor.create_rvb.content.modular_furnace.ModularFurnaceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

@EventBusSubscriber(modid = CreateRebuildVanillaBlocks.MODID)
public class ModEvents {

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel() instanceof Level level && !level.isClientSide()) {
            BlockPos pos = event.getPos();
            BlockState state = event.getPlacedBlock();

            if (state.is(Blocks.FURNACE)) {
                // Пересчитываем структуру от поставленного блока
                updateStructure(level, pos);
            }
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof Level level && !level.isClientSide()) {
            BlockPos pos = event.getPos();
            BlockState state = event.getState();

            if (state.is(Blocks.FURNACE)) {
                // Нам нужно проверить соседей сломанного блока ПОСЛЕ того, как он исчезнет.
                // Запускаем проверку в конце текущего игрового тика
                level.getServer().tell(new net.minecraft.server.TickTask(0, () -> {
                    for (Direction dir : Direction.values()) {
                        BlockPos neighborPos = pos.relative(dir);
                        if (level.getBlockState(neighborPos).is(Blocks.FURNACE)) {
                            updateStructure(level, neighborPos);
                            // Достаточно обновить одну выжившую часть, BFS сам найдет всю остальную группу
                            break; 
                        }
                    }
                }));
            }
        }
    }

    /**
     * Основной метод управления структурой: находит блоки, валидирует форму и раздает/сбрасывает Мастера.
     */
    private static void updateStructure(Level level, BlockPos startPos) {
        // 1. Ищем все соединенные печи с помощью алгоритма BFS
        Set<BlockPos> connectedBlocks = findConnectedFurnaces(level, startPos);

        // 2. Проверяем геометрию по формуле объема идеальной коробки
        if (validatePerfectBoxShape(connectedBlocks)) {
            // Форма идеальна! Находим главного Мастера структуры (минимальный угол)
            BlockPos masterPos = findMasterPosition(connectedBlocks);

            for (BlockPos p : connectedBlocks) {
                if (level.getBlockEntity(p) instanceof ModularFurnaceBlockEntity furnaceBE) {
                    furnaceBE.setMaster(masterPos);
                    // Шлем пакет клиенту, чтобы обновился рендеринг текстур Create
                    level.sendBlockUpdated(p, level.getBlockState(p), level.getBlockState(p), 3);
                }
            }
        } else {
            // Форма нарушена (дыры, выступы буквы Г или Т). Распускаем структуру.
            // Каждый блок забывает Мастера и начинает считать мастером самого себя.
            for (BlockPos p : connectedBlocks) {
                if (level.getBlockEntity(p) instanceof ModularFurnaceBlockEntity furnaceBE) {
                    furnaceBE.setMaster(null);
                    level.sendBlockUpdated(p, level.getBlockState(p), level.getBlockState(p), 3);
                }
            }
        }
    }

    /**
     * Алгоритм BFS (Поиск в ширину) для сбора всех соприкасающихся блоков печей.
     */
    private static Set<BlockPos> findConnectedFurnaces(Level level, BlockPos start) {
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();

        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();

            for (Direction dir : Direction.values()) {
                BlockPos neighbor = current.relative(dir);
                if (!visited.contains(neighbor) && level.getBlockState(neighbor).is(Blocks.FURNACE)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }
        return visited;
    }

    /**
     * ВАЛИДАЦИЯ ПО ФОРМУЛЕ: Сравнение фактического кол-ва блоков с математическим объемом коробки.
     */
    private static boolean validatePerfectBoxShape(Set<BlockPos> blocks) {
        if (blocks == null || blocks.isEmpty()) return false;

        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;

        // Находим крайние точки границ
        for (BlockPos pos : blocks) {
            if (pos.getX() < minX) minX = pos.getX();
            if (pos.getX() > maxX) maxX = pos.getX();
            
            if (pos.getY() < minY) minY = pos.getY();
            if (pos.getY() > maxY) maxY = pos.getY();
            
            if (pos.getZ() < minZ) minZ = pos.getZ();
            if (pos.getZ() > maxZ) maxZ = pos.getZ();
        }

        // Вычисляем стороны параллелепипеда
        int width = (maxX - minX) + 1;
        int height = (maxY - minY) + 1;
        int length = (maxZ - minZ) + 1;

        // Лимиты размеров (например, макс. размер танка 3х3х7, как было заложено в ваших лимитах слоев)
        if (width > 3 || length > 3 || height > 7) return false;

        // Считаем математический объем коробки
        int calculatedVolume = width * height * length;

        // Если фактическое число блоков совпадает с объемом — в структуре нет дыр и лишних хвостов!
        return blocks.size() == calculatedVolume;
    }

    /**
     * Детерминированный поиск позиции Мастера (самый нижний северо-западный угол структуры).
     */
    private static BlockPos findMasterPosition(Set<BlockPos> blocks) {
        BlockPos master = null;
        for (BlockPos pos : blocks) {
            if (master == null) {
                master = pos;
                continue;
            }
            // Сортировка: приоритет по самой нижней высоте (Y), затем север (Z), затем запад (X)
            if (pos.getY() < master.getY() ||
               (pos.getY() == master.getY() && pos.getZ() < master.getZ()) ||
               (pos.getY() == master.getY() && pos.getZ() == master.getZ() && pos.getX() < master.getX())) {
                master = pos;
            }
        }
        return master;
    }
}