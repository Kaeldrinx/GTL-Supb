package com.gtlsupb.gtlsupb.common.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import com.gtlsupb.gtlsupb.api.machine.trait.IWirelessNetworkEnergyHandler;
import com.hepdd.gtmthings.api.misc.WirelessEnergyManager;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class NetworkEnergyContainer extends NotifiableRecipeHandlerTrait<Long>
                                    implements IWirelessNetworkEnergyHandler {

    private UUID uuid;

    public NetworkEnergyContainer(MetaMachine machine) {
        super(machine);
    }

    @Override
    public IO getHandlerIO() {
        return IO.IN;
    }

    @Override
    public boolean isOnline() {
        return uuid != null;
    }

    @Override
    public BigInteger getMaxAvailableEnergy() {
        if (uuid == null) {
            return BigInteger.ZERO;
        }
        return WirelessEnergyManager.getUserEU(uuid);
    }

    public void bind(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUUID() {
        return uuid;
    }

    @Override
    public boolean consumeEnergy(long energy) {
        if (uuid == null) {
            return false;
        }
        return WirelessEnergyManager.addEUToGlobalEnergyMap(uuid, -energy, getMachine());
    }

    @Override
    public RecipeCapability<Long> getCapability() {
        return EURecipeCapability.CAP;
    }

    @Override
    public List<Object> getContents() {
        BigInteger stored = getMaxAvailableEnergy();
        return Collections.singletonList((Object) (stored.bitLength() <= 63 ? stored.longValue() : Long.MAX_VALUE));
    }

    @Override
    public double getTotalContentAmount() {
        return getMaxAvailableEnergy().doubleValue();
    }

    @Override
    public List<Long> handleRecipeInner(
                                        IO io,
                                        GTRecipe recipe,
                                        List<Long> left,
                                        String slotName,
                                        boolean simulate) {
        if (io != IO.IN) return left;

        long sum = 0L;
        for (long v : left) sum += v;
        if (sum <= 0) return null;

        if (uuid == null) return left;

        BigInteger stored = WirelessEnergyManager.getUserEU(uuid);

        long storedLong = stored.bitLength() <= 63 ? stored.longValue() : Long.MAX_VALUE;
        long actual = sum < storedLong ? sum : storedLong;

        if (!simulate && actual > 0) {
            consumeEnergy(actual);
        }
        sum -= actual;
        return sum <= 0 ? null : Collections.singletonList(sum);
    }
}
