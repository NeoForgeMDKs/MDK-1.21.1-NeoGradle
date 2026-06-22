package com.mrammor.create_rvb.content.modular_furnace;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.*;

public class ModularFurnaceConnectivityHandler {

    // Пытаемся собрать структуру
    public static void formStructure(Level level, BlockPos triggerPos) {
        Set<BlockPos> structure = calculateTowerStructure(level, triggerPos);

        if (structure != null) {
            // Если собралась башня — берем блок с мин. координатами как Мастера
            BlockPos masterPos = structure.stream().min(Comparator.comparingLong(BlockPos::asLong)).orElse(triggerPos);
            for (BlockPos p : structure) {
                if (level.getBlockEntity(p) instanceof ModularFurnaceBlockEntity be) {
                    be.setMaster(masterPos);
                }
            }
        } else {
            // Если это лишний "кривой" блок, он становится одиночным
            if (level.getBlockEntity(triggerPos) instanceof ModularFurnaceBlockEntity be) {
                be.setMaster(null);
            }
        }
    }

    // Если блок ломают — разрываем связь для всех его соседей по структуре
    public static void splitStructure(Level level, BlockPos brokenPos) {
        BlockEntity brokenBE = level.getBlockEntity(brokenPos);
        if (!(brokenBE instanceof ModularFurnaceBlockEntity be)) return;
        
        if (!be.hasMaster()) return; // Если блок был одиночным, структура не ломается
        
        BlockPos masterPos = be.getMasterPos();

        // Наша башня максимум 3х3х9. Очищаем мастеров в этой зоне ОЧЕНЬ быстро (O(1))
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 9; y++) {
                for (int z = 0; z < 3; z++) {
                    BlockPos p = masterPos.offset(x, y, z);
                    if (level.getBlockEntity(p) instanceof ModularFurnaceBlockEntity furnace) {
                        if (masterPos.equals(furnace.getMasterPos())) {
                            furnace.setMaster(null);
                        }
                    }
                }
            }
        }

        // Заставляем соседние блоки попробовать собраться заново (если остался слой 2х2)
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = brokenPos.relative(dir);
            if (level.getBlockState(neighbor).is(Blocks.FURNACE)) {
                formStructure(level, neighbor);
            }
        }
    }

    private static Set<BlockPos> calculateTowerStructure(Level level, BlockPos startPos) {
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();

        int minX = startPos.getX(), maxX = startPos.getX();
        int minY = startPos.getY(), maxY = startPos.getY();
        int minZ = startPos.getZ(), maxZ = startPos.getZ();

        queue.add(startPos);
        visited.add(startPos);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();

            if (current.getX() < minX) minX = current.getX();
            if (current.getX() > maxX) maxX = current.getX();
            if (current.getY() < minY) minY = current.getY();
            if (current.getY() > maxY) maxY = current.getY();
            if (current.getZ() < minZ) minZ = current.getZ();
            if (current.getZ() > maxZ) maxZ = current.getZ();

            if (maxX - minX + 1 > 3 || maxZ - minZ + 1 > 3 || maxY - minY + 1 > 9) return null;

            for (Direction dir : Direction.values()) {
                BlockPos neighbor = current.relative(dir);
                if (!visited.contains(neighbor) && level.getBlockState(neighbor).is(Blocks.FURNACE)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        int sizeX = maxX - minX + 1;
        int sizeY = maxY - minY + 1;
        int sizeZ = maxZ - minZ + 1;

        if (sizeX != sizeZ || sizeY > 9) return null;
        if (visited.size() != sizeX * sizeZ * sizeY) return null;

        return visited;
    }
}