package com.mrammor.create_rvb.registry;

import com.mrammor.create_rvb.CreateRebuildVanillaBlocks;
import com.mrammor.create_rvb.content.modular_furnace.ModularFurnaceBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = 
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, CreateRebuildVanillaBlocks.MODID);

    public static final Supplier<BlockEntityType<ModularFurnaceBlockEntity>> MODULAR_FURNACE = 
            BLOCK_ENTITIES.register("modular_furnace", 
                    () -> BlockEntityType.Builder.of(ModularFurnaceBlockEntity::new, Blocks.FURNACE).build(null));
}