package com.gtlsupb.gtlsupb.mixin.gtceu.integration;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.integration.jade.provider.ParallelProvider;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import com.gtlsupb.gtlsupb.api.machine.mutable.MultiTypeElectricMultiblockMachine;
import com.gtlsupb.gtlsupb.common.machine.multiblock.GTLSupbMolecularAssembler;
import com.gtlsupb.gtlsupb.common.machine.multiblock.HypercubePhaseStirringCriticalPolymerizationCoreTower;
import com.gtlsupb.gtlsupb.common.machine.multiblock.MicrocosmFoundry;
import com.gtlsupb.gtlsupb.common.machine.multiblock.SuperStellarWorldForgingHeavenShip;
import com.gtlsupb.gtlsupb.common.machine.multiblock.module.ForgingShipModuleBase;
import com.gtlsupb.gtlsupb.common.machine.multiblock.module.MicrocosmFoundryModuleBase;
import com.gtlsupb.gtlsupb.utils.RainbowText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

@Mixin(ParallelProvider.class)
public abstract class ParallelProviderMixin {

    @Unique
    private static final long GTLSB_INF_FEATURE = -1919810;

    @Inject(method = "appendServerData(Lnet/minecraft/nbt/CompoundTag;Lsnownee/jade/api/BlockAccessor;)V",
            at = @At("TAIL"),
            remap = false)
    private void gtlSupb$appendServerData(CompoundTag tag, BlockAccessor blockAccessor, CallbackInfo ci) {
        if (blockAccessor.getBlockEntity() instanceof MetaMachineBlockEntity blockEntity) {
            var machine = blockEntity.getMetaMachine();
            boolean isInfinite;
            if (machine instanceof HypercubePhaseStirringCriticalPolymerizationCoreTower m) {
                isInfinite = m.getMaxParallel() == Integer.MAX_VALUE;
            } else if (machine instanceof GTLSupbMolecularAssembler m) {
                isInfinite = m.getMaxParallel() == Integer.MAX_VALUE;
            } else if (machine instanceof MicrocosmFoundry) {
                isInfinite = true;
            } else if (machine instanceof MicrocosmFoundryModuleBase m) {
                isInfinite = m.getMaxParallel() == Integer.MAX_VALUE;
            } else if (machine instanceof SuperStellarWorldForgingHeavenShip) {
                isInfinite = true;
            } else if (machine instanceof ForgingShipModuleBase m) {
                isInfinite = m.getMaxParallel() == Integer.MAX_VALUE;
            } else if (machine instanceof MultiTypeElectricMultiblockMachine m) {
                isInfinite = m.getMaxParallel() == Integer.MAX_VALUE;
            } else {
                isInfinite = false;
            }
            if (isInfinite) {
                tag.putLong("parallel", GTLSB_INF_FEATURE);
            }
        }
    }

    @Inject(method = "appendTooltip", at = @At("HEAD"), cancellable = true, remap = false)
    private void gtlSupb$appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config, CallbackInfo ci) {
        CompoundTag serverData = accessor.getServerData();
        if (serverData.contains("parallel")) {
            long parallel = serverData.getLong("parallel");
            if (parallel == GTLSB_INF_FEATURE) {
                tooltip.add(Component.translatable(
                        "gtceu.multiblock.parallel",
                        RainbowText.createLanguageRainbowComponent(
                                Component.translatable("gtlsupb.machine.infinite.parallel"))));
                ci.cancel();
            }
        }
    }
}
