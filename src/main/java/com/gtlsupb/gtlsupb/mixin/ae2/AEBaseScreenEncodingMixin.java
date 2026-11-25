package com.gtlsupb.gtlsupb.mixin.ae2;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

import appeng.client.gui.AEBaseScreen;
import appeng.integration.modules.jeirei.EncodingHelper;
import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.parts.encoding.EncodingMode;
import com.gtlsupb.gtlsupb.GTLSupb;
import com.gtlsupb.gtlsupb.integration.jei.PendingEncoding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(AEBaseScreen.class)
public abstract class AEBaseScreenEncodingMixin {

    private static final int MAX_WAIT_TICKS = 20;

    @Inject(method = "init", at = @At("RETURN"))
    private void gtlsupb$applyPendingEncoding(CallbackInfo ci) {
        if (!((Object) this instanceof AbstractContainerScreen<?> acScreen)) return;
        AbstractContainerMenu menu = acScreen.getMenu();
        if (!(menu instanceof PatternEncodingTermMenu petMenu)) return;

        PendingEncoding pending = PendingEncoding.poll();
        if (pending == null) return;

        if (pending.isCraftingRecipe()) {
            EncodingMode targetMode = gtlsupb$resolveCraftingMode(pending.recipe());
            petMenu.setMode(targetMode);
        }

        gtlsupb$scheduleEncode(petMenu, pending, 0);
    }

    @Unique
    private static EncodingMode gtlsupb$resolveCraftingMode(Object recipe) {
        if (recipe instanceof Recipe<?> r) {
            RecipeType<?> type = r.getType();
            if (type == RecipeType.STONECUTTING) return EncodingMode.STONECUTTING;
            if (type == RecipeType.SMITHING) return EncodingMode.SMITHING_TABLE;
        }
        return EncodingMode.CRAFTING;
    }

    @Unique
    private static void gtlsupb$scheduleEncode(PatternEncodingTermMenu menu, PendingEncoding pending, int attempt) {
        Minecraft mc = Minecraft.getInstance();
        mc.tell(() -> {

            if (attempt >= MAX_WAIT_TICKS) {
                GTLSupb.LOGGER.warn("[EncodingMixin] 等待编码终端模式同步超时，放弃自动编码");
                return;
            }

            if (!(mc.screen instanceof AbstractContainerScreen<?> acScreen) || acScreen.getMenu() != menu) {
                return;
            }

            if (pending.isCraftingRecipe()) {
                EncodingMode targetMode = gtlsupb$resolveCraftingMode(pending.recipe());
                if (menu.getMode() != targetMode) {

                    gtlsupb$scheduleEncode(menu, pending, attempt + 1);
                    return;
                }
            }

            try {
                if (pending.isCraftingRecipe() && pending.recipe() instanceof Recipe<?> recipe) {

                    Predicate<ItemStack> visiblePredicate = stack -> true;
                    EncodingHelper.encodeCraftingRecipe(menu, recipe, pending.genericIngredients(), visiblePredicate);
                } else {

                    EncodingHelper.encodeProcessingRecipe(menu, pending.genericIngredients(), pending.genericResults());
                }
            } catch (Throwable t) {

                GTLSupb.LOGGER.warn("[EncodingMixin] 自动编码失败: {}", t.getMessage());
            }
        });
    }
}
