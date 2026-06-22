package com.mrammor.create_rvb.content.modular_furnace;

import com.mrammor.create_rvb.foundation.data.ModSpriteShifts;
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

public class ModularFurnaceCTBehaviour extends ConnectedTextureBehaviour.Base {

    @Override
    public CTSpriteShiftEntry getShift(BlockState state, Direction direction, @Nullable TextureAtlasSprite sprite) {
        if (sprite == null) return ModSpriteShifts.FURNACE_SIDE;
        
        // Верх и низ соединяем по своей текстуре
        if (direction == Direction.UP || direction == Direction.DOWN) 
            return ModSpriteShifts.FURNACE_TOP;

        // Определяем текстуру боковой грани
        String textureName = sprite.contents().name().toString();
        if (textureName.contains("furnace_front_on")) return ModSpriteShifts.FURNACE_FRONT_ON;
        if (textureName.contains("furnace_front")) return ModSpriteShifts.FURNACE_FRONT;
        
        return ModSpriteShifts.FURNACE_SIDE;
    }

    @Override
    public boolean connectsTo(BlockState state, BlockState other, BlockAndTintGetter reader, BlockPos pos, BlockPos otherPos, Direction face) {
        // 1. Простейшая проверка типа блока
        if (!other.is(Blocks.FURNACE)) return false;

        // 2. Получаем BlockEntity обоих блоков
        BlockEntity be1 = reader.getBlockEntity(pos);
        BlockEntity be2 = reader.getBlockEntity(otherPos);

        // 3. Если сущностей нет (загружаются), не соединяем (или можно вернуть true для дефолтного вида)
        if (be1 == null || be2 == null) return false;

        if (be1 instanceof ModularFurnaceBlockEntity furnace1 && be2 instanceof ModularFurnaceBlockEntity furnace2) {
            
            boolean hasM1 = furnace1.hasMaster();
            boolean hasM2 = furnace2.hasMaster();

            // 4. Если оба блока имеют Мастера — проверяем их равенство
            if (hasM1 && hasM2) {
                return furnace1.getMasterPos().equals(furnace2.getMasterPos());
            }
            
            // 5. Если один из блоков НЕ имеет мастера, значит, структура не полная или еще не собралась.
            // В этот момент они НЕ должны соединяться, чтобы игрок видел, что мультиблок не готов.
            return false;
        }

        return false;
    }

    @Override
    public boolean reverseUVs(BlockState state, Direction face) {
        return false;
    }
}