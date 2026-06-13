package com.example.examplemod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

public class ModularFurnaceCoreBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty ASSEMBLED = BooleanProperty.create("assembled");

    public ModularFurnaceCoreBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(ASSEMBLED, false));
    }

    @Override
        public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    // Теперь передаем только два параметра!
        return new ModularFurnaceCoreEntity(pos, state);
    }
    
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) { 
            Direction facing = state.getValue(FACING);
            boolean isCurrentlyAssembled = state.getValue(ASSEMBLED);

            if (!isCurrentlyAssembled) {
                if (ModularFurnaceValidator.validateAndBuild(level, pos, facing)) {
                    player.sendSystemMessage(Component.literal("§2Плавильня успешно перестроена!"));
                    level.setBlock(pos, state.setValue(ASSEMBLED, true), 3);
                    
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be instanceof ModularFurnaceCoreEntity coreEntity) {
                        coreEntity.initStructure(facing);
                    }
                } else {
                    player.sendSystemMessage(Component.literal("§cОшибка сборки: Нужен куб 3х3х3 из глины и решетка на крыше."));
                }
            } else {
                player.sendSystemMessage(Component.literal("§eЗдесь будет кастомный GUI с насосом!"));
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ModularFurnaceCoreEntity coreEntity) {
                if (state.getValue(ASSEMBLED)) {
                    coreEntity.breakStructure();
                    Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Items.CLAY, 26));
                    Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Items.IRON_BARS, 1));
                }
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(ASSEMBLED, false);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ASSEMBLED);
    }
}