package com.gtlsupb.gtlsupb.api.machine.trait;

import java.math.BigInteger;

public interface IWirelessNetworkEnergyHandler {

    boolean isOnline();

    BigInteger getMaxAvailableEnergy();

    boolean consumeEnergy(long energy);
}
