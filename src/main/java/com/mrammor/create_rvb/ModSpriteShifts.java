package com.mrammor.create_rvb;

import com.simibubi.create.foundation.block.connected.AllCTTypes;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter;
import net.minecraft.resources.ResourceLocation;

public class ModSpriteShifts {
    
    // ВАЖНО: Первый ResourceLocation должен указывать на КЛЮЧ ВАНИЛЬНОЙ ТЕКСТУРЫ (с ней совпадает рендер)
    public static final CTSpriteShiftEntry FURNACE_SIDE = CTSpriteShifter.getCT(
        AllCTTypes.RECTANGLE, 
        ResourceLocation.withDefaultNamespace("block/furnace_side"), // Ванильный исходник боковины
        ResourceLocation.fromNamespaceAndPath(CreateRebuildVanillaBlocks.MODID, "block/upgraded_furnace_side_connected") // Твоя НОВАЯ текстура для боков!
    );

    public static final CTSpriteShiftEntry FURNACE_FRONT = CTSpriteShifter.getCT(
        AllCTTypes.RECTANGLE, 
        ResourceLocation.withDefaultNamespace("block/furnace_front"), // это майнкрафт!
        ResourceLocation.fromNamespaceAndPath(CreateRebuildVanillaBlocks.MODID, "block/upgraded_furnace_connected") 
    );

    public static final CTSpriteShiftEntry FURNACE_FRONT_ON = CTSpriteShifter.getCT(
        AllCTTypes.RECTANGLE, 
        ResourceLocation.withDefaultNamespace("block/furnace_front_on"), // это майнкрафт!
        ResourceLocation.fromNamespaceAndPath(CreateRebuildVanillaBlocks.MODID, "block/upgraded_furnace_connected") 
    );

    // Добавим крышку на всякий случай, если ты захочешь её использовать
    public static final CTSpriteShiftEntry FURNACE_TOP = CTSpriteShifter.getCT(
        AllCTTypes.RECTANGLE, // <--- МЕНЯЕМ ЭТО
        ResourceLocation.withDefaultNamespace("block/furnace_top"), 
        ResourceLocation.fromNamespaceAndPath(CreateRebuildVanillaBlocks.MODID, "block/upgraded_furnace_top_connected") 
    );

    public static void init() {}
}