package com.rz.mswsm.mixin;

import com.rz.mswsm.Config;
import com.rz.mswsm.diagnostic.GenerationRefTracker;
import net.minecraft.server.level.GenerationChunkHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GenerationChunkHolder.class)
public abstract class GenerationChunkHolderDiagnosticMixin {

    @Unique
    private int mswsm$refCountBeforeIncrease;

    @Unique
    private int mswsm$refCountBeforeDecrease;

    @Inject(method = "increaseGenerationRefCount", at = @At("HEAD"))
    private void mswsm$beforeIncreaseGenerationRefCount(CallbackInfo ci) {
        if (!Config.chunkDiagnosticsEnabled()) {
            return;
        }

        GenerationChunkHolder self = (GenerationChunkHolder) (Object) this;
        mswsm$refCountBeforeIncrease = self.getGenerationRefCount();
    }

    @Inject(method = "increaseGenerationRefCount", at = @At("RETURN"))
    private void mswsm$afterIncreaseGenerationRefCount(CallbackInfo ci) {
        if (!Config.chunkDiagnosticsEnabled()) {
            return;
        }

        GenerationChunkHolder self = (GenerationChunkHolder) (Object) this;
        GenerationRefTracker.recordIncrease(
                self,
                mswsm$refCountBeforeIncrease,
                self.getGenerationRefCount()
        );
    }

    @Inject(method = "decreaseGenerationRefCount", at = @At("HEAD"))
    private void mswsm$beforeDecreaseGenerationRefCount(CallbackInfo ci) {
        if (!Config.chunkDiagnosticsEnabled()) {
            return;
        }

        GenerationChunkHolder self = (GenerationChunkHolder) (Object) this;
        mswsm$refCountBeforeDecrease = self.getGenerationRefCount();
    }

    @Inject(method = "decreaseGenerationRefCount", at = @At("RETURN"))
    private void mswsm$afterDecreaseGenerationRefCount(CallbackInfo ci) {
        if (!Config.chunkDiagnosticsEnabled()) {
            return;
        }

        GenerationChunkHolder self = (GenerationChunkHolder) (Object) this;
        GenerationRefTracker.recordDecrease(
                self,
                mswsm$refCountBeforeDecrease,
                self.getGenerationRefCount()
        );
    }
}
