package com.gtlsupb.gtlsupb.mixin.gtlcore;

import org.gtlcore.gtlcore.common.machine.multiblock.part.ae.MEPatternBufferPartMachineBase;

import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.KeyCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MEPatternBufferPartMachineBase.class, remap = false)
public abstract class MEPatternBufferPushPatternMixin {

    @Inject(method = "pushPattern(Lappeng/api/crafting/IPatternDetails;[Lappeng/api/stacks/KeyCounter;)Z",
            at = @At("RETURN"),
            cancellable = true,
            remap = false)
    private void gtlsupb$wakeControllersOnPush(IPatternDetails pattern, KeyCounter[] inputs,
                                               CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ()) return;
        if (!((Object) this instanceof MultiblockPartMachine part)) return;
        try {
            for (IMultiController controller : part.getControllers()) {
                if (!(controller instanceof IRecipeLogicMachine machine)) continue;
                RecipeLogic logic = machine.getRecipeLogic();
                if (logic == null) continue;

                logic.markLastRecipeDirty();
                logic.updateTickSubscription();
            }
        } catch (Throwable ignored) {}
    }
}
