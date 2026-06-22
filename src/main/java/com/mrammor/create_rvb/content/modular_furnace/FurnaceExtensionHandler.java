package com.mrammor.create_rvb.content.modular_furnace;

import com.mrammor.create_rvb.CreateRebuildVanillaBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = CreateRebuildVanillaBlocks.MODID)
public class FurnaceExtensionHandler {

    @SubscribeEvent
    public static void onBlockRightClick(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos clickedPos = event.getPos();
        BlockState clickedState = level.getBlockState(clickedPos);
        
        if (!clickedState.is(Blocks.FURNACE)) return;

        Player player = event.getEntity();
        ItemStack heldItem = event.getItemStack();

        if (heldItem.is(Blocks.FURNACE.asItem()) && !player.isShiftKeyDown()) {
            if (event.getFace() != Direction.UP) {
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
                return;
            }
        }

        if (level.isClientSide || heldItem.getItem() != Items.FURNACE || event.getFace() != Direction.UP) return;

        Direction facing = clickedState.getValue(BlockStateProperties.HORIZONTAL_FACING);

        BlockPos.MutableBlockPos highestPos = clickedPos.mutable();
        while (level.getBlockState(highestPos.above()).is(Blocks.FURNACE)) {
            highestPos.move(Direction.UP);
        }
        
        int currentHeight = 1;
        BlockPos.MutableBlockPos checkDown = highestPos.mutable().move(Direction.DOWN);
        while (level.getBlockState(checkDown).is(Blocks.FURNACE)) {
            currentHeight++;
            checkDown.move(Direction.DOWN);
        }

        if (currentHeight >= 7) return;

        List<BlockPos> currentLayer = new ArrayList<>();
        List<BlockPos> queue = new ArrayList<>();
        BlockPos highestImmutable = highestPos.immutable();
        
        queue.add(highestImmutable);
        currentLayer.add(highestImmutable);

        int index = 0;
        while (index < queue.size()) {
            BlockPos current = queue.get(index++);
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockPos neighbor = current.relative(dir);
                BlockState neighborState = level.getBlockState(neighbor);
                
                if (neighborState.is(Blocks.FURNACE) && !currentLayer.contains(neighbor)) {
                    if (neighborState.getValue(BlockStateProperties.HORIZONTAL_FACING) == facing) {
                        if (Math.abs(neighbor.getX() - highestImmutable.getX()) < 3 && Math.abs(neighbor.getZ() - highestImmutable.getZ()) < 3) {
                            currentLayer.add(neighbor);
                            queue.add(neighbor);
                        }
                    }
                }
            }
        }

        int requiredAmount = currentLayer.size();
        if (!player.isCreative() && heldItem.getCount() < requiredAmount) return;

        for (BlockPos pos : currentLayer) {
            if (!level.getBlockState(pos.above()).canBeReplaced()) return;
        }

        BlockState newFurnaceState = Blocks.FURNACE.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
        for (BlockPos pos : currentLayer) {
            BlockPos targetPos = pos.above();
            level.setBlockAndUpdate(targetPos, newFurnaceState);
            level.levelEvent(2001, targetPos, net.minecraft.world.level.block.Block.getId(newFurnaceState));
        }

        if (!player.isCreative()) {
            heldItem.shrink(requiredAmount);
        }

        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }
}