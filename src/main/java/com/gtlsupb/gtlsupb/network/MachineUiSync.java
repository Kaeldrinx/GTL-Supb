package com.gtlsupb.gtlsupb.network;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;

import com.gtlsupb.gtlsupb.api.machine.ui.MachineUiHost;

import java.util.ArrayList;
import java.util.List;

public final class MachineUiSync {

    private MachineUiSync() {}

    public static void send(ServerPlayer player, BlockPos pos, MachineUiHost host) {
        if (player == null || host == null) return;
        CompoundTag payload = new CompoundTag();
        payload.putString(S2CMachineUiPacket.KEY_TITLE, host.uiTitleKey());

        List<ItemStack> stacks = new ArrayList<>();
        int slots = Math.max(0, host.uiSlotCount());
        for (int i = 0; i < slots; i++) {
            stacks.add(host.uiGetSlot(i));
        }
        payload.put(S2CMachineUiPacket.KEY_ITEMS, S2CMachineUiPacket.writeItems(stacks));

        CompoundTag flags = new CompoundTag();
        for (MachineUiHost.UiButton button : host.uiButtons()) {
            flags.putBoolean(button.id(), host.uiButtonState(button.id()));
        }
        payload.put(S2CMachineUiPacket.KEY_FLAGS, flags);
        payload.putInt(S2CMachineUiPacket.KEY_ROWS, Math.max(1, host.uiSlotsPerRow()));

        ListTag info = new ListTag();
        for (String line : host.uiInfoLines()) {
            info.add(StringTag.valueOf(line == null ? "" : line));
        }
        payload.put(S2CMachineUiPacket.KEY_INFO, info);

        GTLSupbNetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new S2CMachineUiPacket(pos, payload));
    }
}
