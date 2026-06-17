package com.example.examplemod;

import com.example.examplemod.client.ModularFurnaceBakedModel;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;

@EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
public class ModularFurnaceClientEvents {

    @SubscribeEvent
    public static void onModelModify(ModelEvent.ModifyBakingResult event) {
        // ВНИМАНИЕ: Проверь, чтобы "upgraded_furnace" точно совпадало с ID регистрации твоего блока!
        ResourceLocation blockId = ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, "upgraded_furnace");
        
        // Для обычного блока в мире Minecraft ищет модель с определенным blockstate-контекстом
        event.getModels().keySet().forEach(location -> {
            if (location.id().getNamespace().equals(blockId.getNamespace()) && location.id().getPath().equals(blockId.getPath())) {
                BakedModel original = event.getModels().get(location);
                if (original != null) {
                    event.getModels().put(location, new ModularFurnaceBakedModel(original));
                }
            }
        });
    }
}