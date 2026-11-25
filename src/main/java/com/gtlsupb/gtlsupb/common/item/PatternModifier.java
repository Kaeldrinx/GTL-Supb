package com.gtlsupb.gtlsupb.common.item;

import org.gtlcore.gtlcore.common.machine.multiblock.part.ae.MEPatternBufferPartMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.item.component.IItemUIFactory;
import com.gregtechceu.gtceu.integration.ae2.gui.widget.AETextInputButtonWidget;

import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import appeng.api.inventories.InternalInventory;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.crafting.pattern.AEProcessingPattern;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import com.gtlsupb.gtlsupb.api.item.tool.ae2.Ae2PatternHelper;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PatternModifier implements IItemUIFactory {

    public static final PatternModifier INSTANCE = new PatternModifier();

    private int scale = 1;
    private int divScale = 1;
    private int appliedNumber = 1;

    private void setScale(String s) {
        try {
            scale = Math.max(Integer.parseInt(s), 1);
        } catch (NumberFormatException e) {
            scale = 1;
        }
    }

    private void setDivScale(String s) {
        try {
            divScale = Math.max(Integer.parseInt(s), 1);
        } catch (NumberFormatException e) {
            divScale = 1;
        }
    }

    private void setAppliedNumber(String s) {
        try {
            int val = Integer.parseInt(s);
            appliedNumber = Math.max(1, Math.min(16, val));
        } catch (NumberFormatException e) {
            appliedNumber = 1;
        }
    }

    public void gtlsupb$applySettings(int scale, int div, int times) {
        this.scale = Math.max(1, scale);
        this.divScale = Math.max(1, div);
        this.appliedNumber = Math.max(1, Math.min(16, times));
    }

    @Override
    public ModularUI createUI(HeldItemUIFactory.HeldItemHolder heldItemHolder, Player player) {
        return new ModularUI(206, 102, heldItemHolder, player)
                .widget(new WidgetGroup(0, 0, 206, 102)
                        .addWidget(new ImageWidget(8, 8, 190, 86, GuiTextures.DISPLAY))
                        .addWidget(new LabelWidget(12, 12, "AE样板倍乘器"))
                        .addWidget(new LabelWidget(12, 22, "设置参数后，Shift+右键样板供应器方块使用"))
                        .addWidget(new LabelWidget(12, 32, "先做乘法，后做除法"))
                        .addWidget(new LabelWidget(12, 42, "若应用次数为3，则代表执行三次应用次数为1的操作"))

                        .addWidget(new AETextInputButtonWidget(120, 50, 72, 12)
                                .setText(String.valueOf(scale))
                                .setOnConfirm(this::setScale)
                                .setButtonTooltips(Component.translatable("gtlsupb.pattern_modifier.scale")))
                        .addWidget(new AETextInputButtonWidget(120, 64, 72, 12)
                                .setText(String.valueOf(divScale))
                                .setOnConfirm(this::setDivScale)
                                .setButtonTooltips(Component.translatable("gtlsupb.pattern_modifier.div_scale")))
                        .addWidget(new AETextInputButtonWidget(120, 78, 72, 12)
                                .setText(String.valueOf(appliedNumber))
                                .setOnConfirm(this::setAppliedNumber)
                                .setButtonTooltips(Component.translatable("gtlsupb.pattern_modifier.applied_number"))))
                .background(GuiTextures.BACKGROUND);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Item item, Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);

        if (level.isClientSide) {
            net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(
                    net.minecraftforge.api.distmarker.Dist.CLIENT,
                    () -> () -> net.minecraft.client.Minecraft.getInstance()
                            .setScreen(new com.gtlsupb.gtlsupb.client.screen.PatternModifierScreen()));
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player contextPlayer = context.getPlayer();
        if (!(contextPlayer instanceof ServerPlayer)) {
            return InteractionResult.FAIL;
        }
        ServerPlayer player = (ServerPlayer) contextPlayer;

        if (player.isShiftKeyDown()) {
            var pos = context.getClickedPos();
            var level = context.getLevel();
            var tile = level.getBlockEntity(pos);
            InternalInventory internalInventory;
            if (tile instanceof CableBusBlockEntity) {
                var cableBusTile = (CableBusBlockEntity) tile;
                var hitVec = context.getClickLocation();
                var hitInBlock = new Vec3(
                        hitVec.x - pos.getX(),
                        hitVec.y - pos.getY(),
                        hitVec.z - pos.getZ());
                var part = cableBusTile.getCableBus().selectPartLocal(hitInBlock).part;
                if (part instanceof PatternProviderLogicHost) {
                    var logic = ((PatternProviderLogicHost) part).getLogic();
                    internalInventory = logic != null ? logic.getPatternInv() : null;
                } else {
                    internalInventory = null;
                }
            } else if (tile instanceof PatternProviderLogicHost) {
                var logic = ((PatternProviderLogicHost) tile).getLogic();
                internalInventory = logic != null ? logic.getPatternInv() : null;
            } else if (tile instanceof MetaMachineBlockEntity && ((MetaMachineBlockEntity) tile).getMetaMachine() instanceof MEPatternBufferPartMachine) {
                internalInventory = ((MEPatternBufferPartMachine) ((MetaMachineBlockEntity) tile).getMetaMachine())
                        .getTerminalPatternInventory();
            } else {
                internalInventory = null;
            }

            if (internalInventory == null) {
                player.displayClientMessage(
                        Component.translatable("gtlsupb.pattern_modifier.pattern_provider_only"),
                        true);
                return InteractionResult.FAIL;
            }

            for (int i = 0; i < appliedNumber; i++) {
                var newItemStackMap = new Int2ObjectOpenHashMap<ItemStack>();
                for (int slot = 0; slot < internalInventory.size(); slot++) {
                    var itemStack = internalInventory.getStackInSlot(slot);
                    if (!itemStack.isEmpty()) {
                        var newPattern = modifyPattern(itemStack, player);
                        if (newPattern != null) {
                            newItemStackMap.put(slot, newPattern);
                        }
                    }
                }
                final InternalInventory inv = internalInventory;
                newItemStackMap.int2ObjectEntrySet().fastForEach(entry -> {
                    int slot = entry.getIntKey();
                    ItemStack itemStack = entry.getValue();
                    inv.extractItem(slot, 1, false);
                    inv.insertItem(slot, itemStack, false);
                });
            }
            player.displayClientMessage(
                    Component.translatable("gtlsupb.pattern_modifier.updated"),
                    true);
        } else {
            player.displayClientMessage(
                    Component.translatable("gtlsupb.pattern_modifier.right_click_air"),
                    true);
        }
        return InteractionResult.SUCCESS;
    }

    private ItemStack modifyPattern(ItemStack patternStack, ServerPlayer player) {
        AEProcessingPattern aePattern = Ae2PatternHelper.INSTANCE.decode(patternStack, player);
        if (aePattern == null) return null;

        ItemStack result = Ae2PatternHelper.INSTANCE.modifyScale(
                aePattern, scale, false, Integer.MAX_VALUE, Integer.MAX_VALUE, Ae2PatternHelper.ModifyMode.OUTPUT);
        if (result == null) return null;

        if (divScale > 1) {
            AEProcessingPattern secondPattern = Ae2PatternHelper.INSTANCE.decode(result, player);
            if (secondPattern == null) return result;
            result = Ae2PatternHelper.INSTANCE.modifyScale(
                    secondPattern, divScale, true, Integer.MAX_VALUE, Integer.MAX_VALUE,
                    Ae2PatternHelper.ModifyMode.OUTPUT);
            if (result == null) return result;
        }

        return result;
    }
}
