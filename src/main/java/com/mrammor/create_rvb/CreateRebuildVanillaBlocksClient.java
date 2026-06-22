package com.mrammor.create_rvb;

import com.simibubi.create.foundation.block.connected.CTModel;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;

@EventBusSubscriber(modid = CreateRebuildVanillaBlocks.MODID, value = Dist.CLIENT)
public class CreateRebuildVanillaBlocksClient {

    @SubscribeEvent
    public static void onModelBake(ModelEvent.ModifyBakingResult event) {
        var block = net.minecraft.world.level.block.Blocks.FURNACE;
        var blockId = BuiltInRegistries.BLOCK.getKey(block);

        // Перебираем все возможные стейты блока и оборачиваем их модели в CT-рендер Create
        for (BlockState state : block.getStateDefinition().getPossibleStates()) {
            // Пытаемся найти модель по базовому ID
            ModelResourceLocation mrl = new ModelResourceLocation(blockId, "");
            BakedModel originalModel = event.getModels().get(mrl);

            // Если у блока есть свойства (например facing), ищем по полному стейту
            if (originalModel == null && state.toString().contains("[")) {
                String stateProperties = state.toString().split("\\[")[1].replace("]", "");
                mrl = new ModelResourceLocation(blockId, stateProperties);
                originalModel = event.getModels().get(mrl);
            }

            // Оборачиваем рабочую модель
            if (originalModel != null) {
                event.getModels().put(mrl, new CTModel(originalModel, new ModularFurnaceCTBehaviour()));
            }
        }
    }
}