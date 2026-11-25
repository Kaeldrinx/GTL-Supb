package com.gtlsupb.gtlsupb.common.item;

import org.gtlcore.gtlcore.api.item.tool.ae2.patternTool.Ae2GtmProcessingPattern;
import org.gtlcore.gtlcore.api.item.tool.ae2.patternTool.ConflictAnalysisManager;
import org.gtlcore.gtlcore.api.item.tool.ae2.patternTool.ConflictAnalysisResult;
import org.gtlcore.gtlcore.api.item.tool.ae2.patternTool.GTRecipeManager;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.crafting.pattern.AEProcessingPattern;
import com.gtlsupb.gtlsupb.api.item.tool.ae2.Ae2PatternHelper;

import java.util.List;

public class PatternDebuggerItem extends Item {

    public PatternDebuggerItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory.INSTANCE.openUI(serverPlayer, hand);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    public static GTRecipeType resolveType(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String id = raw.trim();
        ResourceLocation rl = ResourceLocation.tryParse(id.contains(":") ? id : "gtceu:" + id);
        if (rl == null) return null;
        GTRecipeType type = GTRecipeTypes.get(rl.toString());
        if (type != null) return type;
        try {
            return GTRegistries.RECIPE_TYPES.get(rl);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static List<GTRecipe> collect(String rawType, int circuit,
                                          String inWhite, String inBlack, String outWhite, String outBlack) {
        GTRecipeType recipeType = resolveType(rawType);
        if (recipeType == null) return List.of();
        GTRecipeManager manager = new GTRecipeManager();
        manager.filterRecipesByType(recipeType);
        manager.filterRecipesByCircuit(circuit);
        if (inWhite != null && !inWhite.isBlank()) manager.filterRecipesByInputsIdArray(inWhite.split("\\s+"), true);
        if (inBlack != null && !inBlack.isBlank()) manager.filterRecipesByInputsIdArray(inBlack.split("\\s+"), false);
        if (outWhite != null && !outWhite.isBlank()) manager.filterRecipesByOutputsIdArray(outWhite.split("\\s+"), true);
        if (outBlack != null && !outBlack.isBlank()) manager.filterRecipesByOutputsIdArray(outBlack.split("\\s+"), false);
        return manager.getRecipes();
    }

    public static final String TAG_PARSED = "gtlsupb.parsed";

    public static final String TAG_SOURCE_RECIPE = "gtlsupb.source_recipe";

    public static final String TAG_SOURCE_TYPE = "gtlsupb.source_type";

    public static java.util.List<ItemStack> buildPatterns(ServerPlayer player, String rawType, int circuit, int scale,
                                                          String inWhite, String inBlack, String outWhite, String outBlack) {
        java.util.List<ItemStack> result = new java.util.ArrayList<>();
        GTRecipeType recipeType = resolveType(rawType);
        if (recipeType == null) return result;
        java.util.List<GTRecipe> recipes = collect(rawType, circuit, inWhite, inBlack, outWhite, outBlack);
        if (recipes.isEmpty()) return result;
        var inWhiteSet = splitIds(inWhite);
        var inBlackSet = splitIds(inBlack);
        var outWhiteSet = splitIds(outWhite);
        var outBlackSet = splitIds(outBlack);
        for (GTRecipe recipe : recipes) {
            try {
                Ae2GtmProcessingPattern pattern = Ae2GtmProcessingPattern.of(recipe, player);
                if (scale > 1) pattern.setScale(scale, false);
                pattern.setDefaultFilter();
                var decoded = org.gtlcore.gtlcore.api.item.tool.ae2.patternTool.Ae2BaseProcessingPatternHelper
                        .decodeToAEProcessingPattern(pattern.getPatternItemStack(), player);
                GenericStack[] inputs = filterStacks(decoded.getSparseInputs(), inWhiteSet, inBlackSet);
                GenericStack[] outputs = filterStacks(decoded.getOutputs(), outWhiteSet, outBlackSet);
                if (circuit > 0) {
                    GenericStack circuitStack = new GenericStack(
                            AEItemKey.of(com.gregtechceu.gtceu.common.item.IntCircuitBehaviour.stack(circuit)), 1L);
                    GenericStack[] withCircuit = java.util.Arrays.copyOf(inputs, inputs.length + 1);
                    withCircuit[inputs.length] = circuitStack;
                    inputs = withCircuit;
                }
                ItemStack stack = appeng.api.crafting.PatternDetailsHelper.encodeProcessingPattern(inputs, outputs);
                if (stack.isEmpty()) continue;
                var tag = stack.getOrCreateTag();
                tag.putBoolean(TAG_PARSED, true);
                if (recipe.id != null) tag.putString(TAG_SOURCE_RECIPE, recipe.id.toString());
                if (recipeType.registryName != null) {
                    tag.putString(TAG_SOURCE_TYPE, recipeType.registryName.toString());
                    Ae2PatternHelper.writePatternRecipeType(stack, recipeType.registryName);
                }
                result.add(stack);
            } catch (Throwable ignored) {}
        }
        return result;
    }

    public static void generate(ServerPlayer player, String rawType, int circuit, int scale,
                                String inWhite, String inBlack, String outWhite, String outBlack) {
        GTRecipeType recipeType = resolveType(rawType);
        if (recipeType == null) {
            player.displayClientMessage(Component.translatable("gtlsupb.pattern_debugger.bad_type", String.valueOf(rawType))
                    .withStyle(ChatFormatting.RED), false);
            return;
        }
        List<GTRecipe> recipes = collect(rawType, circuit, inWhite, inBlack, outWhite, outBlack);
        if (recipes.isEmpty()) {
            player.displayClientMessage(Component.translatable("gtlsupb.pattern_debugger.no_recipe")
                    .withStyle(ChatFormatting.YELLOW), false);
            return;
        }
        var inWhiteSet = splitIds(inWhite);
        var inBlackSet = splitIds(inBlack);
        var outWhiteSet = splitIds(outWhite);
        var outBlackSet = splitIds(outBlack);
        int given = 0;
        int failed = 0;
        for (GTRecipe recipe : recipes) {
            try {
                Ae2GtmProcessingPattern pattern = Ae2GtmProcessingPattern.of(recipe, player);
                if (scale > 1) pattern.setScale(scale, false);
                pattern.setDefaultFilter();

                var decoded = org.gtlcore.gtlcore.api.item.tool.ae2.patternTool.Ae2BaseProcessingPatternHelper
                        .decodeToAEProcessingPattern(pattern.getPatternItemStack(), player);
                GenericStack[] inputs = filterStacks(decoded.getSparseInputs(), inWhiteSet, inBlackSet);
                GenericStack[] outputs = filterStacks(decoded.getOutputs(), outWhiteSet, outBlackSet);

                if (circuit > 0) {
                    GenericStack circuitStack = new GenericStack(
                            AEItemKey.of(com.gregtechceu.gtceu.common.item.IntCircuitBehaviour.stack(circuit)), 1L);
                    GenericStack[] withCircuit = java.util.Arrays.copyOf(inputs, inputs.length + 1);
                    withCircuit[inputs.length] = circuitStack;
                    inputs = withCircuit;
                }

                ItemStack stack = appeng.api.crafting.PatternDetailsHelper.encodeProcessingPattern(inputs, outputs);
                if (stack.isEmpty()) {
                    failed++;
                    continue;
                }

                var tag = stack.getOrCreateTag();
                tag.putBoolean(TAG_PARSED, true);
                if (recipe.id != null) tag.putString(TAG_SOURCE_RECIPE, recipe.id.toString());
                if (recipeType.registryName != null) {
                    tag.putString(TAG_SOURCE_TYPE, recipeType.registryName.toString());

                    Ae2PatternHelper.writePatternRecipeType(stack, recipeType.registryName);
                }
                if (!player.getInventory().add(stack)) player.drop(stack, false);
                given++;
            } catch (Throwable ignored) {
                failed++;
            }
        }
        player.displayClientMessage(Component.translatable("gtlsupb.pattern_debugger.generated", given, failed)
                .withStyle(given > 0 ? ChatFormatting.GREEN : ChatFormatting.YELLOW), false);
    }

    private static java.util.Set<String> splitIds(String raw) {
        java.util.Set<String> out = new java.util.HashSet<>();
        if (raw != null) {
            for (String token : raw.split("\\s+")) {
                if (!token.isBlank()) out.add(token.toLowerCase(java.util.Locale.ROOT));
            }
        }
        return out;
    }

    private static appeng.api.stacks.GenericStack[] filterStacks(appeng.api.stacks.GenericStack[] stacks,
                                                                 java.util.Set<String> white,
                                                                 java.util.Set<String> black) {
        java.util.List<appeng.api.stacks.GenericStack> out = new java.util.ArrayList<>();
        for (appeng.api.stacks.GenericStack stack : stacks) {
            if (stack == null || stack.what() == null) continue;
            String id = stack.what().getId().toString();
            if (!black.isEmpty() && gtlsupb$idMatches(id, black)) continue;
            if (!white.isEmpty() && !gtlsupb$idMatches(id, white)) continue;
            out.add(stack);
        }
        return out.toArray(new appeng.api.stacks.GenericStack[0]);
    }

    private static boolean gtlsupb$idMatches(String id, java.util.Set<String> tokens) {
        for (String token : tokens) {
            if (id.equals(token) || id.endsWith(":" + token)) return true;
        }
        return false;
    }

    public static void analyze(ServerPlayer player, String rawType, int circuit) {
        GTRecipeType recipeType = resolveType(rawType);
        if (recipeType == null) {
            player.displayClientMessage(Component.translatable("gtlsupb.pattern_debugger.bad_type", String.valueOf(rawType))
                    .withStyle(ChatFormatting.RED), false);
            return;
        }
        List<GTRecipe> recipes = collect(rawType, circuit, "", "", "", "");
        if (recipes.isEmpty()) {
            player.displayClientMessage(Component.translatable("gtlsupb.pattern_debugger.no_recipe")
                    .withStyle(ChatFormatting.YELLOW), false);
            return;
        }
        List<ConflictAnalysisResult> conflicts = new ConflictAnalysisManager(recipes).useFindConflictForAll(circuit);
        player.displayClientMessage(Component.translatable("gtlsupb.pattern_debugger.conflict_count",
                conflicts.size(), recipes.size()).withStyle(ChatFormatting.AQUA), false);
        int shown = 0;
        for (ConflictAnalysisResult result : conflicts) {
            if (shown++ >= 5) break;
            player.displayClientMessage(Component.literal("· " + result).withStyle(ChatFormatting.GRAY), false);
        }
    }

    public static void rescale(ServerPlayer player, int scale) {
        Level level = player.level();
        for (ItemStack stack : player.getInventory().items) {
            if (stack.isEmpty()) continue;
            AEProcessingPattern pattern = Ae2PatternHelper.decode(stack, level);
            if (pattern == null) continue;
            try {
                ItemStack copy = Ae2PatternHelper.modifyScale(pattern, Math.max(1, scale), false,
                        64L * 1024L, Long.MAX_VALUE / 4L, Ae2PatternHelper.ModifyMode.BOTH);
                if (copy == null || copy.isEmpty()) {
                    player.displayClientMessage(Component.translatable("gtlsupb.pattern_debugger.rescale_failed")
                            .withStyle(ChatFormatting.RED), false);
                    return;
                }
                Ae2PatternHelper.copyRecipeBinding(stack, copy);
                if (!player.getInventory().add(copy)) player.drop(copy, false);
                player.displayClientMessage(Component.translatable("gtlsupb.pattern_debugger.rescaled", scale)
                        .withStyle(ChatFormatting.GREEN), false);
                return;
            } catch (Throwable ignored) {}
        }
        player.displayClientMessage(Component.translatable("gtlsupb.pattern_debugger.no_pattern")
                .withStyle(ChatFormatting.YELLOW), false);
    }

    public static int gtlsupb$countGeneratable(Player player, String rawType, int circuit,
                                               String inWhite, String inBlack, String outWhite, String outBlack) {
        try {
            return collect(rawType, circuit, inWhite, inBlack, outWhite, outBlack).size();
        } catch (Throwable ignored) {
            return 0;
        }
    }

    private static boolean gtlsupb$isPattern(ItemStack stack, Player player) {
        if (stack.isEmpty() || !stack.hasTag()) return false;
        var tag = stack.getTag();
        return tag != null && (tag.contains("in") || tag.contains("out"));
    }

    @Override
    public net.minecraft.world.InteractionResult useOn(net.minecraft.world.item.context.UseOnContext context) {
        Player player = context.getPlayer();
        net.minecraft.world.level.Level level = context.getLevel();
        if (player == null) return net.minecraft.world.InteractionResult.PASS;
        if (level.isClientSide) return net.minecraft.world.InteractionResult.SUCCESS;
        if (!player.isShiftKeyDown()) return net.minecraft.world.InteractionResult.PASS;
        if (!(level.getBlockEntity(context.getClickedPos()) instanceof com.gregtechceu.gtceu.api.machine.IMachineBlockEntity holder)) {
            return net.minecraft.world.InteractionResult.PASS;
        }
        if (!(holder.getMetaMachine() instanceof com.gtlsupb.gtlsupb.api.machine.IPatternAssemblyHatch assembly)) {
            return net.minecraft.world.InteractionResult.PASS;
        }
        var inv = assembly.getPatternInventory();
        if (inv == null) {
            return net.minecraft.world.InteractionResult.PASS;
        }
        if (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) {
            return net.minecraft.world.InteractionResult.PASS;
        }
        ItemStack tool = context.getItemInHand();
        var tag = tool.getOrCreateTag();
        String type = tag.getString("debug_type");
        if (type.isEmpty()) {
            String resolved = resolveTypeFromInventory(player);
            if (resolved != null) type = resolved;
        }
        if (type.isEmpty()) {
            player.displayClientMessage(
                    Component.translatable("gtlsupb.pattern_debugger.bad_type", ""), false);
            return net.minecraft.world.InteractionResult.CONSUME;
        }
        java.util.List<ItemStack> patterns = buildPatterns(serverPlayer, type,
                tag.getInt("debug_circuit"),
                Math.max(1, tag.getInt("debug_scale")),
                tag.getString("debug_iw"), tag.getString("debug_ib"),
                tag.getString("debug_ow"), tag.getString("debug_ob"));
        int moved = 0;
        for (ItemStack stack : patterns) {
            if (stack.isEmpty()) continue;
            for (int slot = 0; slot < inv.getSlots(); slot++) {
                if (!inv.getStackInSlot(slot).isEmpty()) continue;
                inv.setStackInSlot(slot, stack);
                assembly.onPatternInserted(slot, stack);
                moved++;
                break;
            }
        }
        if (moved > 0) {
            player.displayClientMessage(Component.translatable("gtlsupb.pattern_debugger.inserted", moved), false);
            return net.minecraft.world.InteractionResult.CONSUME;
        }
        return net.minecraft.world.InteractionResult.PASS;
    }

    public static String resolveTypeFromInventory(Player player) {
        Level level = player.level();
        for (ItemStack stack : player.getInventory().items) {
            if (stack.isEmpty()) continue;
            try {
                ResourceLocation typeId = Ae2PatternHelper.readGtlcoreRecipeTypeId(stack);
                if (typeId != null) return typeId.toString();
            } catch (Throwable ignored) {}
            try {
                GTRecipe bound = Ae2PatternHelper.readBindingRecipe(stack, level);
                if (bound != null && bound.recipeType.registryName != null) {
                    return bound.recipeType.registryName.toString();
                }
            } catch (Throwable ignored) {}
        }
        return null;
    }
}
