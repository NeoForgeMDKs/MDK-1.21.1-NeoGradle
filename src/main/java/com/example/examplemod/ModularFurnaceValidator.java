package com.example.examplemod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ModularFurnaceValidator {

    public static boolean tryAssemble(Level level, BlockPos furnacePos) {
        if (level.isClientSide) return false;

        BlockState centerState = level.getBlockState(furnacePos);
        if (!centerState.is(Blocks.FURNACE)) {
            return false;
        }

        Direction facing = centerState.getValue(net.minecraft.world.level.block.AbstractFurnaceBlock.FACING);
        Direction left = facing.getCounterClockWise();
        Direction back = facing.getOpposite();

        // Центр = furnacePos
        BlockPos startLoc = furnacePos.below().relative(left, 1).relative(back, 1);

        // === ПРОВЕРКА ===
        boolean structureOk = true;
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                for (int z = 0; z < 3; z++) {
                    BlockPos checkPos = startLoc.above(y)
                            .relative(left.getOpposite(), x)
                            .relative(facing, z);

                    if (checkPos.equals(furnacePos)) continue;

                    if (!level.getBlockState(checkPos).is(Blocks.CLAY)) {
                        structureOk = false;
                    }
                }
            }
        }

        if (!structureOk) {
            return false;
        }

        // === СБОРКА ===
        // Заменяем печь
        BlockState upgradedState = ExampleMod.UPGRADED_FURNACE.get().defaultBlockState()
                .setValue(net.minecraft.world.level.block.AbstractFurnaceBlock.FACING, facing);

        level.setBlock(furnacePos, upgradedState, 3);

        BlockEntity be = level.getBlockEntity(furnacePos);
        if (be instanceof UpgradedFurnaceEntity entity) {
            entity.initStructure(facing);
        }

        // Ставим части
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                for (int z = 0; z < 3; z++) {
                    BlockPos placePos = startLoc.above(y)
                            .relative(left.getOpposite(), x)
                            .relative(facing, z);

                    if (placePos.equals(furnacePos)) continue;

                    BlockState partState = ExampleMod.MODULAR_FURNACE_PART.get().defaultBlockState()
                            .setValue(ModularFurnacePartBlock.FACING, facing)
                            .setValue(ModularFurnacePartBlock.PART_X, x)
                            .setValue(ModularFurnacePartBlock.PART_Y, y)
                            .setValue(ModularFurnacePartBlock.PART_Z, z);

                    level.setBlock(placePos, partState, 3);
                }
            }
        }

        return true;
    }
}