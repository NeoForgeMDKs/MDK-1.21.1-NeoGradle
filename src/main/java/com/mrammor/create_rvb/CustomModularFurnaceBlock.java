package com.mrammor.create_rvb;

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
        // Подключаем наш тикер к новой блок-сущности
        return createTickerHelper(type, ModBlockEntityTypes.MODULAR_FURNACE.get(), ModularFurnaceBlockEntity::tick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ModularFurnaceBlockEntity furnaceBE) {
            ModularFurnaceBlockEntity master = furnaceBE.getMaster();
            if (master != null) {
                // ИГРА О КЛИКЕ: Если мы кликнули по ЛЮБОЙ части печи, перенаправляем запрос к Мастеру.
                // В будущем здесь будет открытие GUI Мастера:
                // master.openCustomGUI(player);
                player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                    "Клик по структуре! Мастер находится на: " + master.getBlockPos().toShortString()
                ), true);
                return InteractionResult.CONSUME;
            }
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }
}