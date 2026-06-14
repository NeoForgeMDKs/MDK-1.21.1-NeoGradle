package com.example.examplemod;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

@Mod(ExampleMod.MODID)
public class ExampleMod {
    public static final String MODID = "examplemod";
    public static final Logger LOGGER = LogUtils.getLogger();

    // ==================== РЕГИСТРАТОРЫ ====================
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // ==================== БЛОКИ ====================
    public static final DeferredBlock<ModularFurnacePartBlock> MODULAR_FURNACE_PART = BLOCKS.register(
            "modular_furnace_part",
            () -> new ModularFurnacePartBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK))
    );

    public static final DeferredBlock<UpgradedFurnaceBlock> UPGRADED_FURNACE = BLOCKS.register(
            "upgraded_furnace",
            () -> new UpgradedFurnaceBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.FURNACE))
    );

    // ==================== BLOCK ENTITY ====================
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<UpgradedFurnaceEntity>> UPGRADED_FURNACE_BE =
            BLOCK_ENTITIES.register("upgraded_furnace_be",
                    () -> BlockEntityType.Builder.of(UpgradedFurnaceEntity::new, UPGRADED_FURNACE.get()).build(null)
            );

    // ==================== ПРЕДМЕТЫ ====================
    public static final DeferredItem<BlockItem> UPGRADED_FURNACE_ITEM = ITEMS.registerSimpleBlockItem(UPGRADED_FURNACE);

    // ==================== КРЕАТИВ ====================
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MOD_TAB = CREATIVE_MODE_TABS.register("mod_tab", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + MODID))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> UPGRADED_FURNACE_ITEM.get().getDefaultInstance())
                    .displayItems((parameters, output) -> output.accept(UPGRADED_FURNACE_ITEM.get()))
                    .build()
    );

    public ExampleMod(IEventBus modEventBus, net.neoforged.fml.ModContainer modContainer) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        // ←←← ДОБАВЬ ЭТУ СТРОКУ
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(ModEvents.class);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
}