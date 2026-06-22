package com.mrammor.create_rvb;

import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ModularFurnaceCTBehaviour extends ConnectedTextureBehaviour.Base {

    @Override
    public CTSpriteShiftEntry getShift(BlockState state, Direction direction, @Nullable TextureAtlasSprite sprite) {
        if (sprite == null) return ModSpriteShifts.FURNACE_SIDE;

        // 1. Крыша и дно бака
        if (direction == Direction.UP || direction == Direction.DOWN) {
            return ModSpriteShifts.FURNACE_TOP;
        }

        String textureName = sprite.contents().name().toString();

        // 2. Лицо печи (включено/выключено)
        if (textureName.contains("furnace_front_on")) return ModSpriteShifts.FURNACE_FRONT_ON;
        if (textureName.contains("furnace_front")) return ModSpriteShifts.FURNACE_FRONT;
        
        // 3. Все остальные стороны (бока и задник) отправляем на новую текстуру
        return ModSpriteShifts.FURNACE_SIDE;
    }

    @Override
    public boolean connectsTo(BlockState state, BlockState other, BlockAndTintGetter reader, BlockPos pos, BlockPos otherPos, Direction face) {
        // Проверяем, что соединяемся только с ванильной печкой
        if (!other.is(Blocks.FURNACE)) return false;

        int yDiff = Math.abs(pos.getY() - otherPos.getY());
        int xDiff = Math.abs(pos.getX() - otherPos.getX());
        int zDiff = Math.abs(pos.getZ() - otherPos.getZ());

        // Ограничения под размер твоего бака
        if (yDiff >= 9 || xDiff >= 3 || zDiff >= 3) return false;

        return true;
    }

    @Override
    public boolean reverseUVs(BlockState state, Direction face) {
        return false;
    }
}