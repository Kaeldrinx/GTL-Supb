package com.gtlsupb.gtlsupb.mixin.gtceu;

import com.gregtechceu.gtceu.api.gui.widget.PatternPreviewWidget;

import com.lowdragmc.lowdraglib.gui.widget.SceneWidget;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PatternPreviewWidget.class, remap = false)
public abstract class PatternPreviewWidgetMixin {

    @Shadow(remap = false)
    @Final
    private SceneWidget sceneWidget;

    @Inject(method = "setupScene", at = @At("TAIL"), remap = false)
    private void gtlsupb$onSetupScene(CallbackInfo ci) {
        gtlsupb$enablePreviewCache();
    }

    @Inject(method = "onFormedSwitch", at = @At("TAIL"), remap = false)
    private void gtlsupb$onFormedSwitch(boolean formed, CallbackInfo ci) {
        gtlsupb$enablePreviewCache();
    }

    private void gtlsupb$enablePreviewCache() {
        try {
            if (sceneWidget == null) return;
            sceneWidget.useCacheBuffer();
            sceneWidget.needCompileCache();
        } catch (Throwable ignored) {}
    }
}
