package com.mrammor.create_rvb.content.modular_furnace;

import com.mrammor.create_rvb.registry.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
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
     */
    public ModularFurnaceBlockEntity getMaster() {
        if (level == null) return this;
        
        if (masterPos == null) {
            return this; // Если структуры нет, блок является Мастером для самого себя
        }
        
        BlockEntity be = level.getBlockEntity(masterPos);
        if (be instanceof ModularFurnaceBlockEntity masterBE) {
            return masterBE;
        }
        
        return this;
    }

    public void setMaster(BlockPos pos) {
        this.masterPos = pos;
        setChanged(); // Сохраняем на диск чанка
    }

    public BlockPos getMasterPos() {
        return this.masterPos;
    }

    // --- СОХРАНЕНИЕ И ЗАГРУЗКА ДАННЫХ (NBT) ---

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (masterPos != null) {
            tag.put("MasterPos", NbtUtils.writeBlockPos(masterPos));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("MasterPos")) {
            this.masterPos = NbtUtils.readBlockPos(tag, "MasterPos").orElse(null);
        } else {
            this.masterPos = null;
        }
    }

    // --- СЕТЕВАЯ СИНХРОНИЗАЦИЯ С КЛИЕНТОМ (ОЧЕНЬ ВАЖНО ДЛЯ CREATE) ---

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    /**
     * Игровой тикер (метод, вызываемый каждый игровой такт).
     */
    public static void tick(Level level, BlockPos pos, BlockState state, ModularFurnaceBlockEntity blockEntity) {
        // Логику плавки внутри Мастера будем описывать здесь позже.
    }
}