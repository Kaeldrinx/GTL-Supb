package com.gtlsupb.gtlsupb.api.machine;

import com.gtlsupb.gtlsupb.api.machine.trait.IWirelessNetworkEnergyHandler;

public interface IWirelessElectricMultiblockMachine {

    default IWirelessNetworkEnergyHandler getWirelessNetworkEnergyHandler() {
        return null;
    }

    default void setWirelessNetworkEnergyHandler(IWirelessNetworkEnergyHandler trait) {}
}
