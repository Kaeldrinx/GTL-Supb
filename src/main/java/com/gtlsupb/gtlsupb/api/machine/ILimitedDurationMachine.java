package com.gtlsupb.gtlsupb.api.machine;

public interface ILimitedDurationMachine {

    int MIN_DURATION = 1;
    int MAX_DURATION = 200;
    int DEFAULT_DURATION = 20;

    default int getLimitedDuration() {
        return DEFAULT_DURATION;
    }

    default void setLimitedDuration(int duration) {}
}
