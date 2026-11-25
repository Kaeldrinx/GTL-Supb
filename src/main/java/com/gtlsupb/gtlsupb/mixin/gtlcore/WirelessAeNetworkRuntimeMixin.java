package com.gtlsupb.gtlsupb.mixin.gtlcore;

import org.gtlcore.gtlcore.integration.ae2.wireless.WirelessAeNetworkRuntime;

import net.minecraft.resources.ResourceLocation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = WirelessAeNetworkRuntime.class, remap = false)
public class WirelessAeNetworkRuntimeMixin {

    private static final String MOD_NAMESPACE = "gtlsupb";
    private static final String QUANTUM_ENTANGLEMENT_HATCH = "overlimit_quantum_entanglement_hatch";
    private static final String PATTERN_ASSEMBLY = "weihe_pattern_assembly";
    private static final String FTB_QUEST_HATCH = "overlimit_ftb_quest_integration_hatch";
    private static final String ME_DRIVE = "overlimit_me_drive";
    private static final String INTEGRATED_HATCH = "overlimit_integrated_hatch";

    @Inject(method = "isWirelessMeTargetId", at = @At("HEAD"), cancellable = true, remap = false)
    private static void gtlsupb$allowWirelessTarget(ResourceLocation blockId, CallbackInfoReturnable<Boolean> cir) {
        if (blockId == null || !MOD_NAMESPACE.equals(blockId.getNamespace())) return;
        String path = blockId.getPath();
        if (QUANTUM_ENTANGLEMENT_HATCH.equals(path) || PATTERN_ASSEMBLY.equals(path) || FTB_QUEST_HATCH.equals(path) || ME_DRIVE.equals(path) || INTEGRATED_HATCH.equals(path)) {
            cir.setReturnValue(true);
        }
    }
}
