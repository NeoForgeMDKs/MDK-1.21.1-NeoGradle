package com.rz.mswsm.mixin;

import com.rz.mswsm.Config;
import com.rz.mswsm.diagnostic.ShutdownTracker;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerDiagnosticMixin {
    @Inject(
            method = "stopServer",
            at = @At("HEAD")
    )
    private void mswsm$markShutdownBeginning(CallbackInfo ci) {
        if (!Config.chunkDiagnosticsEnabled()) {
            return;
        }

        ShutdownTracker.beginShutdown();
    }
}
