package com.example.examplemod.client;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.UpgradedFurnaceEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelProperty;

public class ModularFurnaceCTBehaviour {

    // Наше собственное свойство модели, которое никогда не сломается при обновлениях NeoForge
    public static final ModelProperty<BlockContext> BLOCK_CONTEXT = new ModelProperty<>();

    public record BlockContext(BlockAndTintGetter level, BlockPos pos) {}

    public static boolean connectsTo(BlockState state, BlockState other, BlockAndTintGetter reader, BlockPos pos, BlockPos otherPos, Direction face) {
        if (!other.is(ExampleMod.UPGRADED_FURNACE.get())) {
            return false;
        }

        if (reader.getBlockEntity(pos) instanceof UpgradedFurnaceEntity be1 && 
            reader.getBlockEntity(otherPos) instanceof UpgradedFurnaceEntity be2) {
            
            return be1.getController().getBlockPos().equals(be2.getController().getBlockPos());
        }

        return false;
    }
}