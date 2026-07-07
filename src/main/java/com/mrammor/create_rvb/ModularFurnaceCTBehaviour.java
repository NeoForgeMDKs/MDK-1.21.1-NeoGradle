package com.mrammor.create_rvb;

import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import com.mrammor.create_rvb.content.modular_furnace.ModularFurnaceBlockEntity;

public class ModularFurnaceCTBehaviour extends ConnectedTextureBehaviour.Base {

    @Override
    public CTSpriteShiftEntry getShift(BlockState state, Direction direction, @Nullable TextureAtlasSprite sprite) {
        if (sprite == null) return ModSpriteShifts.FURNACE_SIDE;

        if (direction == Direction.UP || direction == Direction.DOWN) {
            return ModSpriteShifts.FURNACE_TOP;
        }

        String textureName = sprite.contents().name().toString();

        if (textureName.contains("furnace_front_on")) return ModSpriteShifts.FURNACE_FRONT_ON;
        if (textureName.contains("furnace_front")) return ModSpriteShifts.FURNACE_FRONT;
        
        return ModSpriteShifts.FURNACE_SIDE;
    }

    @Override
    public boolean connectsTo(BlockState state, BlockState other, BlockAndTintGetter reader, BlockPos pos, BlockPos otherPos, Direction face) {
        if (!other.is(Blocks.FURNACE)) return false;

        BlockEntity be1 = reader.getBlockEntity(pos);
        BlockEntity be2 = reader.getBlockEntity(otherPos);

        if (be1 instanceof ModularFurnaceBlockEntity m1 && be2 instanceof ModularFurnaceBlockEntity m2) {
            // Соединяем только если оба блока принадлежат одной структуре
            return m1.getMasterPos() != null && m1.getMasterPos().equals(m2.getMasterPos());
        }
        return false;
    }
}