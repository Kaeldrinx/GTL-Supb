package com.gtlsupb.gtlsupb.integration.jade;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import com.gtlsupb.gtlsupb.GTLSupb;
import com.gtlsupb.gtlsupb.common.data.GTLSupbProviderManager;
import com.gtlsupb.gtlsupb.common.machine.generator.VoidEnergyAbsorberMachine;
import com.gtlsupb.gtlsupb.common.machine.multiblock.FloatingPointEngineMachine;
import com.gtlsupb.gtlsupb.common.machine.multiblock.HolographicStorageArrayMachine;
import com.gtlsupb.gtlsupb.common.machine.multiblock.HyperdimensionalEnergyStabilizationHub;
import com.gtlsupb.gtlsupb.common.machine.multiblock.part.EfficiencyHatchPartMachine;
import com.gtlsupb.gtlsupb.common.machine.multiblock.part.EnergyBoostHatchPartMachine;
import com.gtlsupb.gtlsupb.common.machine.multiblock.part.MEDrivePartMachine;
import com.gtlsupb.gtlsupb.common.machine.multiblock.part.OverclockHatchPartMachine;
import com.gtlsupb.gtlsupb.common.machine.multiblock.part.OverlimitComputeReceiveHatchPartMachine;
import com.gtlsupb.gtlsupb.common.machine.multiblock.part.OverlimitDataReceiveHatchPartMachine;
import com.gtlsupb.gtlsupb.common.machine.multiblock.part.OverlimitFTBQuestIntegrationHatchPartMachine;
import com.gtlsupb.gtlsupb.common.machine.multiblock.part.OverlimitQuantumEntanglementHatchPartMachine;
import com.gtlsupb.gtlsupb.utils.RainbowText;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.Locale;
import java.util.UUID;

public class HatchJadeProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    public static final ResourceLocation UID = GTLSupb.id("hatch_provider");

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        MetaMachine machine = getMachine(accessor);
        if (machine == null) return;

        CompoundTag tag = new CompoundTag();
        if (machine instanceof OverlimitDataReceiveHatchPartMachine hatch) {
            tag.putString("type", "data");
            tag.putBoolean("available", GTLSupbProviderManager.getDataProviderCount(hatch.getOwnerUuid()) > 0);
            writeOwner(tag, accessor.getLevel(), hatch.getOwnerUuid());
        } else if (machine instanceof OverlimitComputeReceiveHatchPartMachine hatch) {
            tag.putString("type", "compute");
            tag.putBoolean("available", GTLSupbProviderManager.getComputeProviderCount(hatch.getOwnerUuid()) > 0);
            writeOwner(tag, accessor.getLevel(), hatch.getOwnerUuid());
        } else if (machine instanceof HolographicStorageArrayMachine provider) {
            tag.putString("type", "holographic_provider");
            tag.putBoolean("working", provider.getRecipeLogic().isWorking());
            writeOwner(tag, accessor.getLevel(), provider.getOwnerUuid());
        } else if (machine instanceof FloatingPointEngineMachine provider) {
            tag.putString("type", "compute_provider");
            tag.putBoolean("working", provider.getRecipeLogic().isWorking());
            writeOwner(tag, accessor.getLevel(), provider.getOwnerUuid());
        } else if (machine instanceof OverclockHatchPartMachine) {
            tag.putString("type", "overclock");
        } else if (machine instanceof EfficiencyHatchPartMachine) {
            tag.putString("type", "efficiency");
        } else if (machine instanceof EnergyBoostHatchPartMachine) {
            tag.putString("type", "boost");
        } else if (machine instanceof VoidEnergyAbsorberMachine voidEnergy) {
            tag.putString("type", "void_energy");
            writeOwner(tag, accessor.getLevel(), voidEnergy.gtlsupb$getOwnerUuid());
        } else if (machine instanceof HyperdimensionalEnergyStabilizationHub hub) {
            tag.putString("type", "energy_hub");
            writeOwner(tag, accessor.getLevel(), hub.gtlsupb$getOwnerUuid());
        } else if (machine instanceof OverlimitFTBQuestIntegrationHatchPartMachine hatch) {
            tag.putString("type", "ftb_quest");
            writeOwner(tag, accessor.getLevel(), hatch.getOwnerUuid());
        } else if (machine instanceof OverlimitQuantumEntanglementHatchPartMachine hatch) {
            tag.putString("type", "quantum_entanglement");
            writeOwner(tag, accessor.getLevel(), hatch.getOwnerUuid());
        } else if (machine instanceof MEDrivePartMachine drive) {
            tag.putString("type", "me_drive");
            writeOwner(tag, accessor.getLevel(), drive.getOwnerUuid());
        } else {
            return;
        }
        data.put(UID.toString(), tag);
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        MetaMachine machine = getMachine(accessor);
        if (machine == null) return;

        CompoundTag tag = accessor.getServerData() == null ? null : accessor.getServerData().getCompound(UID.toString());
        if (tag == null || !tag.contains("type")) return;

        if (machine instanceof OverlimitDataReceiveHatchPartMachine hatch) {
            boolean available = tag.getBoolean("available");
            tooltip.add(rainbow(Component.translatable(
                    available ? "gtlsupb.jade.hatch_data_available" : "gtlsupb.jade.hatch_data_unavailable")));
            appendOwner(tooltip, tag, hatch.getOwnerUuid());
        } else if (machine instanceof OverlimitComputeReceiveHatchPartMachine hatch) {
            boolean available = tag.getBoolean("available");
            tooltip.add(rainbow(Component.translatable(
                    available ? "gtlsupb.jade.hatch_compute_available" : "gtlsupb.jade.hatch_compute_unavailable")));
            appendOwner(tooltip, tag, hatch.getOwnerUuid());
        } else if (machine instanceof HolographicStorageArrayMachine provider) {
            boolean working = tag.getBoolean("working");
            tooltip.add(rainbow(Component.translatable(
                    working ? "gtlsupb.jade.provider_data_active" : "gtlsupb.jade.provider_data_inactive")));
            appendOwner(tooltip, tag, provider.getOwnerUuid());
        } else if (machine instanceof FloatingPointEngineMachine provider) {
            boolean working = tag.getBoolean("working");
            tooltip.add(rainbow(Component.translatable(
                    working ? "gtlsupb.jade.provider_compute_active" : "gtlsupb.jade.provider_compute_inactive")));
            appendOwner(tooltip, tag, provider.getOwnerUuid());
        } else if (machine instanceof OverclockHatchPartMachine hatch) {
            tooltip.add(rainbow(Component.translatable("gtlsupb.jade.hatch_overclock", hatch.getOverclockTierBoost())));
        } else if (machine instanceof EfficiencyHatchPartMachine hatch) {
            tooltip.add(rainbow(Component.translatable("gtlsupb.jade.hatch_efficiency",
                    String.format(Locale.ROOT, "%.2f", hatch.getEuEfficiency()))));
        } else if (machine instanceof EnergyBoostHatchPartMachine hatch) {
            tooltip.add(rainbow(Component.translatable("gtlsupb.jade.hatch_boost", hatch.getEnergyBoostMultiplier())));
        } else if (machine instanceof VoidEnergyAbsorberMachine voidEnergy) {
            appendOwner(tooltip, tag, voidEnergy.gtlsupb$getOwnerUuid());
        } else if (machine instanceof HyperdimensionalEnergyStabilizationHub hub) {
            appendOwner(tooltip, tag, hub.gtlsupb$getOwnerUuid());
        } else if (machine instanceof OverlimitFTBQuestIntegrationHatchPartMachine hatch) {
            appendOwner(tooltip, tag, hatch.getOwnerUuid());
        } else if (machine instanceof OverlimitQuantumEntanglementHatchPartMachine hatch) {
            appendOwner(tooltip, tag, hatch.getOwnerUuid());
        } else if (machine instanceof MEDrivePartMachine drive) {
            appendOwner(tooltip, tag, drive.getOwnerUuid());
        }
    }

    private static MetaMachine getMachine(BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof MetaMachineBlockEntity be)) return null;
        return be.getMetaMachine();
    }

    private static Component rainbow(Component component) {
        return RainbowText.createRainbowComponent(component.getString());
    }

    private static void writeOwner(CompoundTag tag, Level level, UUID ownerUuid) {
        if (ownerUuid == null) return;
        tag.putUUID("owner", ownerUuid);
        if (level != null && !level.isClientSide && level.getServer() != null) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(ownerUuid);
            if (player != null) {
                tag.putString("owner_name", player.getGameProfile().getName());
            }
        }
    }

    private static void appendOwner(ITooltip tooltip, CompoundTag tag, UUID ownerUuid) {
        if (ownerUuid == null) {
            tooltip.add(rainbow(Component.translatable("gtlsupb.jade.hatch_unbound")));
            return;
        }
        String name = tag.getString("owner_name");
        tooltip.add(rainbow(Component.translatable("gtlsupb.jade.hatch_bound_player",
                name.isEmpty() ? ownerUuid.toString() : name)));
    }
}
