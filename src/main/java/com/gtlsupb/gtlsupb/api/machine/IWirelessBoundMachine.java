package com.gtlsupb.gtlsupb.api.machine;

import java.math.BigInteger;
import java.util.UUID;

public interface IWirelessBoundMachine {

    UUID getUuid();

    void setUuid(UUID uuid);

    boolean isWirelessBound();

    BigInteger getWirelessEnergy();
}
