package com.gtlsupb.gtlsupb.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.gtlsupb.gtlsupb.api.item.tool.ae2.Ae2PatternHelper;

@Mod.EventBusSubscriber(modid = "gtlsupb", value = Dist.CLIENT)
public final class PatternBindingTooltip {

    private PatternBindingTooltip() {}

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;
        try {
            ResourceLocation recipeId = Ae2PatternHelper.readRecipeId(stack);
            if (recipeId == null) {

                if (Ae2PatternHelper.hasCustomKeys(stack)) {
                    event.getToolTip().add(Component.translatable("gtlsupb.tooltip.pattern.unbound")
                            .withStyle(ChatFormatting.GRAY));
                }
                return;
            }

            event.getToolTip().add(Component.translatable("gtlsupb.tooltip.pattern.bound",
                    Component.literal(recipeId.toString()).withStyle(ChatFormatting.AQUA))
                    .withStyle(ChatFormatting.GRAY));

            ResourceLocation typeId = Ae2PatternHelper.readRecipeTypeId(stack);
            if (typeId != null) {
                event.getToolTip().add(Component.translatable("gtlsupb.tooltip.pattern.type",
                        Component.literal(typeId.toString()).withStyle(ChatFormatting.DARK_AQUA))
                        .withStyle(ChatFormatting.GRAY));
            }

            boolean resolved = false;
            var level = Minecraft.getInstance().level;
            if (level != null) {
                resolved = level.getRecipeManager().byKey(recipeId).isPresent();
            }
            event.getToolTip().add(Component.translatable(
                    resolved ? "gtlsupb.tooltip.pattern.resolved" : "gtlsupb.tooltip.pattern.unresolved")
                    .withStyle(resolved ? ChatFormatting.GREEN : ChatFormatting.RED));
        } catch (Throwable ignored) {}
    }
}
