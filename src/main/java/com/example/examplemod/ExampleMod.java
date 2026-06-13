package com.example.examplemod;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;

@Mod(ExampleMod.MODID)
public class ExampleMod {
    public static final String MODID = "examplemod";
    public static final Logger LOGGER = LogUtils.getLogger();

    // Оставляем три главных регистратора проекта
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // ================= РЕГИСТРАЦИЯ ТВОЕГО БЛОКА =================
    
    // Создаем блок плавильни на основе свойств ванильного железа
    public static final DeferredBlock<ModularFurnaceCoreBlock> MODULAR_FURNACE_CORE = BLOCKS.register(
        "modular_furnace_core", 
        () -> new ModularFurnaceCoreBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK))

    );

    public static final net.neoforged.neoforge.registries.DeferredHolder<BlockEntityType<?>, BlockEntityType<ModularFurnaceCoreEntity>> MODULAR_FURNACE_CORE_BE = 
        BLOCK_ENTITIES.register("modular_furnace_core_be", () -> BlockEntityType.Builder.of(ModularFurnaceCoreEntity::new, MODULAR_FURNACE_CORE.get()).build(null));

    public static final DeferredBlock<ModularFurnacePartBlock> MODULAR_FURNACE_PART = BLOCKS.register(
        "modular_furnace_part", 
        () -> new ModularFurnacePartBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK))
    );
    

    // Создаем предмет для этого блока, чтобы его можно было держать в инвентаре
    public static final DeferredItem<BlockItem> MODULAR_FURNACE_CORE_ITEM = ITEMS.registerSimpleBlockItem(
        "modular_furnace_core", 
        MODULAR_FURNACE_CORE
    );

    // ================= КРЕАТИВНАЯ ВКЛАДКА МОДА =================
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MOD_TAB = CREATIVE_MODE_TABS.register("mod_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.examplemod")) 
            .withTabsBefore(CreativeModeTabs.COMBAT)
            // Иконкой вкладки будет наш блок плавильни
            .icon(() -> MODULAR_FURNACE_CORE_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                // Добавляем блок плавильни внутрь вкладки
                output.accept(MODULAR_FURNACE_CORE_ITEM.get()); 
            }).build());

    // Конструктор мода
    public ExampleMod(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        // Подключаем регистраторы к шине событий игры
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Мод на модульные печи успешно инициализирован!");
    }
}