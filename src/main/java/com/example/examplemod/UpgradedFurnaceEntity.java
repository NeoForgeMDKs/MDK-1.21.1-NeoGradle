package com.example.examplemod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import java.util.ArrayList;
import java.util.List;

public class UpgradedFurnaceEntity extends BlockEntity {

    // Список позиций всех частей мультиблока
    private final List<BlockPos> structureParts = new ArrayList<>();

    public UpgradedFurnaceEntity(BlockPos pos, BlockState state) {
        super(ExampleMod.UPGRADED_FURNACE_BE.get(), pos, state);
    }

    /**
     * Сохраняем позиции всех частей структуры
     */
    public void initStructure(Direction facing) {
        structureParts.clear();

        Direction left = facing.getCounterClockWise();
        Direction back = facing.getOpposite();

        // Начальная точка для 3x3x3 (нижний задний-левый угол)
        BlockPos startLoc = worldPosition.below().relative(left, 1).relative(back, 1);

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                for (int z = 0; z < 3; z++) {
                    BlockPos partPos = startLoc.above(y)
                            .relative(left.getOpposite(), x)
                            .relative(facing, z);

                    if (!partPos.equals(worldPosition)) {
                        structureParts.add(partPos);
                    }
                }
            }
        }
        setChanged(); // Сообщаем, что данные изменились
    }

    /**
     * Разбираем структуру при разрушении
     */
    public void breakStructure() {
        if (level == null) return;

        for (BlockPos pos : structureParts) {
            BlockState state = level.getBlockState(pos);
            if (state.is(ExampleMod.MODULAR_FURNACE_PART.get())) {
                level.removeBlock(pos, false); // Удаляем без дропа (материалы дропнем из ядра)
            }
        }
        structureParts.clear();
        setChanged();
    }

    // TODO: сюда потом добавим логику плавки, рецепты, прогресс и т.д.
}