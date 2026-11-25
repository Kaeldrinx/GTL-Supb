package com.gtlsupb.gtlsupb.api.machine;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

public class VoltageUnlockSavedData extends SavedData {

    public VoltageUnlockSavedData() {}

    @Override
    public CompoundTag save(CompoundTag tag) {
        return VoltageUnlockManager.saveToNBT();
    }

    public static VoltageUnlockSavedData load(CompoundTag tag) {
        VoltageUnlockSavedData data = new VoltageUnlockSavedData();
        VoltageUnlockManager.loadFromNBT(tag);
        return data;
    }
}
