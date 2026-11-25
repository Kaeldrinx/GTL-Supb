package com.gtlsupb.gtlsupb.mixin.ae2;

import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.crafting.CraftingCalculation;
import com.gtlsupb.gtlsupb.integration.ae2.CatalystPlanNotes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(CraftingCalculation.class)
public abstract class CraftingCalculationMixin {

    @Inject(method = "run", at = @At("HEAD"), require = 0, remap = false)
    private void gtlsupb$beginCatalystNotes(CallbackInfoReturnable<ICraftingPlan> cir) {
        CatalystPlanNotes.beginPlan();
    }

    @Inject(method = "run", at = @At("RETURN"), require = 0, remap = false)
    private void gtlsupb$applyCatalystNotes(CallbackInfoReturnable<ICraftingPlan> cir) {
        CatalystPlanNotes.endPlan();
        try {
            Set<AEKey> keys = CatalystPlanNotes.drain();
            if (keys.isEmpty()) return;
            ICraftingPlan plan = cir.getReturnValue();
            if (plan == null) return;
            KeyCounter missing = plan.missingItems();
            if (missing == null) return;
            for (AEKey key : keys) {
                if (missing.get(key) <= 0L) missing.add(key, 1L);
            }
        } catch (Throwable ignored) {

        }
    }
}
