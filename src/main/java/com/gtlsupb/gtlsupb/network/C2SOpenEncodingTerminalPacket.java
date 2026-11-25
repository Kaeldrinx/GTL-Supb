package com.gtlsupb.gtlsupb.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocator;
import appeng.menu.locator.MenuLocators;
import appeng.menu.me.common.MEStorageMenu;
import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.parts.encoding.PatternEncodingTerminalPart;

import java.util.Set;
import java.util.function.Supplier;

public class C2SOpenEncodingTerminalPacket {

    public C2SOpenEncodingTerminalPacket() {}

    public static void encode(C2SOpenEncodingTerminalPacket msg, FriendlyByteBuf buf) {}

    public static C2SOpenEncodingTerminalPacket decode(FriendlyByteBuf buf) {
        return new C2SOpenEncodingTerminalPacket();
    }

    public static void handle(C2SOpenEncodingTerminalPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            if (!(player.containerMenu instanceof MEStorageMenu menu)) {
                return;
            }

            IGridNode node = menu.getNetworkNode();
            if (node == null && menu.getHost() instanceof IActionHost actionHost) {
                node = actionHost.getActionableNode();
            }
            IGrid grid = (node == null) ? null : node.getGrid();
            if (grid == null) {
                player.sendSystemMessage(Component.translatable("gtlsupb.jei.encode.no_encoding_on_grid"));
                return;
            }

            Set<PatternEncodingTerminalPart> terminals = grid.getMachines(PatternEncodingTerminalPart.class);
            for (PatternEncodingTerminalPart part : terminals) {
                MenuLocator locator = MenuLocators.forPart(part);
                if (MenuOpener.open(PatternEncodingTermMenu.TYPE, player, locator)) {
                    return;
                }
            }

            player.sendSystemMessage(Component.translatable("gtlsupb.jei.encode.no_encoding_on_grid"));
        });
        ctx.get().setPacketHandled(true);
    }
}
