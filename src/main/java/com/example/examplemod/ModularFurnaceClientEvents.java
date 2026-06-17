package com.example.examplemod;

import com.example.examplemod.client.ModularFurnaceBakedModel;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;

@EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
public class ModularFurnaceClientEvents {

    @SubscribeEvent
    public static void onModelModify(ModelEvent.ModifyBakingResult event) {
        event.getModels().keySet().forEach(location -> {
            // Меняем условие на более общее, чтобы захватить все модели из твоего списка
            if (location.id().getNamespace().equals(ExampleMod.MODID) && 
                location.id().getPath().contains("furnace")) { // Захватит всё, где есть слово furnace
                
                BakedModel original = event.getModels().get(location);
                if (original != null && !(original instanceof ModularFurnaceBakedModel)) {
                    event.getModels().put(location, new ModularFurnaceBakedModel(original));
                }
            }
        });
    }
}