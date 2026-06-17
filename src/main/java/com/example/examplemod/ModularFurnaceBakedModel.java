package com.example.examplemod.client;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class ModularFurnaceBakedModel implements IDynamicBakedModel {

    private final BakedModel originalModel;

    public ModularFurnaceBakedModel(BakedModel originalModel) {
        this.originalModel = originalModel;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, 
                                             @NotNull RandomSource rand, @NotNull ModelData modelData, 
                                             @Nullable RenderType renderType) {
        
        // Если запрашивается null-сторона (внутренние/общие данные модели), отдаем оригинал json'а
        if (side == null) {
            return originalModel.getQuads(state, null, rand, modelData, renderType);
        }

        // Защита от фиолетовых текстур: если слой рендера не подходит под сОлид блоки, шлем оригинал
        if (renderType != null && renderType != RenderType.solid() && renderType != RenderType.cutout()) {
            return originalModel.getQuads(state, side, rand, modelData, renderType);
        }

        // Читаем наш контекст
        ModularFurnaceCTBehaviour.BlockContext context = modelData.get(ModularFurnaceCTBehaviour.BLOCK_CONTEXT);
        if (context != null && state != null) {
            BlockPos currentPos = context.pos();
            BlockPos neighborPos = currentPos.relative(side);
            BlockState neighborState = context.level().getBlockState(neighborPos);

            // Наша кастомная логика скрытия стыков: если блоки соединены — убираем рендер грани
            if (ModularFurnaceCTBehaviour.connectsTo(state, neighborState, context.level(), currentPos, neighborPos, side)) {
                return Collections.emptyList();
            }
        }

        // Во всех остальных случаях возвращаем стандартные стороны блока из json
        return originalModel.getQuads(state, side, rand, modelData, renderType);
    }

    @Override public boolean useAmbientOcclusion() { return originalModel.useAmbientOcclusion(); }
    @Override public boolean isGui3d() { return originalModel.isGui3d(); }
    @Override public boolean usesBlockLight() { return originalModel.usesBlockLight(); }
    @Override public boolean isCustomRenderer() { return originalModel.isCustomRenderer(); }
    @Override public @NotNull net.minecraft.client.renderer.texture.TextureAtlasSprite getParticleIcon() { return originalModel.getParticleIcon(); }
    @Override public @NotNull ItemTransforms getTransforms() { return originalModel.getTransforms(); }
    @Override public @NotNull ItemOverrides getOverrides() { return originalModel.getOverrides(); }
}