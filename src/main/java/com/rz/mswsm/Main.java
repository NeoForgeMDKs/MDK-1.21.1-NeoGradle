package com.rz.mswsm;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Main.MOD_ID)
public class Main
{
    public static final String MOD_ID = "mswsm";
    public static final String VERSION = ModList.get()
            .getModContainerById(MOD_ID)
            .orElseThrow()
            .getModInfo()
            .getVersion()
            .toString();
    public Logger logger;

    public Main(ModContainer modContainer)
    {
        logger = LogManager.getLogger();
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        logger.info("Motion Sickness Warning Screen Mod version " + VERSION + " loaded.");
    }
}
