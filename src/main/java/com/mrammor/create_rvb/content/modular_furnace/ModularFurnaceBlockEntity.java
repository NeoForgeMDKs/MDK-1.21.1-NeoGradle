package com.mrammor.create_rvb.content.modular_furnace;

import com.mrammor.create_rvb.registry.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ModularFurnaceBlockEntity extends BlockEntity {
    
    // Позиция главного блока структуры (Мастера), управляющего логикой
    private BlockPos masterPos = null;

    public ModularFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.MODULAR_FURNACE.get(), pos, state);
    }

    /**
     * Возвращает сущность Мастера для этой печи. 
     * Если блок сам является мастером или структура ещё не инициализирована, возвращает себя.
     */
    public ModularFurnaceBlockEntity getMaster() {
        if (level == null) return this;
        
        if (masterPos == null) {
            return this; // Пока нет структуры, этот блок считает мастером самого себя
        }
        
        BlockEntity be = level.getBlockEntity(masterPos);
        if (be instanceof ModularFurnaceBlockEntity masterBE) {
            return masterBE;
        }
        
        return this;
    }

    public void setMaster(BlockPos pos) {
        this.masterPos = pos;
        setChanged(); // Маркируем блок как измененный для сохранения данных
    }

    /**
     * Игровой тикер (метод, вызываемый каждый игровой такт).
     * Сюда мы позже перенесем переплавку, потребление топлива и генерацию тепла.
     */
    public static void tick(Level level, BlockPos pos, BlockState state, ModularFurnaceBlockEntity blockEntity) {
        if (level.isClientSide) return;

        // Тут будет крутиться логика работы многоблочной печи
    }
}