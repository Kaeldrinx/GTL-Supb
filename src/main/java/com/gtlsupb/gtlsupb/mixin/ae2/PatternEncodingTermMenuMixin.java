package com.gtlsupb.gtlsupb.mixin.ae2;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import appeng.api.crafting.PatternDetailsHelper;
import appeng.crafting.pattern.AEProcessingPattern;
import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.menu.slot.RestrictedInputSlot;
import com.gtlsupb.gtlsupb.GTLSupb;
import com.gtlsupb.gtlsupb.api.item.tool.ae2.Ae2PatternHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PatternEncodingTermMenu.class)
public abstract class PatternEncodingTermMenuMixin {

    @Shadow(remap = false)
    private RestrictedInputSlot encodedPatternSlot;

    @Inject(method = "encode", remap = false, at = @At("TAIL"))
    private void gtlsupb$bindRecipeOnEncode(CallbackInfo ci) {
        try {
            ItemStack pattern = this.encodedPatternSlot.getItem();
            if (pattern.isEmpty() || !PatternDetailsHelper.isEncodedPattern(pattern)) return;

            Player player = ((appeng.menu.AEBaseMenu) (Object) this).getPlayer();
            var details = PatternDetailsHelper.decodePattern(pattern, player.level(), false);
            if (!(details instanceof AEProcessingPattern processingPattern)) return;

            var recipe = Ae2PatternHelper.matchPatternRecipe(processingPattern, player.level());
            if (recipe == null) return;

            Ae2PatternHelper.writeRecipeBinding(pattern, recipe, player.level());
        } catch (Throwable t) {

            GTLSupb.LOGGER.warn("[PatternEncodingMixin] 样板配方绑定失败: {}", t.getMessage());
        }
    }
}
