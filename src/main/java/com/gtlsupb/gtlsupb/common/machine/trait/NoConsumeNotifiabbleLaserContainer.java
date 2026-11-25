package com.gtlsupb.gtlsupb.common.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableLaserContainer;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import com.hepdd.gtmthings.api.misc.WirelessEnergyManager;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class NoConsumeNotifiabbleLaserContainer extends NotifiableLaserContainer {

    public UUID owner_uuid;

    public NoConsumeNotifiabbleLaserContainer(MetaMachine machine, long capacity, long voltage, long amperage,
                                              long maxInputVoltage, long maxInputAmperage) {
        super(machine, capacity, voltage, amperage, maxInputVoltage, maxInputAmperage);
    }

    public static NoConsumeNotifiabbleLaserContainer emitterContainer(MetaMachine machine, long capacity,
                                                                      long voltage, long amperage) {
        return new NoConsumeNotifiabbleLaserContainer(machine, capacity, 0, 0, voltage, amperage);
    }

    public static NoConsumeNotifiabbleLaserContainer receiverContainer(MetaMachine machine, long capacity,
                                                                       long voltage, long amperage) {
        return new NoConsumeNotifiabbleLaserContainer(machine, capacity, voltage, amperage, 0, 0);
    }

    @Override
    public List<Long> handleRecipeInner(IO io, GTRecipe recipe, List<Long> leftDuration, String slotName,
                                        boolean simulate) {
        long totalEu = leftDuration.stream().reduce(0L, Long::sum);
        if (io == IO.IN) {

            if (owner_uuid != null) {
                long pool = gtlsupb$poolAsLong(owner_uuid);
                long provided = Math.min(pool, totalEu);
                if (!simulate && provided > 0) {
                    if (!WirelessEnergyManager.addEUToGlobalEnergyMap(owner_uuid, -provided, getMachine())) {

                        return Collections.singletonList(totalEu);
                    }
                }
                long left = totalEu - provided;
                return left > 0 ? Collections.singletonList(left) : null;
            }

            long currentStored = getEnergyStored();
            if (!simulate) {
                addEnergy(-Math.min(currentStored, totalEu));
            }
            long left = totalEu - currentStored;
            return left > 0 ? Collections.singletonList(left) : null;
        } else if (io == IO.OUT) {

            long currentCapacity = getEnergyCapacity() - getEnergyStored();
            long deposit = Math.min(currentCapacity, totalEu);
            if (!simulate) {
                if (owner_uuid == null || !WirelessEnergyManager.addEUToGlobalEnergyMap(owner_uuid, deposit, getMachine())) {
                    addEnergy(deposit);
                }
            }
            totalEu -= currentCapacity;
        }
        return totalEu > 0 ? Collections.singletonList(totalEu) : null;
    }

    private static long gtlsupb$poolAsLong(UUID owner) {
        if (owner == null) return 0L;
        try {
            java.math.BigInteger pool = WirelessEnergyManager.getUserEU(owner);
            if (pool == null || pool.signum() <= 0) return 0L;
            return pool.bitLength() <= 63 ? pool.longValue() : Long.MAX_VALUE;
        } catch (Throwable ignored) {
            return 0L;
        }
    }

    @Override
    public long getEnergyStored() {
        if (isInputSide() && owner_uuid != null) {
            long pool = gtlsupb$poolAsLong(owner_uuid);
            long capacity = getEnergyCapacity();
            return capacity > 0L ? Math.min(capacity, pool) : pool;
        }
        return super.getEnergyStored();
    }

    @Override
    public long removeEnergy(long energyToRemove) {
        if (isInputSide() && owner_uuid != null && energyToRemove > 0L) {
            long removed = Math.min(getEnergyStored(), energyToRemove);
            if (removed > 0L) {
                try {
                    WirelessEnergyManager.addEUToGlobalEnergyMap(owner_uuid, -removed, getMachine());
                } catch (Throwable ignored) {
                    return 0L;
                }
            }
            return removed;
        }
        return super.removeEnergy(energyToRemove);
    }

    @Override
    public long changeEnergy(long differenceAmount) {
        if (isInputSide() && owner_uuid != null && differenceAmount < 0L) {
            return -removeEnergy(-differenceAmount);
        }
        return super.changeEnergy(differenceAmount);
    }

    private boolean isInputSide() {
        try {
            return getInputVoltage() > 0L;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public void setOwner_uuid(UUID owner_uuid) {
        this.owner_uuid = owner_uuid;
    }

    public UUID getOwner_uuid() {
        return this.owner_uuid;
    }
}
