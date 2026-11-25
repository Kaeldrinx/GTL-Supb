package com.gtlsupb.gtlsupb.common.machine.multiblock;

import org.gtlcore.gtlcore.api.machine.multiblock.NoEnergyMultiblockMachine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IInteractedMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.common.data.GTItems;

import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.registries.ForgeRegistries;

import com.gtlsupb.gtlsupb.api.machine.VoltageUnlockManager;
import com.hepdd.gtmthings.utils.TeamUtil;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class UniversalVoltageRegulator extends NoEnergyMultiblockMachine implements IInteractedMachine {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            UniversalVoltageRegulator.class,
            NoEnergyMultiblockMachine.MANAGED_FIELD_HOLDER);

    private static final List<String> TIER_NAMES = List.of(
            "ulv", "lv", "mv", "hv", "ev", "iv", "luv", "zpm", "uv",
            "uhv", "uev", "uiv", "uxv", "opv", "max");

    @Persisted
    public UUID uuid;

    public boolean isBound() {
        return uuid != null;
    }

    @Persisted
    protected final NotifiableItemStackHandler circuitInventory;

    private com.lowdragmc.lowdraglib.syncdata.ISubscription circuitSub;

    public UniversalVoltageRegulator(IMachineBlockEntity holder) {
        super(holder);
        this.circuitInventory = new NotifiableItemStackHandler(this, 2, IO.NONE, IO.BOTH);

        this.circuitInventory.setFilter(stack -> getCircuitTier(stack) >= 0);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (uuid == null && !isRemote()) {
            tryAutoBind();
        }
        if (!isRemote()) {
            if (getLevel() instanceof ServerLevel serverLevel) {
                VoltageUnlockManager.getSavedData(serverLevel);
            }
        }
    }

    private void tryAutoBind() {
        if (!(getLevel() instanceof ServerLevel serverLevel)) return;
        var nearest = serverLevel.players().stream()
                .min(Comparator.comparingDouble(p -> p.distanceToSqr(getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5)))
                .orElse(null);
        if (nearest != null && nearest.distanceToSqr(getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5) <= 64.0) {
            uuid = nearest.getUUID();
            updateVoltageTier();
            nearest.sendSystemMessage(
                    Component.translatable(
                            "gtlsupb.machine.universal_voltage_regulator.auto_bind",
                            TeamUtil.GetName(nearest)));
        }
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        if (!isRemote() && circuitSub == null) {
            circuitSub = circuitInventory.addChangedListener(this::updateVoltageTier);
        }
        updateVoltageTier();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        clearVoltageUnlock();
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (circuitSub != null) {
            circuitSub.unsubscribe();
            circuitSub = null;
        }
        clearVoltageUnlock();
    }

    @Override
    public void onPartUnload() {
        super.onPartUnload();
        clearVoltageUnlock();
    }

    private void clearVoltageUnlock() {
        if (!isRemote() && uuid != null) {
            VoltageUnlockManager.removeTier(uuid);
        }
    }

    @Override
    public InteractionResult onUse(BlockState state, Level world, BlockPos pos, Player player,
                                   InteractionHand hand, BlockHitResult hit) {
        if (player.getItemInHand(hand).is(GTItems.TOOL_DATA_STICK.asItem())) {
            uuid = player.getUUID();
            if (!isRemote()) {
                updateVoltageTier();
            }
            if (isRemote()) {
                player.sendSystemMessage(
                        Component.translatable(
                                "gtlsupb.machine.universal_voltage_regulator.wireless_bind",
                                TeamUtil.GetName(player)));
            }
            return InteractionResult.sidedSuccess(isRemote());
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean onLeftClick(Player player, Level world, InteractionHand hand, BlockPos pos, Direction direction) {
        var stack = player.getItemInHand(hand);
        if (stack.isEmpty()) return false;
        if (stack.is(GTItems.TOOL_DATA_STICK.asItem())) {
            uuid = null;
            if (!isRemote()) {
                updateVoltageTier();
            }
            if (isRemote()) {
                player.sendSystemMessage(
                        Component.translatable("gtlsupb.machine.universal_voltage_regulator.wireless_unbind"));
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        if (player != null && hand != null && player.getItemInHand(hand).is(GTItems.TOOL_DATA_STICK.asItem())) {
            return false;
        }
        return true;
    }

    public int getCircuitTier(ItemStack stack) {
        if (stack.isEmpty()) return -1;

        var resourceLocation = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (resourceLocation == null) return -1;
        String path = resourceLocation.getPath().toLowerCase();
        for (int index = 0; index < TIER_NAMES.size(); index++) {
            String name = TIER_NAMES.get(index);
            if (path.equals(name + "_universal_circuit")) {
                return index;
            }
        }
        return -1;
    }

    private void updateVoltageTier() {
        UUID id = uuid;
        if (id == null) return;
        int tier1 = getCircuitTier(circuitInventory.getStackInSlot(0));
        int tier2 = getCircuitTier(circuitInventory.getStackInSlot(1));

        int newTier;
        if (tier1 >= 0 && tier2 >= 0 && tier1 == tier2) {

            newTier = Math.min(tier1 + 1, GTValues.VN.length - 1);
        } else if (tier1 >= 0) {
            newTier = tier1;
        } else if (tier2 >= 0) {
            newTier = tier2;
        } else {
            newTier = -1;
        }

        if (newTier >= 0) {
            VoltageUnlockManager.setTierDirect(id, newTier);
        } else {
            VoltageUnlockManager.removeTier(id);
        }

        if (VoltageUnlockManager.isDirty) {
            VoltageUnlockManager.isDirty = false;
            if (getLevel() instanceof ServerLevel serverLevel) {
                VoltageUnlockManager.getSavedData(serverLevel).setDirty();
            }
        }
    }

    public int getCurrentUnlockedTier() {
        UUID id = uuid;
        if (id == null) return -1;
        return VoltageUnlockManager.getTier(id);
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (isFormed()) {

            if (isBound()) {
                textList.add(
                        Component.translatable("gtlsupb.machine.universal_voltage_regulator.wireless_bound")
                                .withStyle(ChatFormatting.GREEN));

                int unlockedTier = getCurrentUnlockedTier();
                if (unlockedTier >= 0) {
                    textList.add(
                            Component.translatable(
                                    "gtlsupb.machine.universal_voltage_regulator.unlocked_tier",
                                    Component.literal(GTValues.VN[unlockedTier]).withStyle(ChatFormatting.LIGHT_PURPLE),
                                    Component.literal(String.valueOf(GTValues.V[unlockedTier])).withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.GRAY));
                } else {
                    textList.add(
                            Component.translatable("gtlsupb.machine.universal_voltage_regulator.no_unlock")
                                    .withStyle(ChatFormatting.DARK_GRAY));
                }

                int tier1 = getCircuitTier(circuitInventory.getStackInSlot(0));
                int tier2 = getCircuitTier(circuitInventory.getStackInSlot(1));
                if (tier1 >= 0 && tier2 >= 0 && tier1 == tier2) {
                    int nextTier = Math.min(tier1 + 1, GTValues.VN.length - 1);
                    textList.add(
                            Component.translatable(
                                    "gtlsupb.machine.universal_voltage_regulator.boost_mode",
                                    Component.literal(GTValues.VN[tier1]).withStyle(ChatFormatting.AQUA),
                                    Component.literal(GTValues.VN[nextTier]).withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.GRAY));
                }
            } else {
                textList.add(
                        Component.translatable("gtlsupb.machine.universal_voltage_regulator.not_bound")
                                .withStyle(ChatFormatting.RED));
            }
        }
    }

    @Override
    public Widget createUIWidget() {
        WidgetGroup group = new WidgetGroup(0, 0, 182 + 8, 117 + 8);
        DraggableScrollableWidgetGroup scrollGroup = new DraggableScrollableWidgetGroup(4, 4, 182, 117);
        scrollGroup.setBackground(getScreenTexture());
        scrollGroup.addWidget(new LabelWidget(4, 5, getBlockState().getBlock().getDescriptionId()));
        scrollGroup.addWidget(
                new ComponentPanelWidget(4, 17, this::addDisplayText)
                        .setMaxWidthLimit(150));

        scrollGroup.addWidget(
                new LabelWidget(4, 80, "gtlsupb.machine.universal_voltage_regulator.circuit_slots"));

        scrollGroup.addWidget(new SlotWidget(circuitInventory.storage, 0, 4, 92, true, true).setBackground(GuiTextures.SLOT));
        scrollGroup.addWidget(new SlotWidget(circuitInventory.storage, 1, 22, 92, true, true).setBackground(GuiTextures.SLOT));

        group.addWidget(scrollGroup);
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        return group;
    }
}
