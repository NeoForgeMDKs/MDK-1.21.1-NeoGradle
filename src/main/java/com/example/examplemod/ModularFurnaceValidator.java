package com.example.examplemod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class ModularFurnaceValidator {

    public static boolean validateAndBuild(Level level, BlockPos corePos, Direction facing) {
        // Находим стартовый угол (нижний-левый-передний)
        Direction left = facing.getCounterClockWise();
        BlockPos startLoc = corePos.below(1).relative(left, 1).relative(facing.getOpposite(), 2);

        // ПРОВЕРКА (Сначала сканируем область)
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                for (int z = 0; z < 3; z++) {
                    BlockPos currentPos = startLoc.above(y).relative(left.getOpposite(), x).relative(facing, z);
                    if (currentPos.equals(corePos)) continue;
                    if (!level.getBlockState(currentPos).is(Blocks.CLAY)) return false;
                }
            }
        }

        // ПОСТРОЙКА (Устанавливаем блоки с правильными State)
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                for (int z = 0; z < 3; z++) {
                    BlockPos currentPos = startLoc.above(y).relative(left.getOpposite(), x).relative(facing, z);
                    if (currentPos.equals(corePos)) continue;

                    BlockState partState = ExampleMod.MODULAR_FURNACE_PART.get().defaultBlockState()
                            .setValue(ModularFurnacePartBlock.PART_X, x)
                            .setValue(ModularFurnacePartBlock.PART_Y, y)
                            .setValue(ModularFurnacePartBlock.PART_Z, z)
                            .setValue(ModularFurnacePartBlock.FACING, facing);

                    level.setBlock(currentPos, partState, 3);
                }
            }
        }
        return true;
    }
}