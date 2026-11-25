package com.gtlsupb.gtlsupb.mixin.gtceu.recipe;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.integration.jade.provider.MachineModeProvider;

import com.gtlsupb.gtlsupb.api.machine.logic.MutableRecipesLogic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

@Mixin(MachineModeProvider.class)
public abstract class MachineModeProviderMixin {

    @Inject(method = "appendTooltip(Lsnownee/jade/api/ITooltip;Lsnownee/jade/api/BlockAccessor;" +
            "Lsnownee/jade/api/config/IPluginConfig;)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    private void gtlsupb$hideMachineMode(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config,
                                         CallbackInfo ci) {
        if (accessor.getBlockEntity() instanceof MetaMachineBlockEntity be) {
            var machine = be.getMetaMachine();
            if (machine instanceof IRecipeLogicMachine logicMachine &&
                    logicMachine.getRecipeLogic() instanceof MutableRecipesLogic) {
                ci.cancel();
            }
        }
    }
}
