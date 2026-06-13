package com.example.examplemod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class ModularFurnacePartBlock extends Block {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final IntegerProperty PART_X = IntegerProperty.create("part_x", 0, 2);
    public static final IntegerProperty PART_Y = IntegerProperty.create("part_y", 0, 2);
    public static final IntegerProperty PART_Z = IntegerProperty.create("part_z", 0, 2);

    public ModularFurnacePartBlock(Properties properties) {
        super(properties);
        // Устанавливаем дефолтное состояние, чтобы игра не крашилась при загрузке
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PART_X, 0)
                .setValue(PART_Y, 0)
                .setValue(PART_Z, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        // ОЧЕНЬ ВАЖНО: Если тут не перечислить все свойства, JSON их не увидит
        builder.add(FACING, PART_X, PART_Y, PART_Z);
    }
}