package com.gtlsupb.gtlsupb.mixin.gtceu.recipe;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.integration.jade.provider.RecipeLogicProvider;

import net.minecraft.nbt.CompoundTag;

import com.gtlsupb.gtlsupb.api.recipe.IWirelessGTRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.math.BigInteger;

@Mixin(RecipeLogicProvider.class)
public abstract class RecipeLogicProviderMixin {

    @Inject(method = "write(Lnet/minecraft/nbt/CompoundTag;Lcom/gregtechceu/gtceu/api/machine/trait/RecipeLogic;)V",
            at = @At("TAIL"),
            remap = false)
    private void gtlSupb$writeWirelessEu(CompoundTag data, RecipeLogic capability, CallbackInfo ci) {
        GTRecipe wirelessRecipe = capability.getLastRecipe();
        if (wirelessRecipe instanceof IWirelessGTRecipe recipe) {
            BigInteger bigIntInput = recipe.getWirelessEuTickInputs();
            if (bigIntInput != null && bigIntInput.signum() != 0) {
                data.putByteArray("wirelessTickInputs", bigIntInput.toByteArray());
            }
        }
    }
}
