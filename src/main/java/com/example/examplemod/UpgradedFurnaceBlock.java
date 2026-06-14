package com.example.examplemod;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class UpgradedFurnaceBlock extends AbstractFurnaceBlock {

    public UpgradedFurnaceBlock(Properties properties) {
        super(properties);
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

        // Если кликнули уже по собранной печи
        if (state.is(ExampleMod.UPGRADED_FURNACE.get())) {
            player.sendSystemMessage(Component.literal("§6Улучшенная печь! GUI скоро..."));
            return InteractionResult.SUCCESS;
        }

        // Пытаемся найти и собрать
        BlockPos furnacePos = findFurnaceInStructure(level, pos);
        if (furnacePos != null && ModularFurnaceValidator.tryAssemble(level, furnacePos)) {
            player.sendSystemMessage(Component.literal("§2✅ Мультиблок успешно собран!"));
        } else {
            player.sendSystemMessage(Component.literal("§c❌ Нужен полный куб 3x3x3 из глины с печью в центре!"));
        }

        return InteractionResult.SUCCESS;
    }

    private BlockPos findFurnaceInStructure(Level level, BlockPos clickedPos) {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos check = clickedPos.offset(x, y, z);
                    if (level.getBlockState(check).is(Blocks.FURNACE)) {
                        return check;
                    }
                }
            }
        }
        return null;
    }

    @Override
    protected void openContainer(Level level, BlockPos pos, Player player) {
        player.sendSystemMessage(Component.literal("§e[TODO] GUI улучшенной печи"));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new UpgradedFurnaceEntity(pos, state);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.is(newState.getBlock())) return;

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof UpgradedFurnaceEntity entity) {
            entity.breakStructure();
        }

        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Items.CLAY, 26));

        super.onRemove(state, level, pos, newState, isMoving);
    }
}