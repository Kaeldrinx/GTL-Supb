package com.gtlsupb.gtlsupb.mixin.gtceu.recipe;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.integration.jade.provider.RecipeOutputProvider;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import com.gtlsupb.gtlsupb.api.machine.logic.MutableRecipesLogic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

@Mixin(RecipeOutputProvider.class)
public abstract class RecipeOutputProviderMixin {

    @Inject(method = "addTooltip(Lnet/minecraft/nbt/CompoundTag;Lsnownee/jade/api/ITooltip;" +
            "Lnet/minecraft/world/entity/player/Player;Lsnownee/jade/api/BlockAccessor;" +
            "Lnet/minecraft/world/level/block/entity/BlockEntity;Lsnownee/jade/api/config/IPluginConfig;)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    private void gtlsupb$hideNativeOutput(CompoundTag data, ITooltip tooltip, Player player, BlockAccessor accessor,
                                          BlockEntity blockEntity, IPluginConfig config, CallbackInfo ci) {
        if (blockEntity instanceof MetaMachineBlockEntity be) {
            var machine = be.getMetaMachine();
            if (machine instanceof IRecipeLogicMachine logicMachine &&
                    logicMachine.getRecipeLogic() instanceof MutableRecipesLogic) {
                ci.cancel();
            }
        }
    }
}
