package com.mrammor.create_rvb;

import com.simibubi.create.foundation.block.connected.CTModel;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;

// Обязательно добавляем bus = EventBusSubscriber.Bus.MOD
@EventBusSubscriber(modid = CreateRebuildVanillaBlocks.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class CreateRebuildVanillaBlocksClient {

    @SubscribeEvent
    public static void onModelBake(ModelEvent.ModifyBakingResult event) {
        var block = net.minecraft.world.level.block.Blocks.FURNACE;

        // Перебираем все возможные стейты блока и оборачиваем их модели в CT-рендер Create
        for (BlockState state : block.getStateDefinition().getPossibleStates()) {
            
            // Используем стандартный метод Minecraft для получения точного ModelResourceLocation
            ModelResourceLocation mrl = BlockModelShaper.stateToModelLocation(state);
            BakedModel originalModel = event.getModels().get(mrl);

            // Оборачиваем рабочую модель
            if (originalModel != null) {
                event.getModels().put(mrl, new CTModel(originalModel, new ModularFurnaceCTBehaviour()));
            }
        }
    }
}