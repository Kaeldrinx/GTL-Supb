package com.gtlsupb.gtlsupb.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import com.gtlsupb.gtlsupb.GTLSupb;
import com.gtlsupb.gtlsupb.integration.ftbquests.AEQuestSettings;

public final class GTLSupbNetworkHandler {

    public static final GTLSupbNetworkHandler INSTANCE = new GTLSupbNetworkHandler();

    public static final String PROTOCOL_VERSION = "10";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            GTLSupb.id("main"),
            () -> PROTOCOL_VERSION,
            v -> v.equals(PROTOCOL_VERSION),
            v -> v.equals(PROTOCOL_VERSION));

    private static int nextId = 0;

    private GTLSupbNetworkHandler() {}

    public static void init() {
        CHANNEL.registerMessage(
                nextId++,
                S2CAEQuestSettingsPacket.class,
                S2CAEQuestSettingsPacket::encode,
                S2CAEQuestSettingsPacket::decode,
                S2CAEQuestSettingsPacket::handle);

        CHANNEL.registerMessage(
                nextId++,
                C2SAEQuestTogglePacket.class,
                C2SAEQuestTogglePacket::encode,
                C2SAEQuestTogglePacket::decode,
                C2SAEQuestTogglePacket::handle);

        CHANNEL.registerMessage(
                nextId++,
                C2SAEPatternProviderPacket.class,
                C2SAEPatternProviderPacket::encode,
                C2SAEPatternProviderPacket::decode,
                C2SAEPatternProviderPacket::handle);

        CHANNEL.registerMessage(
                nextId++,
                C2SAEQuestSubmitPacket.class,
                C2SAEQuestSubmitPacket::encode,
                C2SAEQuestSubmitPacket::decode,
                C2SAEQuestSubmitPacket::handle);

        CHANNEL.registerMessage(
                nextId++,
                S2CRewardListPacket.class,
                S2CRewardListPacket::encode,
                S2CRewardListPacket::decode,
                S2CRewardListPacket::handle);

        CHANNEL.registerMessage(
                nextId++,
                C2SOpenEncodingTerminalPacket.class,
                C2SOpenEncodingTerminalPacket::encode,
                C2SOpenEncodingTerminalPacket::decode,
                C2SOpenEncodingTerminalPacket::handle);

        CHANNEL.registerMessage(
                nextId++,
                C2SVoidEnergyAbsorberTogglePacket.class,
                C2SVoidEnergyAbsorberTogglePacket::encode,
                C2SVoidEnergyAbsorberTogglePacket::decode,
                C2SVoidEnergyAbsorberTogglePacket::handle);

        CHANNEL.registerMessage(
                nextId++,
                C2SQuantumFilterPacket.class,
                C2SQuantumFilterPacket::encode,
                C2SQuantumFilterPacket::decode,
                C2SQuantumFilterPacket::handle);

        CHANNEL.registerMessage(
                nextId++,
                C2SModuleModeMaskPacket.class,
                C2SModuleModeMaskPacket::encode,
                C2SModuleModeMaskPacket::decode,
                C2SModuleModeMaskPacket::handle);

        CHANNEL.registerMessage(
                nextId++,
                C2SCosmicSiphonerOverclockPacket.class,
                C2SCosmicSiphonerOverclockPacket::encode,
                C2SCosmicSiphonerOverclockPacket::decode,
                C2SCosmicSiphonerOverclockPacket::handle);

        CHANNEL.registerMessage(
                nextId++,
                C2SPatternAssemblyConfigPacket.class,
                C2SPatternAssemblyConfigPacket::encode,
                C2SPatternAssemblyConfigPacket::decode,
                C2SPatternAssemblyConfigPacket::handle);

        CHANNEL.registerMessage(
                nextId++,
                C2SPatternPackPacket.class,
                C2SPatternPackPacket::encode,
                C2SPatternPackPacket::decode,
                C2SPatternPackPacket::handle);

        CHANNEL.registerMessage(
                nextId++,
                C2SPatternNamePacket.class,
                C2SPatternNamePacket::encode,
                C2SPatternNamePacket::decode,
                C2SPatternNamePacket::handle);

        CHANNEL.registerMessage(
                nextId++,
                C2SMachineUiPacket.class,
                C2SMachineUiPacket::encode,
                C2SMachineUiPacket::decode,
                C2SMachineUiPacket::handle);
        CHANNEL.registerMessage(
                nextId++,
                S2CMachineUiPacket.class,
                S2CMachineUiPacket::encode,
                S2CMachineUiPacket::decode,
                S2CMachineUiPacket::handle);

        CHANNEL.registerMessage(
                nextId++,
                C2SPatternModifierPacket.class,
                C2SPatternModifierPacket::encode,
                C2SPatternModifierPacket::decode,
                C2SPatternModifierPacket::handle);
        CHANNEL.registerMessage(
                nextId++,
                S2CPatternModifierPacket.class,
                S2CPatternModifierPacket::encode,
                S2CPatternModifierPacket::decode,
                S2CPatternModifierPacket::handle);

        CHANNEL.registerMessage(
                nextId++,
                C2SSuperTerminalSettingsPacket.class,
                C2SSuperTerminalSettingsPacket::encode,
                C2SSuperTerminalSettingsPacket::decode,
                C2SSuperTerminalSettingsPacket::handle);
        CHANNEL.registerMessage(
                nextId++,
                C2SQuantumTeleportPacket.class,
                C2SQuantumTeleportPacket::encode,
                C2SQuantumTeleportPacket::decode,
                C2SQuantumTeleportPacket::handle);

        CHANNEL.registerMessage(
                nextId++,
                C2SIntegratedHatchPacket.class,
                C2SIntegratedHatchPacket::encode,
                C2SIntegratedHatchPacket::decode,
                C2SIntegratedHatchPacket::handle);

        CHANNEL.registerMessage(
                nextId++,
                C2SIntegratedTagFilterPacket.class,
                C2SIntegratedTagFilterPacket::encode,
                C2SIntegratedTagFilterPacket::decode,
                C2SIntegratedTagFilterPacket::handle);

        CHANNEL.registerMessage(
                nextId++,
                C2SPatternCustomNamePacket.class,
                C2SPatternCustomNamePacket::encode,
                C2SPatternCustomNamePacket::decode,
                C2SPatternCustomNamePacket::handle);

        CHANNEL.registerMessage(
                nextId++,
                C2SIntegratedRequesterPacket.class,
                C2SIntegratedRequesterPacket::encode,
                C2SIntegratedRequesterPacket::decode,
                C2SIntegratedRequesterPacket::handle);
        GTLSupb.LOGGER.info("{} network handler initialized", GTLSupb.NAME);
    }

    public static S2CAEQuestSettingsPacket buildAEQuestSettingsPacket(ServerPlayer player) {
        var server = player.getServer();
        var uuid = player.getUUID();
        return new S2CAEQuestSettingsPacket(
                AEQuestSettings.isDetectionEnabled(server, uuid),
                AEQuestSettings.isRewardEnabled(server, uuid),
                AEQuestSettings.getInterval(server, uuid));
    }
}
