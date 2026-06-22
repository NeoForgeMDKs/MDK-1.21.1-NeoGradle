package com.mrammor.create_rvb.foundation.data;

import com.mrammor.create_rvb.CreateRebuildVanillaBlocks;
import com.simibubi.create.foundation.block.connected.AllCTTypes;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter;
import net.minecraft.resources.ResourceLocation;

public class ModSpriteShifts {
    
    public static final CTSpriteShiftEntry FURNACE_SIDE = CTSpriteShifter.getCT(
        AllCTTypes.RECTANGLE, 
        ResourceLocation.withDefaultNamespace("block/furnace_side"), 
        ResourceLocation.fromNamespaceAndPath(CreateRebuildVanillaBlocks.MODID, "block/upgraded_furnace_side_connected")
    );

    public static final CTSpriteShiftEntry FURNACE_FRONT = CTSpriteShifter.getCT(
        AllCTTypes.RECTANGLE, 
        ResourceLocation.withDefaultNamespace("block/furnace_front"), 
        ResourceLocation.fromNamespaceAndPath(CreateRebuildVanillaBlocks.MODID, "block/upgraded_furnace_connected") 
    );

    public static final CTSpriteShiftEntry FURNACE_FRONT_ON = CTSpriteShifter.getCT(
        AllCTTypes.RECTANGLE, 
        ResourceLocation.withDefaultNamespace("block/furnace_front_on"), 
        ResourceLocation.fromNamespaceAndPath(CreateRebuildVanillaBlocks.MODID, "block/upgraded_furnace_connected") 
    );

    public static final CTSpriteShiftEntry FURNACE_TOP = CTSpriteShifter.getCT(
        AllCTTypes.RECTANGLE, 
        ResourceLocation.withDefaultNamespace("block/furnace_top"), 
        ResourceLocation.fromNamespaceAndPath(CreateRebuildVanillaBlocks.MODID, "block/upgraded_furnace_top_connected") 
    );

    public static void init() {}
}