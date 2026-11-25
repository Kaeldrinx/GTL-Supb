package com.gtlsupb.gtlsupb.api.machine.logic;

import com.gtlsupb.gtlsupb.api.machine.IWirelessElectricMultiblockMachine;
import com.gtlsupb.gtlsupb.api.machine.trait.IWirelessNetworkEnergyHandler;
import com.gtlsupb.gtlsupb.api.recipe.IWirelessGTRecipe;

import java.math.BigInteger;

public interface IWirelessRecipeLogic {

    default boolean handleWirelessTickInput(IWirelessGTRecipe recipe) {
        BigInteger euTickInputs = recipe.getWirelessEuTickInputs();
        if (euTickInputs == null) return true;

        IWirelessNetworkEnergyHandler trait = getWirelessMachine().getWirelessNetworkEnergyHandler();
        if (trait == null || !trait.isOnline()) return false;

        BigInteger amount = euTickInputs.abs();
        long eu = amount.bitLength() <= 63 ? amount.longValue() : Long.MAX_VALUE;
        return trait.consumeEnergy(eu);
    }

    default IWirelessElectricMultiblockMachine getWirelessMachine() {
        return new IWirelessElectricMultiblockMachine() {};
    }
}
