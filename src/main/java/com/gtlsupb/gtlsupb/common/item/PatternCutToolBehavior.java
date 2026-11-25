package com.gtlsupb.gtlsupb.common.item;

import org.gtlcore.gtlcore.common.machine.multiblock.part.ae.MEPatternBufferPartMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.common.item.TooltipBehavior;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;

public class PatternCutToolBehavior extends TooltipBehavior implements IInteractionItem {

    public static final String CUT_TAG = "cut";

    public static final PatternCutToolBehavior INSTANCE = new PatternCutToolBehavior();

    public PatternCutToolBehavior() {
        super(list -> {});
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        int count = gtlsupb$countPatterns(stack);
        tooltip.add(Component.translatable("gtlsupb.item.pattern_cut_tool.count", count)
                .withStyle(count > 0 ? ChatFormatting.GOLD : ChatFormatting.GRAY));
        tooltip.add(Component.translatable("gtlsupb.item.pattern_cut_tool.hint")
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) {

            return gtlsupb$hasCutData(stack) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        if (!(level.getBlockEntity(context.getClickedPos()) instanceof MetaMachineBlockEntity be)) {
            return InteractionResult.PASS;
        }
        if (!(be.getMetaMachine() instanceof MEPatternBufferPartMachine part)) {
            return InteractionResult.PASS;
        }
        CompoundTag behaviours = stack.getTagElement(ToolHelper.BEHAVIOURS_TAG_KEY);
        if (behaviours == null) return InteractionResult.PASS;
        CompoundTag cut = behaviours.getCompound(CUT_TAG);
        if (cut.isEmpty()) return InteractionResult.PASS;

        if (!part.pasteFromTag(cut)) {
            return InteractionResult.FAIL;
        }

        Player player = context.getPlayer();
        if (player == null || !player.isCreative()) {

            stack.shrink(1);
        }
        if (player != null) {
            player.displayClientMessage(Component.translatable("gtlsupb.item.pattern_cut_tool.pasted"), true);
        }
        return InteractionResult.SUCCESS;
    }

    public static boolean gtlsupb$hasCutData(ItemStack stack) {
        CompoundTag behaviours = stack.getTagElement(ToolHelper.BEHAVIOURS_TAG_KEY);
        return behaviours != null && !behaviours.getCompound(CUT_TAG).isEmpty();
    }

    public static int gtlsupb$countPatterns(ItemStack stack) {
        CompoundTag behaviours = stack.getTagElement(ToolHelper.BEHAVIOURS_TAG_KEY);
        if (behaviours == null) return 0;
        CompoundTag cut = behaviours.getCompound(CUT_TAG);
        if (cut.isEmpty()) return 0;
        return cut.getList("patterns", Tag.TAG_COMPOUND).size();
    }
}
