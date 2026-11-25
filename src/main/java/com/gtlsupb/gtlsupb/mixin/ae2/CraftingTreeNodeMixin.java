package com.gtlsupb.gtlsupb.mixin.ae2;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.KeyCounter;
import appeng.crafting.CraftingTreeNode;
import appeng.crafting.inv.CraftingSimulationState;
import com.gtlsupb.gtlsupb.integration.ae2.CatalystInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingTreeNode.class)
public abstract class CraftingTreeNodeMixin {

    @Shadow(remap = false)
    IPatternDetails.IInput parentInput;

    @Inject(method = "request", at = @At("HEAD"), cancellable = true, remap = false)
    private void gtlsupb$skipCatalystRequest(CraftingSimulationState inv, long requestedAmount,
                                             KeyCounter containerItems, CallbackInfo ci) {
        if (this.parentInput instanceof CatalystInput) {
            ci.cancel();
        }
    }
}
