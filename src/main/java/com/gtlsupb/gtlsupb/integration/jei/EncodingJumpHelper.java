package com.gtlsupb.gtlsupb.integration.jei;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.Recipe;

import appeng.api.stacks.GenericStack;
import appeng.client.Hotkey;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.HotkeyPacket;
import appeng.integration.modules.jei.GenericEntryStackHelper;
import appeng.integration.modules.jeirei.EncodingHelper;
import appeng.menu.me.common.MEStorageMenu;
import com.gtlsupb.gtlsupb.network.C2SOpenEncodingTerminalPacket;
import com.gtlsupb.gtlsupb.network.GTLSupbNetworkHandler;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class EncodingJumpHelper {

    private static final String WET_HOTKEY_NAME = "wireless_pattern_encoding_terminal";

    private EncodingJumpHelper() {}

    public static boolean tryEncodingJump(IRecipeLayoutDrawable<?> layout, Player player,
                                          AbstractContainerMenu containerMenu) {
        if (layout == null || player == null) return false;
        Object recipe = layout.getRecipe();
        if (recipe == null) return false;

        boolean craftingRecipe = (recipe instanceof Recipe<?> r) && EncodingHelper.isSupportedCraftingRecipe(r);
        boolean hasTerminal = WirelessTerminalDetector.hasWirelessEncodingTerminal(player);
        boolean isInWiredTerminal = !hasTerminal && (containerMenu instanceof MEStorageMenu);

        if (hasTerminal) {

            PendingIngredients cap = captureIngredients(layout.getRecipeSlotsView(), craftingRecipe);
            PendingEncoding.set(recipe, cap.ingredients(), cap.results(), craftingRecipe);
            try {
                NetworkHandler.instance().sendToServer(new HotkeyPacket(new Hotkey(WET_HOTKEY_NAME, null)));
            } catch (Throwable ignored) {

                PendingEncoding.clear();
            }
            return true;
        } else if (isInWiredTerminal) {

            PendingIngredients cap = captureIngredients(layout.getRecipeSlotsView(), craftingRecipe);
            PendingEncoding.set(recipe, cap.ingredients(), cap.results(), craftingRecipe);
            GTLSupbNetworkHandler.CHANNEL.sendToServer(new C2SOpenEncodingTerminalPacket());
            return true;
        }
        return false;
    }

    public static Component noTerminalMessage() {
        return Component.translatable("gtlsupb.jei.encode.no_terminal");
    }

    private static PendingIngredients captureIngredients(IRecipeSlotsView slotsView, boolean craftingRecipe) {
        if (craftingRecipe) {
            List<IRecipeSlotView> recipeSlots = slotsView.getSlotViews();
            ArrayList<List<GenericStack>> result = new ArrayList<>(9);
            for (int i = 0; i < 9; ++i) {
                if (i < recipeSlots.size()) {
                    IRecipeSlotView slot = recipeSlots.get(i);
                    result.add(slot.getIngredients(VanillaTypes.ITEM_STACK)
                            .map(GenericStack::fromItemStack)
                            .filter(Objects::nonNull)
                            .toList());
                } else {
                    result.add(Collections.emptyList());
                }
            }
            return new PendingIngredients(result, Collections.emptyList());
        } else {
            return new PendingIngredients(
                    GenericEntryStackHelper.ofInputs(slotsView),
                    GenericEntryStackHelper.ofOutputs(slotsView));
        }
    }

    private record PendingIngredients(List<List<GenericStack>> ingredients, List<GenericStack> results) {}
}
