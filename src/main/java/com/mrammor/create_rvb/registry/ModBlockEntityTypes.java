package com.mrammor.create_rvb.registry;

import com.mrammor.create_rvb.CreateRebuildVanillaBlocks;
import com.mrammor.create_rvb.content.modular_furnace.ModularFurnaceBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntityTypes {
    // Создаем регистратор для Block Entity
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = 
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, CreateRebuildVanillaBlocks.MODID);

    // Регистрируем тип нашей кастомной блок-сущности модульной печи
    // ВАЖНО: Пока у тебя нет зарегистрированного кастомного блока в коде, мы временно привязываем его к Blocks.FURNACE,
    // чтобы компилятор не ругался. Когда зарегистрируешь свой блок, замени Blocks.FURNACE на свой блок.
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ModularFurnaceBlockEntity>> MODULAR_FURNACE =
            BLOCK_ENTITY_TYPES.register("modular_furnace", () -> 
                    BlockEntityType.Builder.of(ModularFurnaceBlockEntity::new, Blocks.FURNACE).build(null)
            );

    // Метод для вызова в главном классе мода
    public static void register(IEventBus eventBus) {
        BLOCK_ENTITY_TYPES.register(eventBus);
    }
}