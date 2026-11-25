package com.gtlsupb.gtlsupb.client;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import com.lowdragmc.lowdraglib.utils.BlockInfo;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.gtlsupb.gtlsupb.utils.RainbowText;

import java.util.List;

@Mod.EventBusSubscriber(modid = "gtlsupb", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class GTLSupbItemTooltipHandler {

    private static final String NAMESPACE = "gtlsupb";

    private GTLSupbItemTooltipHandler() {}

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id == null || !NAMESPACE.equals(id.getNamespace())) return;

        var tooltip = event.getToolTip();

        tooltip.add(RainbowText.createRainbowComponent(Component.translatable("gtlsupb.added_by").getString()));

        String descKey = "gtlsupb.desc." + id.getPath();
        if (I18n.exists(descKey)) {
            tooltip.add(Component.translatable(descKey));

            for (int line = 1; line <= 16; line++) {
                String extraKey = descKey + "." + line;
                if (!I18n.exists(extraKey)) break;
                tooltip.add(Component.translatable(extraKey));
            }
        }

        MachineDefinition definition = resolveDefinition(stack, id);
        GTRecipeType[] recipeTypes = definition == null ? null : definition.getRecipeTypes();
        if (recipeTypes == null || recipeTypes.length == 0) {
            return;
        }

        if (!Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("gtlsupb.tip.hold_shift"));
            return;
        }

        if (definition instanceof MultiblockMachineDefinition multiblock) {
            appendStructureInfo(tooltip, multiblock);
        }

        tooltip.add(Component.translatable("gtlsupb.tip.modes"));
        for (GTRecipeType recipeType : recipeTypes) {
            if (recipeType == null) continue;
            tooltip.add(Component.translatable("gtlsupb.tip.mode_entry", recipeTypeName(recipeType)));
        }
    }

    private static MachineDefinition resolveDefinition(ItemStack stack, ResourceLocation itemId) {
        try {
            MachineDefinition definition = GTRegistries.MACHINES.get(itemId);
            if (definition != null) return definition;
            if (stack.getItem() instanceof BlockItem blockItem) {
                ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(blockItem.getBlock());
                if (blockId != null) {
                    return GTRegistries.MACHINES.get(blockId);
                }
            }
        } catch (Throwable ignored) {

        }
        return null;
    }

    private static void appendStructureInfo(List<Component> tooltip, MultiblockMachineDefinition definition) {
        try {
            List<MultiblockShapeInfo> shapes = definition.getMatchingShapes();
            if (shapes.isEmpty()) return;
            BlockInfo[][][] blocks = shapes.get(0).getBlocks();
            if (blocks.length == 0 || blocks[0].length == 0 || blocks[0][0].length == 0) return;
            int depth = blocks.length;
            int height = blocks[0].length;
            int width = blocks[0][0].length;
            tooltip.add(Component.translatable("gtlsupb.tip.structure_size", width, height, depth));

            var controllerBlock = definition.get();
            int controllerRow = -1;
            outer:
            for (int z = 0; z < depth; z++) {
                for (int y = 0; y < blocks[z].length; y++) {
                    for (int x = 0; x < blocks[z][y].length; x++) {
                        BlockInfo info = blocks[z][y][x];
                        if (info == null) continue;
                        if (info.getBlockState().getBlock() == controllerBlock) {
                            controllerRow = y;
                            break outer;
                        }
                    }
                }
            }
            if (controllerRow >= 0) {
                tooltip.add(Component.translatable("gtlsupb.tip.controller_height", controllerRow));
            }
        } catch (Throwable ignored) {

        }
    }

    private static Component recipeTypeName(GTRecipeType recipeType) {
        if (recipeType.registryName == null) {
            return Component.literal("?");
        }
        String path = recipeType.registryName.getPath();
        String key = "gtceu." + path;
        if (I18n.exists(key)) {
            return Component.translatable(key);
        }
        String mapKey = "gtceu.recipe_map." + path;
        if (I18n.exists(mapKey)) {
            return Component.translatable(mapKey);
        }
        return Component.literal(path);
    }
}
