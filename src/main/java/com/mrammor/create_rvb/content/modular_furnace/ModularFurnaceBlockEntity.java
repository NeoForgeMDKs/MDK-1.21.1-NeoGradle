package com.mrammor.create_rvb.content.modular_furnace;

import com.mrammor.create_rvb.registry.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ModularFurnaceBlockEntity extends BlockEntity {

    private BlockPos masterPos = null;

    public ModularFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.MODULAR_FURNACE.get(), pos, state);
    }

    // ВОЗВРАЩАЕМ МЕТОД TICK ДЛЯ ТИКЕРА БЛОКА
    public static void tick(Level level, BlockPos pos, BlockState state, ModularFurnaceBlockEntity blockEntity) {
        // Здесь в будущем будет общая плавка предметов
    }

    // ВОЗВРАЩАЕМ МЕТОД GETMASTER ДЛЯ КЛИКА ПКМ ПО БЛОКУ
    public ModularFurnaceBlockEntity getMaster() {
        if (level != null && hasMaster()) {
            BlockEntity be = level.getBlockEntity(masterPos);
            if (be instanceof ModularFurnaceBlockEntity masterBE) {
                return masterBE;
            }
        }
        return this; // Если мастера нет, этот блок сам себе мастер
    }

    public void setMaster(BlockPos pos) {
        if (java.util.Objects.equals(this.masterPos, pos)) return;
        this.masterPos = pos;
        setChanged();
        
        if (level != null) {
            // Флаг 3 обновляет блок на клиенте и сервере
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            
            // КРИТИЧЕСКИЙ МОМЕНТ: Заставляем модель обновиться
            if (level.isClientSide) {
                requestModelDataUpdate();
                // "Пингуем" соседей, чтобы они тоже перерисовали свои швы
                level.setBlocksDirty(worldPosition, getBlockState(), getBlockState());
            }
        }
    }

    public BlockPos getMasterPos() {
        return masterPos != null ? masterPos : this.worldPosition;
    }

    public boolean hasMaster() {
        return masterPos != null;
    }

    // --- СИНХРОНИЗАЦИЯ С КЛИЕНТОМ ---

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (masterPos != null) {
            tag.put("MasterPos", NbtUtils.writeBlockPos(masterPos));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("MasterPos")) {
            this.masterPos = NbtUtils.readBlockPos(tag, "MasterPos").orElse(null);
        } else {
            this.masterPos = null;
        }
        if (level != null && level.isClientSide) {
            requestModelDataUpdate();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
}