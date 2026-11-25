package com.gtlsupb.gtlsupb.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.feature.IGridConnectedMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.trait.GridNodeHolder;

import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IManagedGridNode;
import com.gtlsupb.gtlsupb.common.data.PlayerBindingHelper;
import com.gtlsupb.gtlsupb.integration.ftbquests.AEQuestSettings;
import com.gtlsupb.gtlsupb.integration.ftbquests.FTBQuestHatchManager;

import java.util.EnumSet;
import java.util.UUID;

public class OverlimitFTBQuestIntegrationHatchPartMachine extends TieredIOPartMachine
                                                          implements IGridConnectedMachine, IMachineLife {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            OverlimitFTBQuestIntegrationHatchPartMachine.class, TieredIOPartMachine.MANAGED_FIELD_HOLDER);

    private static final int UI_WIDTH = 176;
    private static final int UI_HEIGHT = 108;

    private final GridNodeHolder nodeHolder = new GridNodeHolder(this);

    @DescSynced
    @Persisted
    private boolean isOnline = false;

    @Persisted
    @DescSynced
    private UUID ownerUuid;

    @Persisted
    @DescSynced
    private boolean detectionEnabled = true;

    @Persisted
    @DescSynced
    private boolean autoSubmitEnabled = false;

    @Persisted
    @DescSynced
    private boolean rewardEnabled = true;

    @Persisted
    @DescSynced
    private int detectionInterval = AEQuestSettings.INTERVAL_DEFAULT;

    public OverlimitFTBQuestIntegrationHatchPartMachine(IMachineBlockEntity holder) {
        super(holder, GTValues.MAX, IO.BOTH);
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public boolean isDetectionEnabled() {
        return detectionEnabled;
    }

    public boolean isAutoSubmitEnabled() {
        return autoSubmitEnabled;
    }

    public boolean isRewardEnabled() {
        return rewardEnabled;
    }

    public void gtlsupb$setDetectionEnabled(boolean value) {
        this.detectionEnabled = value;
        markDirty();
    }

    public void gtlsupb$setAutoSubmitEnabled(boolean value) {
        this.autoSubmitEnabled = value;
        markDirty();
    }

    public void gtlsupb$setRewardEnabled(boolean value) {
        this.rewardEnabled = value;
        markDirty();
    }

    public int gtlsupb$getInterval() {
        return detectionInterval;
    }

    public void gtlsupb$setInterval(int value) {
        this.detectionInterval = Math.max(AEQuestSettings.INTERVAL_MIN,
                Math.min(AEQuestSettings.INTERVAL_MAX, value));
        markDirty();
    }

    @Override
    public IManagedGridNode getMainNode() {
        return nodeHolder.getMainNode();
    }

    @Override
    public boolean isOnline() {
        return this.isOnline;
    }

    @Override
    public void setOnline(boolean online) {
        this.isOnline = online;
    }

    @Override
    public boolean shouldSyncME() {
        return false;
    }

    @Override
    public boolean updateMEStatus() {
        this.isOnline = getMainNode().isActive() && getMainNode().isOnline();
        return this.isOnline;
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        updateMEStatus();
        FTBQuestHatchManager.register(this);
    }

    @Override
    public void onRotated(Direction oldFacing, Direction newFacing) {
        super.onRotated(oldFacing, newFacing);
        this.getMainNode().setExposedOnSides(EnumSet.allOf(Direction.class));
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote() && ownerUuid == null) {
            Player nearest = PlayerBindingHelper.findNearestPlayer(getLevel(), getPos(), 8.0d);
            if (nearest != null) {
                ownerUuid = nearest.getUUID();
                markDirty();
            }
        }
        this.getMainNode().setExposedOnSides(EnumSet.allOf(Direction.class));
        FTBQuestHatchManager.register(this);
    }

    @Override
    public void onMachineRemoved() {
        FTBQuestHatchManager.unregister(this);
    }

    private ButtonWidget gtlsupb$toggle(int x, int y, java.util.function.BooleanSupplier getter, String onKey,
                                        String offKey, java.util.function.Consumer<Boolean> setter) {
        return new ButtonWidget(x, y, 108, 16,
                new GuiTextureGroup(GuiTextures.BUTTON,
                        new TextTexture(() -> Component.translatable(getter.getAsBoolean() ? onKey : offKey).getString())),
                clickData -> {
                    setter.accept(!getter.getAsBoolean());
                    markDirty();
                });
    }

    @Override
    public Widget createUIWidget() {
        WidgetGroup group = new WidgetGroup(0, 0, UI_WIDTH, UI_HEIGHT);
        group.addWidget(new LabelWidget(6, 4,
                () -> this.isOnline ? "gtceu.gui.me_network.online" : "gtceu.gui.me_network.offline"));
        group.addWidget(new LabelWidget(6, 18,
                () -> Component.translatable("gtlsupb.jade.hatch_bound_player",
                        ownerUuid == null ? "-" : ownerUuid.toString()).getString()));

        group.addWidget(gtlsupb$toggle(8, 34, this::isDetectionEnabled,
                "gtlsupb.aequest.toggle.detection.on", "gtlsupb.aequest.toggle.detection.off",
                this::gtlsupb$setDetectionEnabled));
        group.addWidget(gtlsupb$toggle(8, 52, this::isAutoSubmitEnabled,
                "gtlsupb.aequest.toggle.auto_submit.on", "gtlsupb.aequest.toggle.auto_submit.off",
                this::gtlsupb$setAutoSubmitEnabled));
        group.addWidget(gtlsupb$toggle(8, 70, this::isRewardEnabled,
                "gtlsupb.aequest.toggle.reward.on", "gtlsupb.aequest.toggle.reward.off",
                this::gtlsupb$setRewardEnabled));

        group.addWidget(new ButtonWidget(8, 88, 52, 16,
                new GuiTextureGroup(GuiTextures.BUTTON,
                        new TextTexture(() -> "-")),
                clickData -> gtlsupb$setInterval(detectionInterval - AEQuestSettings.INTERVAL_STEP)));
        group.addWidget(new LabelWidget(64, 91,
                () -> Component.translatable("gtlsupb.aequest.toggle.interval", detectionInterval).getString()));
        group.addWidget(new ButtonWidget(116, 88, 52, 16,
                new GuiTextureGroup(GuiTextures.BUTTON,
                        new TextTexture(() -> "+")),
                clickData -> gtlsupb$setInterval(detectionInterval + AEQuestSettings.INTERVAL_STEP)));
        return group;
    }
}
