package com.example.examplemod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import java.util.HashSet;
import java.util.Set;

public class UpgradedFurnaceEntity extends AbstractFurnaceBlockEntity {

    private final Set<BlockPos> dynamicStructure = new HashSet<>();
    private BlockPos controllerPos = BlockPos.ZERO;
    private boolean isController = false;

    public UpgradedFurnaceEntity(BlockPos pos, BlockState state) {
        super(ExampleMod.UPGRADED_FURNACE_BE.get(), pos, state, RecipeType.SMELTING);
    }

    // Метод для получения главного блока печи (Контроллера)
    public UpgradedFurnaceEntity getController() {
        if (this.isController) return this;
        
        if (this.controllerPos != null && level != null) {
            var be = level.getBlockEntity(this.controllerPos);
            if (be instanceof UpgradedFurnaceEntity controllerEntity) {
                return controllerEntity;
            }
        }
        return this; // Если что-то пошло не так, возвращаем себя
    }

    @Override
    protected Component getDefaultName() {
        return Component.literal("Upgraded Modular Furnace");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory playerInventory) {
        return null; 
    }

    public void updateDynamicStructure(Set<BlockPos> blocks) {
        this.dynamicStructure.clear();
        this.dynamicStructure.addAll(blocks);
        
        if (blocks.isEmpty() || level == null) return;

        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;

        for (BlockPos pos : blocks) {
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());

            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
        }

        this.controllerPos = new BlockPos(minX, minY, minZ);
        this.isController = worldPosition.equals(this.controllerPos);

        int sizeX = maxX - minX + 1;
        int sizeY = maxY - minY + 1;
        int sizeZ = maxZ - minZ + 1;

        BlockState currentState = level.getBlockState(worldPosition);
        Direction facing = currentState.hasProperty(UpgradedFurnaceBlock.FACING) 
                ? currentState.getValue(UpgradedFurnaceBlock.FACING) 
                : Direction.NORTH;

        int localX = 0;
        int localY = Math.min(2, Math.max(0, worldPosition.getY() - minY));
        int localZ = 0;

        switch (facing) {
            case NORTH:
                localX = (worldPosition.getX() == minX) ? 0 : (worldPosition.getX() == maxX ? 2 : 1);
                localZ = (worldPosition.getZ() == minZ) ? 0 : (worldPosition.getZ() == maxZ ? 2 : 1);
                break;
            case SOUTH:
                localX = (worldPosition.getX() == maxX) ? 0 : (worldPosition.getX() == minX ? 2 : 1);
                localZ = (worldPosition.getZ() == maxZ) ? 0 : (worldPosition.getZ() == minZ ? 2 : 1);
                break;
            case WEST:
                localX = (worldPosition.getZ() == maxZ) ? 0 : (worldPosition.getZ() == minX ? 2 : 1);
                localZ = (worldPosition.getX() == minX) ? 0 : (worldPosition.getX() == maxX ? 2 : 1);
                break;
            case EAST:
                localX = (worldPosition.getZ() == minX) ? 0 : (worldPosition.getZ() == maxZ ? 2 : 1);
                localZ = (worldPosition.getX() == maxX) ? 0 : (worldPosition.getX() == minX ? 2 : 1);
                break;
        }

        if (!level.isClientSide) {
            if (currentState.hasProperty(UpgradedFurnaceBlock.PART_X)) {
                BlockState newState = currentState
                        .setValue(UpgradedFurnaceBlock.PART_X, localX)
                        .setValue(UpgradedFurnaceBlock.PART_Y, localY)
                        .setValue(UpgradedFurnaceBlock.PART_Z, localZ);
                
                level.setBlock(worldPosition, newState, 3);
                level.sendBlockUpdated(worldPosition, currentState, newState, 3);
            }
        }
        
        setChanged();
    }

    // Правильный метод передачи данных модели для NeoForge 1.21.1 (внутри сущности!)
    @Override
    public net.neoforged.neoforge.client.model.data.ModelData getModelData() {
        net.neoforged.neoforge.client.model.data.ModelData.Builder builder = net.neoforged.neoforge.client.model.data.ModelData.builder();
        
        if (this.level != null && this.worldPosition != null) {
            builder.with(com.example.examplemod.client.ModularFurnaceCTBehaviour.BLOCK_CONTEXT, 
                new com.example.examplemod.client.ModularFurnaceCTBehaviour.BlockContext(this.level, this.worldPosition));
        }
        
        return builder.build();
    }
}