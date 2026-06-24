package com.rz.mswsm.client;

import com.rz.mswsm.Main;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

@EventBusSubscriber(
        modid = Main.MOD_ID,
        value = Dist.CLIENT
)
public class ClientEvents
{
    @SubscribeEvent
    public static void onClientLogin(ClientPlayerNetworkEvent.LoggingIn event)
    {
        Minecraft minecraft = Minecraft.getInstance();

        minecraft.tell(() -> {
            if (WarningScreen.shouldShow()) {
                minecraft.setScreen(new WarningScreen());
            }
        });
    }
}
