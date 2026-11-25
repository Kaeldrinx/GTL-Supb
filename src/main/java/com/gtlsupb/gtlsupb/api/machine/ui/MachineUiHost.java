package com.gtlsupb.gtlsupb.api.machine.ui;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface MachineUiHost {

    String uiTitleKey();

    int uiSlotCount();

    default int uiSlotsPerRow() {
        return 9;
    }

    ItemStack uiGetSlot(int index);

    void uiSetSlot(int index, ItemStack stack);

    default List<UiButton> uiButtons() {
        return List.of();
    }

    default boolean uiButtonState(String id) {
        return false;
    }

    default void uiOnButton(String id, boolean shift) {}

    default List<String> uiInfoLines() {
        return List.of();
    }

    record UiButton(String id, String labelKey, boolean toggle) {}
}
