package com.gtlsupb.gtlsupb.api.machine;

public interface IBatchProcessingMachine {

    boolean isBatchEnabled();

    void setBatchEnabled(boolean enabled);

    int getBatchSize();

    void setBatchSize(int size);
}
