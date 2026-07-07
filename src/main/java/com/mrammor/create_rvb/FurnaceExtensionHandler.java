package com.mrammor.create_rvb;

import com.mrammor.create_rvb.content.modular_furnace.ModularFurnaceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
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
        if (level.isClientSide) return;

        Player player = event.getEntity();
        InteractionHand hand = event.getHand();
        ItemStack heldItem = player.getItemInHand(hand);

        // 1. Проверяем предмет в руке и блок, по которому кликнули
        if (heldItem.getItem() != Items.FURNACE) return;
        BlockPos clickedPos = event.getPos();
        BlockState clickedState = level.getBlockState(clickedPos);
        if (!clickedState.is(Blocks.FURNACE)) return;

        // 2. Клик должен быть строго по верхней грани
        if (event.getFace() != Direction.UP) return;

        // Направление печи, чтобы новые блоки смотрели туда же
        Direction facing = clickedState.getValue(BlockStateProperties.HORIZONTAL_FACING);

        // 3. Находим самую верхнюю точку ИМЕННО этой печи, чтобы понять текущую высоту стопки
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

        // Защита от превышения лимита бака по высоте (7 блоков)
        if (currentHeight >= 7) return;

        // Находим мастера кликнутого блока, чтобы не заходить на чужую территорию
        BlockPos clickedMaster = null;
        if (level.getBlockEntity(clickedPos) instanceof ModularFurnaceBlockEntity clickedBE) {
            clickedMaster = clickedBE.getMasterPos();
        }

        // 4. Сканируем весь горизонтальный слой на этой максимальной высоте (поиск платформы печей)
        List<BlockPos> currentLayer = new ArrayList<>();
        List<BlockPos> queue = new ArrayList<>();
        BlockPos highestImmutable = highestPos.immutable();
        queue.add(highestImmutable);
        currentLayer.add(highestImmutable);

        // Алгоритм поиска соединенных блоков печи на одном Y-уровне с проверкой мастера
        int index = 0;
        while (index < queue.size()) {
            BlockPos current = queue.get(index++);
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockPos neighbor = current.relative(dir);
                // Проверяем, что сосед — это печь, мы его еще не проверяли и расстояние в пределах структуры (макс 3х3)
                if (level.getBlockState(neighbor).is(Blocks.FURNACE) && !currentLayer.contains(neighbor)) {
                    if (Math.abs(neighbor.getX() - highestImmutable.getX()) < 3 && Math.abs(neighbor.getZ() - highestImmutable.getZ()) < 3) {
                        
                        // ПРОВЕРКА НА ЧУЖОЙ МАСТЕР:
                        if (level.getBlockEntity(neighbor) instanceof ModularFurnaceBlockEntity neighborBE) {
                            BlockPos neighborMaster = neighborBE.getMasterPos();
                            // Если у соседа есть мастер, и он отличается от нашего — это чужая печь стоит впритык! Игнорируем её.
                            if (clickedMaster != null && neighborMaster != null && !clickedMaster.equals(neighborMaster)) {
                                continue; 
                            }
                        }

                        currentLayer.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
        }

        // 5. Проверяем, достаточно ли у игрока печей в руке, чтобы построить ВЕСЬ следующий слой
        int requiredAmount = currentLayer.size();
        if (!player.isCreative() && heldItem.getCount() < requiredAmount) {
            return; // Печей в руке не хватает на целый слой!
        }

        // 6. Проверяем, свободны ли все места уровнем выше над каждым блоком нашей платформы
        for (BlockPos pos : currentLayer) {
            BlockPos targetPos = pos.above();
            if (!level.getBlockState(targetPos).canBeReplaced()) {
                return; // Что-то мешает строительству (например, факел или потолок)
            }
        }

        // 7. Строим новый слой!
        BlockState newFurnaceState = Blocks.FURNACE.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
        for (BlockPos pos : currentLayer) {
            BlockPos targetPos = pos.above();
            level.setBlockAndUpdate(targetPos, newFurnaceState);
            level.levelEvent(2001, targetPos, net.minecraft.world.level.block.Block.getId(newFurnaceState)); // Звук и частицы
        }

        // 8. Списываем блоки из руки
        if (!player.isCreative()) {
            heldItem.shrink(requiredAmount);
        }

        event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
        event.setCanceled(true);
    }
}