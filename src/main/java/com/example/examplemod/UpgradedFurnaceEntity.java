package com.example.examplemod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;

public class UpgradedFurnaceEntity extends AbstractFurnaceBlockEntity {

    private BlockPos controllerPos; // Храним позицию главного блока мультиблока

    public UpgradedFurnaceEntity(BlockPos pos, BlockState state) {
        // ExampleMod.UPGRADED_FURNACE_BE должен указывать на твой DeferredRegister/DeferredHolder сущности
        super(ExampleMod.UPGRADED_FURNACE_BE.get(), pos, state, RecipeType.SMELTING);
    }

    @Override
    protected net.minecraft.network.chat.Component getDefaultName() {
        return net.minecraft.network.chat.Component.literal("Upgraded Furnace");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return null; // Сюда потом привяжешь свой контейнер для GUI
    }

    public UpgradedFurnaceEntity getController() {
        if (level != null && controllerPos != null && level.getBlockEntity(controllerPos) instanceof UpgradedFurnaceEntity be) {
            return be;
        }
        return this;
    }

    public void setControllerPos(BlockPos pos) {
        this.controllerPos = pos;
        this.setChanged();
    }

    /**
     * Основной метод обновления структуры.
     * Расчитывает размеры, слои и передает данные в блокстейт.
     */
    public void updateDynamicStructure(Set<BlockPos> structureBlocks, Direction mainFacing) {
        if (level == null || level.isClientSide) return;

        // 1. Находим физические границы текущей построенной коробки
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;

        for (BlockPos p : structureBlocks) {
            minX = Math.min(minX, p.getX()); maxX = Math.max(maxX, p.getX());
            minY = Math.min(minY, p.getY()); maxY = Math.max(maxY, p.getY());
            minZ = Math.min(minZ, p.getZ()); maxZ = Math.max(maxZ, p.getZ());
        }

        // 2. Проверяем слои (работает как для 1 слоя, так и для бесконечной высоты)
        boolean isBottom = (worldPosition.getY() == minY);
        boolean isTop = (worldPosition.getY() == maxY);

        // 3. Вычисляем часть мультиблока по твоей схеме моделей
        FurnacePart part = calculatePart(mainFacing, worldPosition, minX, maxX, minZ, maxZ);

        // 4. Получаем старый стейт и сравниваем с новым, чтобы не спамить обновлениями пакетов
        BlockState currentState = getBlockState();
        BlockState newState = currentState
                .setValue(UpgradedFurnaceBlock.FACING, mainFacing)
                .setValue(UpgradedFurnaceBlock.BOTTOM, isBottom)
                .setValue(UpgradedFurnaceBlock.TOP, isTop)
                .setValue(UpgradedFurnaceBlock.PART, part);

        if (currentState != newState) {
            level.setBlock(worldPosition, newState, 3); // Флаг 3 обновляет блок и на клиенте, и на сервере
        }
    }

    /**
     * Математический адаптер под твою сетку моделей:
     * Перед: [Left (2)]  [Mid (3)]  [Right (1)]
     * Середина: [Mid (3)]  [Center]   [Mid (3)]
     * Зад:  [Right (1)] [Mid (3)]  [Left (2)]
     */
    private FurnacePart calculatePart(Direction facing, BlockPos pos, int minX, int maxX, int minZ, int maxZ) {
        // Превращаем мировые координаты в относительную сетку (0 - край минимума, 2 - край максимума, 1 - между ними)
        int gridX = (pos.getX() == minX) ? 0 : (pos.getX() == maxX ? 2 : 1);
        int gridZ = (pos.getZ() == minZ) ? 0 : (pos.getZ() == maxZ ? 2 : 1);

        int localX = 1; 
        int localZ = 1;

        // Разворачиваем координатную сетку в зависимости от взгляда печи (FACING)
        switch (facing) {
            case NORTH -> { localX = gridX; localZ = gridZ; }
            case SOUTH -> { localX = 2 - gridX; localZ = 2 - gridZ; }
            case WEST  -> { localX = 2 - gridZ; localZ = gridX; }
            case EAST  -> { localX = gridZ; localZ = 2 - gridX; }
        }

        // Если это внутренности или центральный столб
        if (localX == 1 && localZ == 1) {
            return FurnacePart.CENTER;
        }

        // ПЕРЕДНЯЯ ЛИНИЯ (Ближе всего к лицу печи)
        if (localZ == 0) {
            if (localX == 0) return FurnacePart.FRONT_RIGHT;  // Было FRONT_LEFT
            if (localX == 2) return FurnacePart.FRONT_LEFT;   // Было FRONT_RIGHT
            return FurnacePart.FRONT_MID;
        } 
        // ЗАДНЯЯ ЛИНИЯ (Противоположная сторона)
        else if (localZ == 2) {
            if (localX == 0) return FurnacePart.BACK_LEFT;    // Было BACK_RIGHT
            if (localX == 2) return FurnacePart.BACK_RIGHT;   // Было BACK_LEFT
            return FurnacePart.BACK_MID;
        } 
        // БОКОВЫЕ СТЕНЫ (Середина по оси Z)
        else {
            if (localX == 0) return FurnacePart.MID_RIGHT;    // Было MID_LEFT
            if (localX == 2) return FurnacePart.MID_LEFT;     // Было MID_RIGHT
            return FurnacePart.CENTER;
        }
    }
}