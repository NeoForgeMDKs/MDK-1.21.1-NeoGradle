package com.rz.mswsm;

import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
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
    public Main()
    {
        logger = LogManager.getLogger();
        logger.info("Motion Sickness Warning Screen Mod version " + VERSION + " loaded.");
    }
}
