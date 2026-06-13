package com.example.examplemod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import java.util.ArrayList;
import java.util.List;

public class ModularFurnaceCoreEntity extends BlockEntity {
    private final List<BlockPos> subclassBlocks = new ArrayList<>();

    // ИСПРАВЛЕНИЕ: Убираем первый аргумент BlockEntityType из конструктора!
    // Вместо него передаем в super(...) готовый регистратор из ExampleMod напрямую.
    public ModularFurnaceCoreEntity(BlockPos pos, BlockState state) {
        super(ExampleMod.MODULAR_FURNACE_CORE_BE.get(), pos, state);
    }

    public void initStructure(Direction facing) {
        this.subclassBlocks.clear();
        Direction back = facing.getOpposite();
        Direction left = facing.getClockWise();

        BlockPos startPos = this.worldPosition.below().relative(left, 1).relative(back, 2);

        for (int y = 0; y < 3; y++) {
            for (int l = 0; l < 3; l++) {
                for (int b = 0; b < 3; b++) {
                    BlockPos currentPos = startPos.above(y).relative(left.getOpposite(), l).relative(facing, b);
                    if (!currentPos.equals(this.worldPosition)) {
                        this.subclassBlocks.add(currentPos);
                    }
                }
            }
        }
        setChanged();
    }

    public void breakStructure() {
        if (level == null) return;
        for (BlockPos pos : subclassBlocks) {
            if (level.getBlockState(pos).is(ExampleMod.MODULAR_FURNACE_PART.get())) {
                level.removeBlock(pos, false);
            }
        }
        subclassBlocks.clear();
        setChanged();
    }
}