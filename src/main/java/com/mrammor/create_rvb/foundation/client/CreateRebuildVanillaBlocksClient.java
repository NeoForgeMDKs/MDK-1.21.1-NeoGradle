package com.mrammor.create_rvb.foundation.client;

import com.mrammor.create_rvb.content.modular_furnace.ModularFurnaceCTBehaviour;
import com.mrammor.create_rvb.foundation.data.ModSpriteShifts;
import com.simibubi.create.foundation.block.connected.CTModel;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ModelEvent;

public class CreateRebuildVanillaBlocksClient {

    public static void onClientSetup(FMLClientSetupEvent event) {
        // Инициализируем текстуры только на клиенте
        ModSpriteShifts.init();
    }

    public static void onModelBake(ModelEvent.ModifyBakingResult event) {
        var block = net.minecraft.world.level.block.Blocks.FURNACE;
        var blockId = BuiltInRegistries.BLOCK.getKey(block);

        for (BlockState state : block.getStateDefinition().getPossibleStates()) {
            String variant = BlockModelShaper.statePropertiesToString(state.getValues());
            ModelResourceLocation mrl = new ModelResourceLocation(blockId, variant);
            BakedModel originalModel = event.getModels().get(mrl);

            if (originalModel != null) {
                event.getModels().put(mrl, new CTModel(originalModel, new ModularFurnaceCTBehaviour()));
            }
        }
    }
}