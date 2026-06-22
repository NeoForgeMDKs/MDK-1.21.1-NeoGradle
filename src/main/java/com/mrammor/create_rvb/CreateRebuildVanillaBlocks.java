package com.mrammor.create_rvb;

import com.mrammor.create_rvb.foundation.client.CreateRebuildVanillaBlocksClient;
import com.mrammor.create_rvb.registry.ModBlockEntityTypes;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

@Mod(CreateRebuildVanillaBlocks.MODID)
public class CreateRebuildVanillaBlocks {
    public static final String MODID = "create_rvb";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CreateRebuildVanillaBlocks(IEventBus modEventBus) {
        // Регистрируем блок-сущности
        ModBlockEntityTypes.BLOCK_ENTITIES.register(modEventBus);

        // Безопасное подключение клиента без использования устаревших аннотаций @EventBusSubscriber
        if (FMLEnvironment.dist.isClient()) {
            modEventBus.addListener(CreateRebuildVanillaBlocksClient::onClientSetup);
            modEventBus.addListener(CreateRebuildVanillaBlocksClient::onModelBake);
        }

        LOGGER.info("Create: Rebuild Vanilla Blocks успешно инициализирован в стиле Create!");
    }
}