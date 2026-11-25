package com.gtlsupb.gtlsupb.client;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.gtlsupb.gtlsupb.utils.RainbowText;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = "gtlsupb")
public final class RainbowItemNameHandler {

    private static final String MOD_NAMESPACE = "gtlsupb";

    private RainbowItemNameHandler() {}

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        try {
            ItemStack stack = event.getItemStack();
            if (stack == null || stack.isEmpty()) return;

            ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (id == null || !MOD_NAMESPACE.equals(id.getNamespace())) return;

            var tooltip = event.getToolTip();
            if (tooltip == null || tooltip.isEmpty()) return;

            String name = Component.translatable(stack.getDescriptionId()).getString();
            if (name == null || name.isEmpty()) return;
            tooltip.set(0, RainbowText.createRainbowComponent(name));
        } catch (Throwable ignored) {

        }
    }
}
