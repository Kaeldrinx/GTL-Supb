package com.gtlsupb.gtlsupb.network;

import com.gregtechceu.gtceu.api.gui.factory.MachineUIFactory;
import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.helpers.patternprovider.PatternContainer;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.menu.MenuOpener;
import appeng.menu.implementations.PatternProviderMenu;
import appeng.menu.locator.MenuLocator;
import appeng.menu.locator.MenuLocators;
import appeng.menu.me.common.MEStorageMenu;
import appeng.parts.AEBasePart;

import java.util.List;
import java.util.function.Supplier;

public class C2SAEPatternProviderPacket {

    private final int containerId;
    private final AEKey key;

    public C2SAEPatternProviderPacket(int containerId, AEKey key) {
        this.containerId = containerId;
        this.key = key;
    }

    public static void encode(C2SAEPatternProviderPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.containerId);
        AEKey.writeKey(buf, msg.key);
    }

    public static C2SAEPatternProviderPacket decode(FriendlyByteBuf buf) {
        int containerId = buf.readVarInt();
        AEKey key = AEKey.readKey(buf);
        return new C2SAEPatternProviderPacket(containerId, key);
    }

    public static void handle(C2SAEPatternProviderPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || msg.key == null) return;

            if (player.containerMenu == null || player.containerMenu.containerId != msg.containerId) return;
            if (!(player.containerMenu instanceof MEStorageMenu menu)) return;

            IGridNode node = menu.getNetworkNode();
            if (node == null) return;
            IGrid grid = node.getGrid();
            if (grid == null) return;
            ICraftingService crafting = grid.getCraftingService();
            if (crafting == null) return;

            if (crafting.getCraftingFor(msg.key).isEmpty()) {
                player.displayClientMessage(Component.translatable("gtlsupb.ae_pattern.no_pattern"), true);
                return;
            }

            PatternContainer target = null;
            search:
            for (Class<?> machineClass : grid.getMachineClasses()) {
                if (!PatternContainer.class.isAssignableFrom(machineClass)) continue;
                Class<? extends PatternContainer> containerClass = machineClass.asSubclass(PatternContainer.class);
                for (PatternContainer container : grid.getMachines(containerClass)) {
                    List<IPatternDetails> patterns = getPatterns(container);
                    if (patterns == null) continue;
                    for (IPatternDetails pattern : patterns) {
                        GenericStack[] outputs = pattern.getOutputs();
                        if (outputs == null) continue;
                        for (GenericStack out : outputs) {
                            if (out != null && msg.key.equals(out.what())) {
                                target = container;
                                break search;
                            }
                        }
                    }
                }
            }

            if (target == null) {
                player.displayClientMessage(Component.translatable("gtlsupb.ae_pattern.not_found"), true);
                return;
            }

            player.closeContainer();

            if (target instanceof PatternProviderLogicHost host) {
                BlockEntity be = host.getBlockEntity();
                if (be == null || be.getLevel() == null) {
                    player.displayClientMessage(Component.translatable("gtlsupb.ae_pattern.not_found"), true);
                    return;
                }
                ServerLevel destLevel = (ServerLevel) be.getLevel();
                BlockPos pos = be.getBlockPos();
                MenuLocator locator = (host instanceof AEBasePart part) ? MenuLocators.forPart(part) : MenuLocators.forBlockEntity(be);
                player.teleportTo(destLevel, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                        player.getYRot(), player.getXRot());
                MenuOpener.open(PatternProviderMenu.TYPE, player, locator);
                player.displayClientMessage(Component.translatable("gtlsupb.ae_pattern.jumped"), true);
                return;
            }

            if (target instanceof MetaMachine machine) {
                if (!(machine.getLevel() instanceof ServerLevel destLevel)) {
                    player.displayClientMessage(Component.translatable("gtlsupb.ae_pattern.not_found"), true);
                    return;
                }
                BlockPos pos = machine.getPos();
                player.teleportTo(destLevel, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                        player.getYRot(), player.getXRot());
                MachineUIFactory.INSTANCE.openUI(machine, player);
                player.displayClientMessage(Component.translatable("gtlsupb.ae_pattern.jumped"), true);
                return;
            }

            player.displayClientMessage(Component.translatable("gtlsupb.ae_pattern.not_found"), true);
        });
        ctx.get().setPacketHandled(true);
    }

    private static List<IPatternDetails> getPatterns(PatternContainer container) {
        if (container instanceof PatternProviderLogicHost host) {
            var logic = host.getLogic();
            return logic == null ? null : logic.getAvailablePatterns();
        }
        if (container instanceof appeng.api.networking.crafting.ICraftingProvider provider) {
            return provider.getAvailablePatterns();
        }
        return null;
    }
}
