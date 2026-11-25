package com.gtlsupb.gtlsupb.api.machine;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

public class SteamEnergySavedData extends SavedData {

    public SteamEnergySavedData() {}

    @Override
    public CompoundTag save(CompoundTag tag) {
        return SteamEnergyManager.saveToNBT();
    }

    public static SteamEnergySavedData load(CompoundTag tag) {
        SteamEnergySavedData data = new SteamEnergySavedData();
        SteamEnergyManager.loadFromNBT(tag);
        return data;
    }
}
