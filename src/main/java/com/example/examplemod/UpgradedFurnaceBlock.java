package com.example.examplemod;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import java.util.Set;

public class UpgradedFurnaceBlock extends AbstractFurnaceBlock {

    // Свойства для наложения моделей из BlockState JSON
    public static final IntegerProperty PART_X = IntegerProperty.create("part_x", 0, 2);
    public static final IntegerProperty PART_Y = IntegerProperty.create("part_y", 0, 2);
    public static final IntegerProperty PART_Z = IntegerProperty.create("part_z", 0, 2);

    public UpgradedFurnaceBlock(Properties properties) {
        super(properties);
        // Задаем начальное состояние: блок выглядит как центр фасада (1, 1, 0)
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LIT, false)
                .setValue(PART_X, 1)
                .setValue(PART_Y, 1)
                .setValue(PART_Z, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        // Добавляем LIT вместе со всеми свойствами мультиблока
        builder.add(FACING, BlockStateProperties.LIT, PART_X, PART_Y, PART_Z);
    }

    @Override
    protected MapCodec<? extends AbstractFurnaceBlock> codec() {
        return simpleCodec(UpgradedFurnaceBlock::new);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        Set<BlockPos> connected = ModularFurnaceValidator.findConnectedBlocks(level, pos);
        
        if (!connected.isEmpty()) {
            for (BlockPos blockPos : connected) {
                BlockEntity be = level.getBlockEntity(blockPos);
                if (be instanceof UpgradedFurnaceEntity furnaceEntity) {
                    furnaceEntity.updateDynamicStructure(connected);
                }
            }
            player.sendSystemMessage(Component.literal("§2✅ Модульная печь активна! Блоков: " + connected.size()));
        } else {
            player.sendSystemMessage(Component.literal("§c❌ Ошибка структуры!"));
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected void openContainer(Level level, BlockPos pos, Player player) {
        player.sendSystemMessage(Component.literal("§e[TODO] Скоро здесь будет кастомный GUI!"));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new UpgradedFurnaceEntity(pos, state);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.is(newState.getBlock())) return;

        super.onRemove(state, level, pos, newState, isMoving);
        
        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            BlockEntity be = level.getBlockEntity(neighborPos);
            if (be instanceof UpgradedFurnaceEntity furnaceEntity) {
                Set<BlockPos> remainingBlocks = ModularFurnaceValidator.findConnectedBlocks(level, neighborPos);
                furnaceEntity.updateDynamicStructure(remainingBlocks);
            }
        }
    }

    @Override
    public <T extends BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T> getTicker(Level level, BlockState state, net.minecraft.world.level.block.entity.BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ExampleMod.UPGRADED_FURNACE_BE.get(), UpgradedFurnaceEntity::serverTick);
    }
}