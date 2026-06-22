package com.mrammor.create_rvb;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(CreateRebuildVanillaBlocks.MODID)
public class CreateRebuildVanillaBlocks {
    public static final String MODID = "create_rvb";
    private static final Logger LOGGER = LogUtils.getLogger();

    public CreateRebuildVanillaBlocks(IEventBus modEventBus) {
        // Просто пинаем регистратор текстур Create
        ModSpriteShifts.init();
        LOGGER.info("Create: Rebuild Vanilla Blocks успешно внедрился в ванильные печи!");
    }
}