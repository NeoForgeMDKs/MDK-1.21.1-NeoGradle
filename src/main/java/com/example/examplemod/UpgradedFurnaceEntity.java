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

    private BlockPos controllerPos;

    public UpgradedFurnaceEntity(BlockPos pos, BlockState state) {
        super(ExampleMod.UPGRADED_FURNACE_BE.get(), pos, state, RecipeType.SMELTING);
    }

    @Override
    protected net.minecraft.network.chat.Component getDefaultName() {
        return net.minecraft.network.chat.Component.literal("Upgraded Furnace");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) { return null; }

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

    public void updateDynamicStructure(Set<BlockPos> structureBlocks, Direction mainFacing) {
        if (level == null || level.isClientSide) return;

        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;

        for (BlockPos p : structureBlocks) {
            minX = Math.min(minX, p.getX()); maxX = Math.max(maxX, p.getX());
            minY = Math.min(minY, p.getY()); maxY = Math.max(maxY, p.getY());
            minZ = Math.min(minZ, p.getZ()); maxZ = Math.max(maxZ, p.getZ());
        }

        boolean isBottom = (worldPosition.getY() == minY);
        boolean isTop = (worldPosition.getY() == maxY);

        FurnacePart part = calculatePart(mainFacing, worldPosition, minX, maxX, minZ, maxZ);

        BlockState currentState = getBlockState();
        BlockState newState = currentState
                .setValue(UpgradedFurnaceBlock.FACING, mainFacing)
                .setValue(UpgradedFurnaceBlock.BOTTOM, isBottom)
                .setValue(UpgradedFurnaceBlock.TOP, isTop)
                .setValue(UpgradedFurnaceBlock.PART, part);

        if (currentState != newState) {
            level.setBlock(worldPosition, newState, 3);
        }
    }

    private FurnacePart calculatePart(Direction facing, BlockPos pos, int minX, int maxX, int minZ, int maxZ) {
        int gridX = (pos.getX() == minX) ? 0 : (pos.getX() == maxX ? 2 : 1);
        int gridZ = (pos.getZ() == minZ) ? 0 : (pos.getZ() == maxZ ? 2 : 1);

        int localX = 1; 
        int localZ = 1;

        switch (facing) {
            case NORTH -> { localX = gridX; localZ = gridZ; }
            case SOUTH -> { localX = 2 - gridX; localZ = 2 - gridZ; }
            case WEST  -> { localX = 2 - gridZ; localZ = gridX; }
            case EAST  -> { localX = gridZ; localZ = 2 - gridX; }
        }

        // Если это внутренности мультиблока
        if (localX == 1 && localZ == 1) {
            return FurnacePart.CENTER;
        }

        // ПЕРЕДНЯЯ ПАНЕЛЬ (Лицо печи)
        if (localZ == 0) {
            if (localX == 0) return FurnacePart.FRONT_LEFT;
            if (localX == 2) return FurnacePart.FRONT_RIGHT;
            return FurnacePart.FRONT_MID;
        } 
        // ЗАДНЯЯ ПАНЕЛЬ
        else if (localZ == 2) {
            if (localX == 0) return FurnacePart.BACK_LEFT;
            if (localX == 2) return FurnacePart.BACK_RIGHT;
            return FurnacePart.BACK_MID;
        } 
        // БОКОВИНЫ (Вот тут они часто ломались или возвращали CENTER)
        else {
            if (localX == 0) return FurnacePart.MID_CENTER_LEFT;  // Левая стена
            if (localX == 2) return FurnacePart.MID_CENTER_RIGHT; // Правая стена
            return FurnacePart.CENTER;
        }
    }
}