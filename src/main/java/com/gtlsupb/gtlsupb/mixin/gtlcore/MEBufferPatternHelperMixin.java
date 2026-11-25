package com.gtlsupb.gtlsupb.mixin.gtlcore;

import org.gtlcore.gtlcore.integration.ae2.handler.MEBufferPatternHelper;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.crafting.pattern.AEProcessingPattern;
import com.gtlsupb.gtlsupb.api.item.tool.ae2.Ae2PatternHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MEBufferPatternHelper.class)
public abstract class MEBufferPatternHelperMixin {

    @Inject(method = "createPatternWithoutCircuit", at = @At("RETURN"), cancellable = true, remap = false)
    private void gtlsupb$preserveCustomKeys(AEProcessingPattern pattern, Level level, boolean keepByProduct,
                                            CallbackInfoReturnable<IPatternDetails> cir) {
        try {
            if (!(cir.getReturnValue() instanceof AEProcessingPattern rebuilt)) return;
            ItemStack originalStack = pattern.getDefinition().toStack();
            if (!Ae2PatternHelper.hasCustomKeys(originalStack)) return;

            ItemStack rebuiltStack = PatternDetailsHelper.encodeProcessingPattern(rebuilt.getSparseInputs(),
                    rebuilt.getSparseOutputs());
            Ae2PatternHelper.copyAllCustomKeys(originalStack, rebuiltStack);
            IPatternDetails restored = PatternDetailsHelper.decodePattern(rebuiltStack, level, false);
            if (restored != null) {
                cir.setReturnValue(restored);
            }
        } catch (Throwable ignored) {

        }
    }

    @Inject(method = "rebuildPatternCircuit", at = @At("RETURN"), remap = false)
    private void gtlsupb$preserveCustomKeysOnRebuild(ItemStack originalPatternStack, int circuitConfig,
                                                     boolean replaceExisting, Level level,
                                                     CallbackInfoReturnable<ItemStack> cir) {
        try {
            ItemStack rebuilt = cir.getReturnValue();
            if (rebuilt == null || rebuilt.isEmpty() || rebuilt == originalPatternStack) return;
            Ae2PatternHelper.copyAllCustomKeys(originalPatternStack, rebuilt);
        } catch (Throwable ignored) {}
    }
}
