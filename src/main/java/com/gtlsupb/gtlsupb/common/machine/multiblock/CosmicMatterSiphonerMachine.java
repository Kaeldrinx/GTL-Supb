package com.gtlsupb.gtlsupb.common.machine.multiblock;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;

import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.network.chat.Component;

import com.gtlsupb.gtlsupb.common.data.machines.multiblock.CosmicMatterSiphonerPattern;
import com.gtlsupb.gtlsupb.common.recipe.GTLSupbRecipeTypes;

public class CosmicMatterSiphonerMachine extends ZeroEnergyMultiTypeMachine {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            CosmicMatterSiphonerMachine.class,
            ZeroEnergyMultiTypeMachine.MANAGED_FIELD_HOLDER);

    private static final int MAX_OVERCLOCK_SHIFT = 30;

    @Persisted
    @DescSynced
    private boolean overclockEnabled = false;

    @DescSynced
    private int casingTier = 0;

    public CosmicMatterSiphonerMachine(IMachineBlockEntity holder) {
        super(holder, GTLSupbRecipeTypes.COSMIC_SIPHON_RECIPES);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public RecipeLogic createRecipeLogic(Object... args) {
        return new CosmicMatterSiphonerLogic(this);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        Object tier = getMultiblockState().getMatchContext().get(CosmicMatterSiphonerPattern.TIER_KEY);
        casingTier = tier instanceof Integer ? (int) tier : 0;
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        casingTier = 0;
    }

    public int getCasingTier() {
        return casingTier;
    }

    public long gtlsupb$getOverclockFactor() {
        if (!overclockEnabled) return 1L;
        int shift = Math.max(0, Math.min(MAX_OVERCLOCK_SHIFT, casingTier + 1));
        return 1L << shift;
    }

    public boolean gtlsupb$isOverclockEnabled() {
        return overclockEnabled;
    }

    public void gtlsupb$setOverclockEnabled(boolean value) {
        if (isRemote()) return;
        this.overclockEnabled = value;
        markDirty();
        notifyBlockUpdate();
    }

    @Override
    public double outputMultiplier() {
        return (double) gtlsupb$getOverclockFactor();
    }

    @Override
    public int getMaxParallel() {
        long factor = gtlsupb$getOverclockFactor();
        return factor >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) factor;
    }

    @Override
    public long maxParallel() {
        return gtlsupb$getOverclockFactor();
    }

    @Override
    public long getMaxParallelLong() {
        return gtlsupb$getOverclockFactor();
    }

    @Override
    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        super.attachConfigurators(configuratorPanel);
        configuratorPanel.attachConfigurators(new com.gregtechceu.gtceu.api.gui.fancy.IFancyConfigurator() {

            @Override
            public Component getTitle() {
                return Component.translatable("gtlsupb.machine.cosmic_siphoner.overclock");
            }

            @Override
            public com.lowdragmc.lowdraglib.gui.texture.IGuiTexture getIcon() {
                return new com.lowdragmc.lowdraglib.gui.texture.ResourceTexture(
                        "expatternprovider:textures/item/pattern_modifier.png");
            }

            @Override
            public com.lowdragmc.lowdraglib.gui.widget.Widget createConfigurator() {
                return new WidgetGroup(0, 0, 140, 34)
                        .addWidget(new LabelWidget(4, 2, () -> Component.translatable(
                                "gtlsupb.machine.cosmic_siphoner.overclock.status",
                                casingTier,
                                overclockEnabled ?
                                        Component.translatable("gtlsupb.machine.cosmic_siphoner.overclock.on",
                                                gtlsupb$getOverclockFactor()).getString() :
                                        Component.translatable("gtlsupb.machine.cosmic_siphoner.overclock.off").getString())
                                .getString()))
                        .addWidget(new ButtonWidget(4, 16, 132, 14,
                                new GuiTextureGroup(GuiTextures.BUTTON,
                                        new TextTexture(Component.translatable(
                                                "gtlsupb.machine.cosmic_siphoner.overclock.toggle").getString())),
                                clickData -> com.gtlsupb.gtlsupb.network.GTLSupbNetworkHandler.CHANNEL.sendToServer(
                                        new com.gtlsupb.gtlsupb.network.C2SCosmicSiphonerOverclockPacket(
                                                getPos(), !overclockEnabled))));
            }
        });
    }

    private com.gregtechceu.gtceu.api.machine.TickableSubscription gtlsupb$energySubs;

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            gtlsupb$energySubs = subscribeServerTick(gtlsupb$energySubs, this::gtlsupb$tickEnergy);
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (gtlsupb$energySubs != null) {
            unsubscribe(gtlsupb$energySubs);
            gtlsupb$energySubs = null;
        }
    }

    private void gtlsupb$tickEnergy() {
        if (isRemote() || getLevel() == null) return;
        if (getRecipeLogic() == null || !getRecipeLogic().isWorking()) return;
        if (getOffsetTimer() % 20L != 0L) return;
        java.util.UUID owner = gtlsupb$getEnergyOwner();
        if (owner == null) {
            var nearest = com.gtlsupb.gtlsupb.common.data.PlayerBindingHelper.findNearestPlayer(
                    getLevel(), getPos(), 16.0d);
            if (nearest != null) {
                owner = nearest.getUUID();
                gtlsupb$bindEnergyOwner(owner);
            }
        }
        if (owner == null) return;
        int tier = Math.max(0, Math.min(14, casingTier));
        java.math.BigInteger cost = java.math.BigInteger.valueOf(com.gregtechceu.gtceu.api.GTValues.VA[tier])
                .multiply(java.math.BigInteger.valueOf(gtlsupb$getOverclockFactor()));
        try {
            if (!com.hepdd.gtmthings.api.misc.WirelessEnergyManager.addEUToGlobalEnergyMap(
                    owner, cost.negate(), this)) {
                getRecipeLogic().interruptRecipe();
            }
        } catch (Throwable ignored) {}
    }
}
