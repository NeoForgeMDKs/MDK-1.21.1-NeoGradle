package com.example.examplemod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class ModularFurnaceValidator {

    // Ограничиваем максимальный размер печи (например, 3x3x3 = 27 блоков)
    private static final int MAX_SIZE = 3; 

    public static Set<BlockPos> findConnectedBlocks(Level level, BlockPos startPos) {
        Set<BlockPos> connected = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();

        BlockState startState = level.getBlockState(startPos);
        if (!startState.is(ExampleMod.UPGRADED_FURNACE.get())) {
            return connected; // Возвращаем пустой сет
        }

        queue.add(startPos);
        connected.add(startPos);

        // 1. Собираем все соединенные блоки нашей печи (Flood fill)
        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            
            // Если блоков больше 27, прерываем поиск — структура слишком большая
            if (connected.size() > MAX_SIZE * MAX_SIZE * MAX_SIZE) return new HashSet<>();

            for (Direction direction : Direction.values()) {
                BlockPos neighbor = current.relative(direction);
                if (!connected.contains(neighbor)) {
                    BlockState neighborState = level.getBlockState(neighbor);
                    if (neighborState.is(ExampleMod.UPGRADED_FURNACE.get())) {
                        connected.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
        }

        // 2. Проверяем, образуют ли найденные блоки ИДЕАЛЬНЫЙ куб/параллелепипед
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;

        for (BlockPos pos : connected) {
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
        }

        int widthX = maxX - minX + 1;
        int height = maxY - minY + 1;
        int widthZ = maxZ - minZ + 1;

        // Если размеры превышают лимит (например, 4x3x3), отменяем сборку
        if (widthX > MAX_SIZE || height > MAX_SIZE || widthZ > MAX_SIZE) {
            return new HashSet<>();
        }

        // Проверяем, что куб сплошной (объем куба равен количеству найденных блоков)
        int expectedVolume = widthX * height * widthZ;
        if (connected.size() != expectedVolume) {
            return new HashSet<>(); // Есть дыры или торчащие лишние блоки
        }

        return connected; // Всё идеально! Возвращаем сет для сборки
    }
}