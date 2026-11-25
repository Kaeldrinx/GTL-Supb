package com.gtlsupb.gtlsupb.mixin.gtlcore;

import org.gtlcore.gtlcore.common.machine.multiblock.part.ae.MEMolecularAssemblerIOPartMachine;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

import com.gtlsupb.gtlsupb.common.machine.multiblock.module.ChronoDeductionArray;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MEMolecularAssemblerIOPartMachine.class, remap = false)
public class MEMolecularAssemblerIOPartMachineMixin {

    @Inject(method = "shouldOpenUI", at = @At("HEAD"), cancellable = true, remap = false)
    private void gtlsupb$allowChronoModulePatternPort(Player player, InteractionHand hand, BlockHitResult hit,
                                                      CallbackInfoReturnable<Boolean> cir) {
        for (IMultiController controller : ((MultiblockPartMachine) (Object) this).getControllers()) {
            if (controller instanceof ChronoDeductionArray) {
                cir.setReturnValue(true);
                return;
            }
        }
    }
}
