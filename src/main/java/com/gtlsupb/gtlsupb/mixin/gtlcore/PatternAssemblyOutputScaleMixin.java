package com.gtlsupb.gtlsupb.mixin.gtlcore;

import org.gtlcore.gtlcore.common.machine.multiblock.part.ae.MEPatternBufferPartMachine;

import net.minecraft.world.item.ItemStack;

import appeng.api.crafting.IPatternDetails;
import com.gtlsupb.gtlsupb.common.machine.multiblock.part.PatternAssemblyPartMachine;
import com.gtlsupb.gtlsupb.integration.ae2.PatternOutputScaleRewriter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MEPatternBufferPartMachine.class, remap = false)
public abstract class PatternAssemblyOutputScaleMixin {

    @Inject(method = "getRealPattern", at = @At("RETURN"), cancellable = true, remap = false)
    private void gtlsupb$scalePatternOutput(int slot, ItemStack stack,
                                            CallbackInfoReturnable<IPatternDetails> cir) {
        if (!(((Object) this) instanceof PatternAssemblyPartMachine assembly)) return;
        IPatternDetails original = cir.getReturnValue();
        if (original == null) return;
        int multiplier = assembly.gtlsupb$getRecognizedOutputMultiplier();
        if (multiplier <= 1) return;
        IPatternDetails rewritten = PatternOutputScaleRewriter.rewrite(
                original, assembly.getLevel(), multiplier);
        if (rewritten != null && rewritten != original) cir.setReturnValue(rewritten);
    }
}
