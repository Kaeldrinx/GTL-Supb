package com.gtlsupb.gtlsupb.util;

import net.minecraft.world.item.ItemStack;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class PullIngredientOrdering {

    private PullIngredientOrdering() {}

    public static List<ItemStack> preferSpecificComponentsFirst(List<ItemStack> stacks) {
        if (stacks == null || stacks.size() < 2) return stacks;
        List<ItemStack> copy = new ArrayList<>(stacks);
        copy.sort(Comparator.comparingInt(PullIngredientOrdering::componentSpecificityRank).reversed());
        return copy;
    }

    public static int itemKeyComponentSpecificityRank(AEItemKey key) {
        if (key == null) return -1;
        try {
            ItemStack stack = key.toStack(1);
            return componentSpecificityRank(stack);
        } catch (Throwable ignored) {
            return 0;
        }
    }

    public static int genericStackItemSpecificityRank(GenericStack stack) {
        if (stack == null || !(stack.what() instanceof AEItemKey key)) return -1;
        return itemKeyComponentSpecificityRank(key);
    }

    public static int componentSpecificityRank(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return -1;
        try {
            if (!stack.hasTag() || stack.getTag() == null) return 0;
            return Math.max(1, stack.getTag().size());
        } catch (Throwable ignored) {
            return 0;
        }
    }
}
