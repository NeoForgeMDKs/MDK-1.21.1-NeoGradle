package com.mrammor.create_rvb.content.modular_furnace;

import com.mrammor.create_rvb.registry.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class CustomModularFurnaceBlock extends FurnaceBlock {

    public CustomModularFurnaceBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ModularFurnaceBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntityTypes.MODULAR_FURNACE.get(), ModularFurnaceBlockEntity::tick);
    }

    // --- ЛОГИКА CREATE: Мгновенное обновление при установке ---
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (state.getBlock() != oldState.getBlock()) {
            ModularFurnaceConnectivityHandler.formStructure(level, pos);
        }
    }

    // --- ЛОГИКА CREATE: Сброс структуры при поломке ---
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            ModularFurnaceConnectivityHandler.splitStructure(level, pos);
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
}