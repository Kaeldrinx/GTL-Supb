package com.gtlsupb.gtlsupb.integration.jei;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Method;

public final class WirelessTerminalDetector {

    private static final ResourceLocation WET_ID = new ResourceLocation("ae2wtlib", "wireless_pattern_encoding_terminal");

    private static final ResourceLocation WUT_ID = new ResourceLocation("ae2wtlib", "wireless_universal_terminal");

    private static final String WUT_ENCODING_KEY = "pattern_encoding";

    private WirelessTerminalDetector() {}

    public static boolean hasWirelessEncodingTerminal(Player player) {
        if (player == null) return false;

        for (ItemStack stack : player.getInventory().items) {
            if (isEncodingTerminal(stack)) return true;
        }

        for (ItemStack stack : player.getInventory().offhand) {
            if (isEncodingTerminal(stack)) return true;
        }

        if (checkCurios(player)) return true;

        return false;
    }

    private static boolean isEncodingTerminal(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Item item = stack.getItem();
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(item);
        if (itemId == null) return false;

        if (itemId.equals(WET_ID)) {
            return true;
        }
        if (itemId.equals(WUT_ID)) {

            return stack.hasTag() && stack.getTag().getBoolean(WUT_ENCODING_KEY);
        }
        return false;
    }

    private static Method M_CURIOS_GET_INVENTORY;
    private static Method M_LAZY_RESOLVE;
    private static Method M_CURIOS_GET_EQUIPPED;
    private static Method M_ITEM_GET_SLOTS;
    private static Method M_ITEM_GET_STACK;
    private static boolean CURIOS_RESOLVED;

    private static boolean gtlsupb$resolveCurios() {
        if (CURIOS_RESOLVED) return M_CURIOS_GET_INVENTORY != null;
        CURIOS_RESOLVED = true;
        try {
            Class<?> curiosApi = Class.forName("top.theillusivec4.curios.api.CuriosApi");
            M_CURIOS_GET_INVENTORY = curiosApi.getMethod("getCuriosInventory", LivingEntity.class);
            Class<?> lazyOptional = Class.forName("net.minecraftforge.common.util.LazyOptional");
            M_LAZY_RESOLVE = lazyOptional.getMethod("resolve");
            Class<?> curiosHandler = Class.forName("top.theillusivec4.curios.api.type.capability.ICuriosItemHandler");
            M_CURIOS_GET_EQUIPPED = curiosHandler.getMethod("getEquippedCurios");
            M_ITEM_GET_SLOTS = net.minecraftforge.items.IItemHandler.class.getMethod("getSlots");
            M_ITEM_GET_STACK = net.minecraftforge.items.IItemHandler.class.getMethod("getStackInSlot", int.class);
        } catch (Throwable ignored) {
            M_CURIOS_GET_INVENTORY = null;
        }
        return M_CURIOS_GET_INVENTORY != null;
    }

    private static boolean checkCurios(Player player) {
        try {
            if (!gtlsupb$resolveCurios()) return false;

            Object lazyOpt = M_CURIOS_GET_INVENTORY.invoke(null, player);
            if (lazyOpt == null) return false;

            Object resolved = M_LAZY_RESOLVE.invoke(lazyOpt);
            if (!(resolved instanceof java.util.Optional<?> optional)) return false;
            Object handler = optional.orElse(null);
            if (handler == null) return false;

            Object itemHandler = M_CURIOS_GET_EQUIPPED.invoke(handler);
            if (itemHandler == null) return false;

            int slots = (int) M_ITEM_GET_SLOTS.invoke(itemHandler);
            for (int i = 0; i < slots; i++) {
                Object stackObj = M_ITEM_GET_STACK.invoke(itemHandler, i);
                if (stackObj instanceof ItemStack is && isEncodingTerminal(is)) {
                    return true;
                }
            }
        } catch (Throwable ignored) {

        }
        return false;
    }
}
