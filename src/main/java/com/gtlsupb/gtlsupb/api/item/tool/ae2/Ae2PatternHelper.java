package com.gtlsupb.gtlsupb.api.item.tool.ae2;

import org.gtlcore.gtlcore.common.data.GTLRecipeTypes;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntCircuitIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import appeng.crafting.pattern.AEProcessingPattern;
import appeng.crafting.pattern.EncodedPatternItem;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public final class Ae2PatternHelper {

    public static final Ae2PatternHelper INSTANCE = new Ae2PatternHelper();

    private Ae2PatternHelper() {}

    public enum ModifyMode {
        INPUT,
        OUTPUT,
        BOTH
    }

    public static AEProcessingPattern decode(ItemStack patternStack, ServerPlayer player) {
        return decode(patternStack, player.level());
    }

    public static AEProcessingPattern decode(ItemStack patternStack, net.minecraft.world.level.Level level) {
        if (!(patternStack.getItem() instanceof EncodedPatternItem)) return null;
        EncodedPatternItem patternItem = (EncodedPatternItem) patternStack.getItem();
        var decoded = patternItem.decode(patternStack, level, false);
        return decoded instanceof AEProcessingPattern ? (AEProcessingPattern) decoded : null;
    }

    public static ItemStack modifyScale(
                                        AEProcessingPattern pattern,
                                        int scale,
                                        boolean div,
                                        long maxItemStack,
                                        long maxFluidStack,
                                        ModifyMode mode) {
        GenericStack[] inputs = pattern.getSparseInputs();
        GenericStack[] outputs = pattern.getOutputs();

        GenericStack[] modifiedInputs;
        GenericStack[] modifiedOutputs;

        switch (mode) {
            case INPUT:
                if (!checkModify(inputs, scale, div, maxItemStack, maxFluidStack)) return null;
                modifiedInputs = modifyStacks(inputs, scale, div);
                modifiedOutputs = outputs;
                break;
            case OUTPUT:
                if (!checkModify(outputs, scale, div, maxItemStack, maxFluidStack)) return null;
                modifiedInputs = inputs;
                modifiedOutputs = modifyStacks(outputs, scale, div);
                break;
            case BOTH:
                if (!checkModify(inputs, scale, div, maxItemStack, maxFluidStack)) return null;
                if (!checkModify(outputs, scale, div, maxItemStack, maxFluidStack)) return null;
                modifiedInputs = modifyStacks(inputs, scale, div);
                modifiedOutputs = modifyStacks(outputs, scale, div);
                break;
            default:
                throw new IllegalArgumentException("Unknown ModifyMode: " + mode);
        }

        return PatternDetailsHelper.encodeProcessingPattern(modifiedInputs, modifiedOutputs);
    }

    private static boolean checkModify(
                                       GenericStack[] stacks,
                                       int scale,
                                       boolean div,
                                       long maxItemStack,
                                       long maxFluidStack) {
        if (div) {
            for (GenericStack stack : stacks) {
                if (stack != null && stack.amount() % scale != 0L) return false;
            }
        } else {
            for (GenericStack stack : stacks) {
                if (stack != null) {
                    long perUnit = stack.what().getAmountPerUnit();
                    if (stack.what().getType().equals(AEKeyType.fluids())) {
                        if (stack.amount() * scale > maxFluidStack * perUnit) return false;
                    }
                    if (stack.what().getType().equals(AEKeyType.items())) {
                        if (stack.amount() * scale > maxItemStack * perUnit) return false;
                    }
                }
            }
        }
        return true;
    }

    private static GenericStack[] modifyStacks(GenericStack[] stacks, int scale, boolean div) {
        GenericStack[] result = new GenericStack[stacks.length];
        for (int i = 0; i < stacks.length; i++) {
            GenericStack stack = stacks[i];
            if (stack != null) {
                long amt = div ? stack.amount() / scale : stack.amount() * scale;
                result[i] = new GenericStack(stack.what(), amt);
            }
        }
        return result;
    }

    public static final String TAG_RECIPE_ID = "gtlsupb.recipe_id";

    public static final String TAG_RECIPE_TYPE = "gtlsupb.recipe_type";

    public static final String TAG_CATALYST_KEYS = "gtlsupb.catalyst_keys";

    public static final String TAG_CATALYST_VERSION = "gtlsupb.catalyst_ver";

    public static final int CATALYST_VERSION = 2;

    public static final String TAG_GTLCORE_ROOT = "gtlcore";
    public static final String TAG_GTLCORE_RECIPE_TYPES = "patternQuickUploadRecipeTypes";

    private static final class RecipeTypePoolHolder {

        static final List<GTRecipeType> POOL = build();

        static List<GTRecipeType> build() {
            LinkedHashSet<GTRecipeType> types = new LinkedHashSet<>(GTRegistries.RECIPE_TYPES.values());
            types.add(GTLRecipeTypes.DEHYDRATOR_RECIPES);
            types.add(GTLRecipeTypes.LIGHTNING_PROCESSOR_RECIPES);
            return new ArrayList<>(types);
        }
    }

    private static List<GTRecipeType> getRecipeTypePool() {
        return RecipeTypePoolHolder.POOL;
    }

    public static void writeRecipeBinding(ItemStack pattern, GTRecipe recipe) {
        writeRecipeBinding(pattern, recipe, null);
    }

    public static void writeRecipeBinding(ItemStack pattern, GTRecipe recipe, Level level) {
        if (pattern == null || pattern.isEmpty() || recipe == null || recipe.id == null) return;
        var tag = pattern.getOrCreateTag();
        tag.putString(TAG_RECIPE_ID, recipe.id.toString());
        tag.putString(TAG_RECIPE_TYPE, recipe.recipeType.registryName.toString());

        writeGtlcoreRecipeType(tag, recipe.recipeType.registryName);

        net.minecraft.nbt.ListTag catalystList = new net.minecraft.nbt.ListTag();
        var catalystKeys = collectPatternCatalystKeys(recipe, pattern, level);
        if (catalystKeys.isEmpty()) catalystKeys = collectRecipeCatalystKeys(recipe);
        for (var entry : it.unimi.dsi.fastutil.objects.Object2LongMaps.fastIterable(catalystKeys)) {
            net.minecraft.nbt.CompoundTag entryTag = new net.minecraft.nbt.CompoundTag();
            entryTag.put("ae_key", entry.getKey().toTagGeneric());
            entryTag.putLong("amount", Math.max(entry.getLongValue(), 1L));
            catalystList.add(entryTag);
        }
        if (!catalystList.isEmpty()) {
            tag.put(TAG_CATALYST_KEYS, catalystList);
            tag.putInt(TAG_CATALYST_VERSION, CATALYST_VERSION);
        } else {

            tag.remove(TAG_CATALYST_KEYS);
            tag.remove(TAG_CATALYST_VERSION);
        }
    }

    public static it.unimi.dsi.fastutil.objects.Object2LongMap<AEKey> collectRecipeCatalystKeys(GTRecipe recipe) {
        it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap<AEKey> keys = new it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap<>();
        if (recipe == null) return keys;
        for (Content c : recipe.getInputContents(ItemRecipeCapability.CAP)) {
            if (c == null || c.content == null || c.chance > 0) continue;
            try {
                Object content = ItemRecipeCapability.CAP.of(c.content);
                if (content instanceof Ingredient ing) {
                    long amount = Math.max(getIngredientAmount(ing), 1L);
                    for (ItemStack s : ing.getItems()) {
                        if (s == null || s.isEmpty()) continue;
                        if (com.gregtechceu.gtceu.common.item.IntCircuitBehaviour.isIntegratedCircuit(s)) continue;
                        keys.addTo(appeng.api.stacks.AEItemKey.of(s), amount);
                        break;
                    }
                }
            } catch (Throwable ignored) {}
        }
        for (Content c : recipe.getInputContents(FluidRecipeCapability.CAP)) {
            if (c == null || c.content == null || c.chance > 0) continue;
            try {
                Object content = FluidRecipeCapability.CAP.of(c.content);
                if (content instanceof com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient fg) {
                    for (com.lowdragmc.lowdraglib.side.fluid.FluidStack fs : fg.getStacks()) {
                        if (fs == null || fs.isEmpty()) continue;
                        keys.addTo(appeng.api.stacks.AEFluidKey.of(fs.getFluid(), fs.getTag()), Math.max(fg.getAmount(), 1L));
                        break;
                    }
                }
            } catch (Throwable ignored) {}
        }
        return keys;
    }

    public static it.unimi.dsi.fastutil.objects.Object2LongMap<AEKey> collectPatternCatalystKeys(GTRecipe recipe, ItemStack patternStack, Level level) {
        it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap<AEKey> keys = new it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap<>();
        if (recipe == null) return keys;
        AEProcessingPattern pattern = (patternStack == null || level == null) ? null : decode(patternStack, level);

        for (Content c : recipe.getInputContents(ItemRecipeCapability.CAP)) {
            if (c == null || c.content == null || c.chance > 0) continue;
            try {
                Object content = ItemRecipeCapability.CAP.of(c.content);
                if (content instanceof Ingredient ing) {
                    long amount = Math.max(getIngredientAmount(ing), 1L);
                    AEKey resolved = null;
                    if (pattern != null) {
                        for (GenericStack input : pattern.getSparseInputs()) {
                            if (input == null || input.what() == null) continue;
                            if (!(input.what() instanceof appeng.api.stacks.AEItemKey itemKey)) continue;
                            if (ing.test(itemKey.toStack(1))) {
                                resolved = input.what();
                                break;
                            }
                        }
                    }
                    if (resolved == null) {
                        for (ItemStack s : ing.getItems()) {
                            if (s == null || s.isEmpty()) continue;
                            if (com.gregtechceu.gtceu.common.item.IntCircuitBehaviour.isIntegratedCircuit(s)) continue;
                            resolved = appeng.api.stacks.AEItemKey.of(s);
                            break;
                        }
                    }
                    if (resolved != null) keys.addTo(resolved, amount);
                }
            } catch (Throwable ignored) {}
        }

        for (Content c : recipe.getInputContents(FluidRecipeCapability.CAP)) {
            if (c == null || c.content == null || c.chance > 0) continue;
            try {
                Object content = FluidRecipeCapability.CAP.of(c.content);
                if (content instanceof com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient fg) {
                    long amount = Math.max(fg.getAmount(), 1L);
                    appeng.api.stacks.AEFluidKey resolved = null;
                    if (pattern != null) {
                        for (GenericStack input : pattern.getSparseInputs()) {
                            if (input == null || input.what() == null) continue;
                            if (!(input.what() instanceof appeng.api.stacks.AEFluidKey fluidKey)) continue;
                            for (com.lowdragmc.lowdraglib.side.fluid.FluidStack fs : fg.getStacks()) {
                                if (fs != null && !fs.isEmpty() && fs.getFluid() == fluidKey.getFluid()) {
                                    resolved = fluidKey;
                                    break;
                                }
                            }
                            if (resolved != null) break;
                        }
                    }
                    if (resolved == null) {
                        for (com.lowdragmc.lowdraglib.side.fluid.FluidStack fs : fg.getStacks()) {
                            if (fs == null || fs.isEmpty()) continue;
                            resolved = appeng.api.stacks.AEFluidKey.of(fs.getFluid(), fs.getTag());
                            break;
                        }
                    }
                    if (resolved != null) keys.addTo(resolved, amount);
                }
            } catch (Throwable ignored) {}
        }
        return keys;
    }

    public static it.unimi.dsi.fastutil.objects.Object2LongMap<AEKey> collectCatalystKeysFromPattern(ItemStack pattern) {
        it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap<AEKey> demands = new it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap<>();
        for (net.minecraft.nbt.CompoundTag entry : readCatalystKeys(pattern)) {
            try {
                AEKey key = AEKey.fromTagGeneric(entry.getCompound("ae_key"));
                if (key != null) demands.addTo(key, Math.max(entry.getLong("amount"), 1L));
            } catch (Throwable ignored) {}
        }
        return demands;
    }

    public static boolean hasValidCatalystRecord(ItemStack pattern) {
        if (pattern == null || pattern.isEmpty() || !pattern.hasTag()) return false;
        var tag = pattern.getTag();
        if (tag == null) return false;
        return tag.contains(TAG_CATALYST_KEYS, Tag.TAG_LIST) && tag.contains(TAG_CATALYST_VERSION, Tag.TAG_INT) && tag.getInt(TAG_CATALYST_VERSION) >= CATALYST_VERSION;
    }

    private static final ResourceLocation VIRTUAL_INGREDIENT_ID = new ResourceLocation("gtlcore", "virtual_ingredient");
    private static final String TAG_VIRTUAL_ITEM = "VirtualItem";
    private static final String TAG_VIRTUAL_FLUID = "VirtualFluid";
    private static final String TAG_VIRTUAL_MARKED = "marked";

    public static Item getVirtualIngredientItem() {
        try {
            return net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(VIRTUAL_INGREDIENT_ID);
        } catch (Throwable t) {
            return null;
        }
    }

    public static boolean isVirtualSupplyAvailable() {
        return getVirtualIngredientItem() != null;
    }

    public static ItemStack createVirtualMarker(AEKey key) {
        Item item = getVirtualIngredientItem();
        if (item == null || key == null) return ItemStack.EMPTY;
        try {
            ItemStack wrapper = new ItemStack(item);
            if (key instanceof appeng.api.stacks.AEItemKey itemKey) {
                ItemStack payload = itemKey.toStack(1);
                if (payload.isEmpty()) return ItemStack.EMPTY;
                com.lowdragmc.lowdraglib.misc.ItemStackTransfer transfer = new com.lowdragmc.lowdraglib.misc.ItemStackTransfer(1);
                transfer.setStackInSlot(0, payload.copyWithCount(1));

                com.lowdragmc.lowdraglib.syncdata.ITagSerializable<net.minecraft.nbt.CompoundTag> serializable = transfer;
                wrapper.getOrCreateTag().put(TAG_VIRTUAL_ITEM, serializable.serializeNBT());
            } else if (key instanceof appeng.api.stacks.AEFluidKey fluidKey) {
                long capacity = com.lowdragmc.lowdraglib.side.fluid.FluidHelper.getBucket();
                com.lowdragmc.lowdraglib.misc.FluidStorage storage = new com.lowdragmc.lowdraglib.misc.FluidStorage(capacity);
                storage.setFluidInTank(0, com.lowdragmc.lowdraglib.side.fluid.FluidStack.create(fluidKey.getFluid(), capacity));
                com.lowdragmc.lowdraglib.syncdata.ITagSerializable<net.minecraft.nbt.CompoundTag> serializable = storage;
                wrapper.getOrCreateTag().put(TAG_VIRTUAL_FLUID, serializable.serializeNBT());
            } else {
                return ItemStack.EMPTY;
            }
            wrapper.getOrCreateTag().putBoolean(TAG_VIRTUAL_MARKED, true);
            return wrapper;
        } catch (Throwable t) {
            return ItemStack.EMPTY;
        }
    }

    public static boolean isVirtualMarker(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.hasTag()) return false;
        Item item = getVirtualIngredientItem();
        if (item == null || !stack.is(item)) return false;
        return stack.getTag().getBoolean(TAG_VIRTUAL_MARKED);
    }

    public static AEKey readVirtualMarkerPayload(ItemStack stack) {
        if (!isVirtualMarker(stack)) return null;
        net.minecraft.nbt.CompoundTag tag = stack.getTag();
        try {
            if (tag.contains(TAG_VIRTUAL_ITEM)) {
                com.lowdragmc.lowdraglib.misc.ItemStackTransfer transfer = new com.lowdragmc.lowdraglib.misc.ItemStackTransfer(1);
                transfer.deserializeNBT(tag.getCompound(TAG_VIRTUAL_ITEM));
                ItemStack payload = transfer.getStackInSlot(0);
                return payload.isEmpty() ? null : appeng.api.stacks.AEItemKey.of(payload);
            }
            if (tag.contains(TAG_VIRTUAL_FLUID)) {
                com.lowdragmc.lowdraglib.misc.FluidStorage storage = new com.lowdragmc.lowdraglib.misc.FluidStorage(
                        com.lowdragmc.lowdraglib.side.fluid.FluidHelper.getBucket());
                storage.deserializeNBT(tag.getCompound(TAG_VIRTUAL_FLUID));
                com.lowdragmc.lowdraglib.side.fluid.FluidStack payload = storage.getFluidInTank(0);

                return payload.isEmpty() ? null : appeng.api.stacks.AEFluidKey.of(payload.getFluid());
            }
        } catch (Throwable ignored) {}
        return null;
    }

    public static java.util.List<net.minecraft.nbt.CompoundTag> readCatalystKeys(ItemStack pattern) {
        java.util.List<net.minecraft.nbt.CompoundTag> result = new java.util.ArrayList<>();
        if (pattern == null || pattern.isEmpty() || !pattern.hasTag()) return result;
        net.minecraft.nbt.Tag tag = pattern.getTag().get(TAG_CATALYST_KEYS);
        if (tag == null) return result;

        if (!pattern.getTag().contains(TAG_CATALYST_VERSION, Tag.TAG_INT)) return result;
        if (pattern.getTag().getInt(TAG_CATALYST_VERSION) < CATALYST_VERSION) return result;
        if (tag instanceof net.minecraft.nbt.ListTag list) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i) instanceof net.minecraft.nbt.CompoundTag c) {
                    result.add(c);
                }
            }
        }
        return result;
    }

    public static ResourceLocation readRecipeId(ItemStack pattern) {
        if (pattern == null || pattern.isEmpty() || !pattern.hasTag()) return null;
        var tag = pattern.getTag();
        if (tag == null || !tag.contains(TAG_RECIPE_ID, Tag.TAG_STRING)) return null;
        return ResourceLocation.tryParse(tag.getString(TAG_RECIPE_ID));
    }

    public static ResourceLocation readRecipeTypeId(ItemStack pattern) {
        if (pattern == null || pattern.isEmpty() || !pattern.hasTag()) return null;
        var tag = pattern.getTag();
        if (tag == null) return null;
        if (tag.contains(TAG_RECIPE_TYPE, Tag.TAG_STRING)) {
            ResourceLocation id = ResourceLocation.tryParse(tag.getString(TAG_RECIPE_TYPE));
            if (id != null) return id;
        }
        return readGtlcoreRecipeTypeId(pattern);
    }

    public static void writePatternRecipeType(net.minecraft.world.item.ItemStack pattern, ResourceLocation typeId) {
        if (pattern == null || pattern.isEmpty() || typeId == null) return;
        writeGtlcoreRecipeType(pattern.getOrCreateTag(), typeId);
    }

    private static void writeGtlcoreRecipeType(net.minecraft.nbt.CompoundTag tag, ResourceLocation typeId) {
        if (tag == null || typeId == null) return;
        var gtlcoreTag = tag.contains(TAG_GTLCORE_ROOT, Tag.TAG_COMPOUND) ? tag.getCompound(TAG_GTLCORE_ROOT) : new net.minecraft.nbt.CompoundTag();
        var list = new net.minecraft.nbt.ListTag();
        list.add(net.minecraft.nbt.StringTag.valueOf(typeId.toString()));
        gtlcoreTag.put(TAG_GTLCORE_RECIPE_TYPES, list);
        tag.put(TAG_GTLCORE_ROOT, gtlcoreTag);
    }

    public static ResourceLocation readGtlcoreRecipeTypeId(ItemStack pattern) {
        if (pattern == null || pattern.isEmpty() || !pattern.hasTag()) return null;
        var tag = pattern.getTag();
        if (tag == null || !tag.contains(TAG_GTLCORE_ROOT, Tag.TAG_COMPOUND)) return null;
        var gtlcoreTag = tag.getCompound(TAG_GTLCORE_ROOT);
        if (!gtlcoreTag.contains(TAG_GTLCORE_RECIPE_TYPES, Tag.TAG_LIST)) return null;
        var list = gtlcoreTag.getList(TAG_GTLCORE_RECIPE_TYPES, Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            ResourceLocation id = ResourceLocation.tryParse(list.getString(i));
            if (id != null) return id;
        }
        return null;
    }

    public static GTRecipe readBindingRecipe(ItemStack pattern, Level level) {
        ResourceLocation typeId = canonicalTypeId(readRecipeTypeId(pattern));
        ResourceLocation recipeId = readRecipeId(pattern);
        if (level != null && typeId != null && recipeId != null) {
            var opt = level.getRecipeManager().byKey(recipeId);
            if (opt.isPresent() && opt.get() instanceof GTRecipe gtRecipe &&
                    gtRecipe.recipeType.registryName.equals(typeId)) {
                return gtRecipe;
            }
        }
        return reBindByContent(pattern, level);
    }

    public static GTRecipe reBindByContent(ItemStack pattern, Level level) {
        if (pattern == null || pattern.isEmpty() || level == null) return null;
        try {
            AEProcessingPattern decoded = decode(pattern, level);
            if (decoded == null) return null;
            GTRecipe recipe = matchPatternRecipe(decoded, level);
            if (recipe == null) return null;
            writeRecipeBinding(pattern, recipe, level);
            return recipe;
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static GTRecipeType readBindingRecipeType(ItemStack pattern, Level level) {
        if (pattern == null || pattern.isEmpty()) return null;
        GTRecipeType type = resolveBindingRecipeType(readRecipeTypeId(pattern));
        if (type != null) return type;
        GTRecipe recipe = level == null ? null : reBindByContent(pattern, level);
        return recipe == null ? null : recipe.recipeType;
    }

    private static GTRecipeType resolveBindingRecipeType(ResourceLocation rawTypeId) {
        ResourceLocation typeId = canonicalTypeId(rawTypeId);
        if (typeId == null) return null;
        try {
            if (net.minecraft.core.registries.BuiltInRegistries.RECIPE_TYPE.get(typeId) instanceof GTRecipeType type) {
                return canonicalFurnaceType(type);
            }
        } catch (Throwable ignored) {}

        try {
            GTRecipeType type = com.gregtechceu.gtceu.api.registry.GTRegistries.RECIPE_TYPES.get(typeId);
            if (type != null) return canonicalFurnaceType(type);
        } catch (Throwable ignored) {}
        if (isFurnaceAlias(rawTypeId)) {
            return furnaceRecipeType();
        }
        return null;
    }

    private static GTRecipeType canonicalFurnaceType(GTRecipeType type) {
        if (type == null) return null;
        GTRecipeType furnace = furnaceRecipeType();
        if (furnace == null || type == furnace) return type;
        ResourceLocation id = type.registryName;
        if (id == null || !isFurnaceAlias(id)) return type;
        return furnace;
    }

    private static GTRecipeType furnaceRecipeType() {
        try {
            return com.gregtechceu.gtceu.common.data.GTRecipeTypes.FURNACE_RECIPES;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static ResourceLocation furnaceTypeId() {
        GTRecipeType furnace = furnaceRecipeType();
        if (furnace != null && furnace.registryName != null) return furnace.registryName;
        return new ResourceLocation("gtceu", "electric_furnace");
    }

    private static boolean isFurnaceAlias(@Nullable ResourceLocation id) {
        if (id == null) return false;
        if ("minecraft".equals(id.getNamespace()) && "smelting".equals(id.getPath())) return true;
        return "gtceu".equals(id.getNamespace()) &&
                ("electric_smelting".equals(id.getPath()) || "smelting".equals(id.getPath()));
    }

    private static ResourceLocation canonicalTypeId(ResourceLocation id) {
        if (id == null) return null;
        if (!isFurnaceAlias(id)) return id;
        return furnaceTypeId();
    }

    public static void copyRecipeBinding(ItemStack from, ItemStack to) {
        if (from == null || from.isEmpty() || to == null || to.isEmpty() || !from.hasTag()) return;
        if (!hasCustomKeys(from)) return;
        copyAllCustomKeys(from, to);

        var fromTag = from.getTag();
        if (fromTag != null && fromTag.contains(TAG_GTLCORE_ROOT, Tag.TAG_COMPOUND)) {
            to.getOrCreateTag().put(TAG_GTLCORE_ROOT, fromTag.getCompound(TAG_GTLCORE_ROOT).copy());
        }
    }

    public static final String TAG_PREFIX = "gtlsupb.";

    public static boolean hasCustomKeys(ItemStack pattern) {
        if (pattern == null || pattern.isEmpty() || !pattern.hasTag()) return false;
        var tag = pattern.getTag();
        if (tag == null) return false;
        for (String key : tag.getAllKeys()) {
            if (key.startsWith(TAG_PREFIX)) return true;
        }
        return false;
    }

    public static void copyAllCustomKeys(ItemStack from, ItemStack to) {
        if (from == null || from.isEmpty() || to == null || to.isEmpty() || !from.hasTag()) return;
        var fromTag = from.getTag();
        if (fromTag == null) return;
        var toTag = to.getOrCreateTag();
        for (String key : fromTag.getAllKeys()) {
            if (!key.startsWith(TAG_PREFIX)) continue;
            net.minecraft.nbt.Tag value = fromTag.get(key);
            if (value != null) toTag.put(key, value.copy());
        }
    }

    public static boolean sameCustomKeys(ItemStack first, ItemStack second) {
        if (!hasCustomKeys(first) || !hasCustomKeys(second)) return false;
        var firstTag = first.getTag();
        var secondTag = second.getTag();
        if (firstTag == null || secondTag == null) return false;
        for (String key : firstTag.getAllKeys()) {
            if (!key.startsWith(TAG_PREFIX)) continue;
            net.minecraft.nbt.Tag value = firstTag.get(key);
            net.minecraft.nbt.Tag other = secondTag.get(key);
            if (value == null || other == null || !value.equals(other)) return false;
        }
        return true;
    }

    public static GTRecipe matchPatternRecipe(AEProcessingPattern pattern, Level level) {
        if (pattern == null || level == null) return null;
        GenericStack[] pInputs = pattern.getSparseInputs();
        GenericStack[] pOutputs = pattern.getOutputs();

        if (pOutputs == null || pOutputs.length == 0) return null;

        List<GTRecipeType> hinted = hintRecipeTypes(pattern);
        var hits = scanRecipeHits(pattern, level, hinted);
        if (hits.isEmpty() && !hinted.isEmpty()) {
            hits = scanRecipeHits(pattern, level, getRecipeTypePool());
        }

        return hits.size() == 1 ? hits.values().iterator().next() : null;
    }

    private static Map<GTRecipeType, GTRecipe> scanRecipeHits(AEProcessingPattern pattern, Level level,
                                                              List<GTRecipeType> types) {
        Map<GTRecipeType, GTRecipe> hits = new java.util.LinkedHashMap<>();

        PatternKeys keys = buildPatternKeys(pattern);
        if (keys == null) return hits;
        for (GTRecipeType type : types) {
            if (type == null || hits.containsKey(type)) continue;
            for (GTRecipe recipe : level.getRecipeManager().getAllRecipesFor(type)) {
                if (matchesPattern(recipe, keys)) {
                    hits.put(type, recipe);
                    break;
                }
            }

            if (!hits.containsKey(type) && type == com.gregtechceu.gtceu.common.data.GTRecipeTypes.FURNACE_RECIPES) {
                GTRecipe synthetic = gtlsupb$matchSmelting(level, keys);
                if (synthetic != null) {
                    hits.put(type, synthetic);
                }
            }
        }
        return hits;
    }

    private static GTRecipe gtlsupb$matchSmelting(Level level, PatternKeys keys) {
        try {
            for (net.minecraft.world.item.crafting.SmeltingRecipe smelting : level.getRecipeManager()
                    .getAllRecipesFor(net.minecraft.world.item.crafting.RecipeType.SMELTING)) {
                net.minecraft.world.item.ItemStack result = smelting.getResultItem(level.registryAccess());
                if (result.isEmpty()) continue;

                GTRecipe synthetic = com.gregtechceu.gtceu.common.data.GTRecipeTypes.FURNACE_RECIPES
                        .toGTrecipe(smelting.getId(), smelting);
                if (matchesPattern(synthetic, keys)) {
                    return synthetic;
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }

    private static final class PatternKeys {

        final Map<AEItemKey, Long> inputItems;
        final Map<AEFluidKey, Long> inputFluids;
        final Map<AEItemKey, Long> outputItems;
        final Map<AEFluidKey, Long> outputFluids;

        PatternKeys(Map<AEItemKey, Long> inputItems, Map<AEFluidKey, Long> inputFluids,
                    Map<AEItemKey, Long> outputItems, Map<AEFluidKey, Long> outputFluids) {
            this.inputItems = inputItems;
            this.inputFluids = inputFluids;
            this.outputItems = outputItems;
            this.outputFluids = outputFluids;
        }
    }

    private static PatternKeys buildPatternKeys(AEProcessingPattern pattern) {
        Map<AEItemKey, Long> inputItems = new HashMap<>();
        Map<AEFluidKey, Long> inputFluids = new HashMap<>();
        if (!collectPatternKeys(pattern.getSparseInputs(), inputItems, inputFluids)) return null;

        inputItems.entrySet().removeIf(e -> e.getKey().toStack().is(
                com.gregtechceu.gtceu.common.data.GTItems.INTEGRATED_CIRCUIT.get()));

        Map<AEItemKey, Long> outputItems = new HashMap<>();
        Map<AEFluidKey, Long> outputFluids = new HashMap<>();
        if (!collectPatternKeys(pattern.getOutputs(), outputItems, outputFluids)) return null;
        return new PatternKeys(inputItems, inputFluids, outputItems, outputFluids);
    }

    private static List<GTRecipeType> hintRecipeTypes(AEProcessingPattern pattern) {
        List<GTRecipeType> types = new java.util.ArrayList<>();
        try {
            ItemStack definition = pattern.getDefinition().toStack();
            CompoundTag tag = definition.getTag();
            if (tag == null || !tag.contains("gtlcore", Tag.TAG_COMPOUND)) return types;
            CompoundTag gtlcoreTag = tag.getCompound("gtlcore");
            if (!gtlcoreTag.contains("patternQuickUploadRecipeTypes", Tag.TAG_LIST)) return types;
            var list = gtlcoreTag.getList("patternQuickUploadRecipeTypes", Tag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) {

                GTRecipeType resolved = resolveBindingRecipeType(ResourceLocation.tryParse(list.getString(i)));
                if (resolved != null && !types.contains(resolved)) {
                    types.add(resolved);
                }
            }
        } catch (Throwable ignored) {}
        return types;
    }

    private static final int MAX_PATTERN_SCALE = 64;

    private static boolean matchesPattern(GTRecipe recipe, PatternKeys keys) {
        if (diagnosePattern(recipe, 1, keys) == null) return true;
        int scale = guessScale(recipe, keys);
        return scale > 1 && scale <= MAX_PATTERN_SCALE && diagnosePattern(recipe, scale, keys) == null;
    }

    private static int guessScale(GTRecipe recipe, PatternKeys keys) {
        try {
            List<Content> inItems = recipe.inputs.get(ItemRecipeCapability.CAP);
            if (inItems == null) return 1;
            for (Content content : inItems) {
                if (isOptionalContent(content)) continue;
                Object obj = content.getContent();
                Ingredient ingredient = obj instanceof ItemStack st ? Ingredient.of(st) :
                        (obj instanceof Ingredient ing ? ing : null);
                if (ingredient == null || isCircuitIngredient(ingredient)) continue;
                long need = getIngredientAmount(ingredient);
                if (need <= 0L) continue;
                for (var e : keys.inputItems.entrySet()) {
                    if (!ingredient.test(e.getKey().toStack())) continue;
                    long have = e.getValue();
                    if (have > need && have % need == 0L && have / need <= MAX_PATTERN_SCALE) {
                        return (int) (have / need);
                    }
                    return 1;
                }
            }
        } catch (Throwable ignored) {}
        return 1;
    }

    private static String diagnosePattern(GTRecipe recipe, int scale, PatternKeys keys) {
        Map<AEItemKey, Long> pInputItems = new HashMap<>(keys.inputItems);
        Map<AEFluidKey, Long> pInputFluids = new HashMap<>(keys.inputFluids);

        List<Content> inItems = recipe.inputs.get(ItemRecipeCapability.CAP);
        List<Content> inFluids = recipe.inputs.get(FluidRecipeCapability.CAP);
        if (!consumeRecipeInputs(inItems, pInputItems, pInputFluids, false, scale)) return "输入物品不匹配或无法静态确认";
        if (!consumeRecipeInputs(inFluids, pInputItems, pInputFluids, true, scale)) return "输入流体不匹配或无法静态确认";
        if (!allConsumed(pInputItems, pInputFluids)) {
            return "样板输入多余:" + firstLeftover(pInputItems, pInputFluids);
        }

        Map<AEItemKey, Long> pOutputItems = new HashMap<>(keys.outputItems);
        Map<AEFluidKey, Long> pOutputFluids = new HashMap<>(keys.outputFluids);

        List<Content> outItems = recipe.outputs.get(ItemRecipeCapability.CAP);
        List<Content> outFluids = recipe.outputs.get(FluidRecipeCapability.CAP);
        if (!consumeRecipeOutputs(outItems, pOutputItems, pOutputFluids, false, scale)) return "输出物品不匹配或配方副产物未写入样板";
        if (!consumeRecipeOutputs(outFluids, pOutputItems, pOutputFluids, true, scale)) return "输出流体不匹配或配方副产物未写入样板";
        if (!allConsumed(pOutputItems, pOutputFluids)) {
            return "样板输出多余:" + firstLeftover(pOutputItems, pOutputFluids);
        }
        return null;
    }

    private static boolean isOptionalContent(Content content) {
        try {
            return content.chance < content.maxChance;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static long scaled(long amount, int scale) {
        if (scale <= 1 || amount <= 0L) return amount;
        return amount > Long.MAX_VALUE / scale ? Long.MAX_VALUE : amount * scale;
    }

    private static String firstLeftover(Map<AEItemKey, Long> itemMap, Map<AEFluidKey, Long> fluidMap) {
        for (var entry : itemMap.entrySet()) {
            if (entry.getValue() != 0L) return entry.getKey().getId().toString();
        }
        for (var entry : fluidMap.entrySet()) {
            if (entry.getValue() != 0L) return entry.getKey().getFluid().toString();
        }
        return "?";
    }

    private static boolean collectPatternKeys(
                                              GenericStack[] stacks,
                                              Map<AEItemKey, Long> items,
                                              Map<AEFluidKey, Long> fluids) {
        for (GenericStack stack : stacks) {
            if (stack == null || stack.what() == null) continue;
            AEKey key = stack.what();
            long amount = stack.amount();
            if (key instanceof AEItemKey itemKey) {
                items.merge(itemKey, amount, Long::sum);
            } else if (key instanceof AEFluidKey fluidKey) {
                fluids.merge(fluidKey, amount, Long::sum);
            } else {
                return false;
            }
        }
        return true;
    }

    private static boolean consumeRecipeInputs(List<Content> contents, Map<AEItemKey, Long> itemMap,
                                               Map<AEFluidKey, Long> fluidMap, boolean isFluid, int scale) {
        if (contents == null) return true;
        for (Content content : contents) {
            Object obj = content.getContent();
            boolean optional = isOptionalContent(content);
            if (isFluid) {

                if (obj instanceof com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient fIng) {
                    long need = scaled(fIng.getAmount(), scale);
                    boolean got = false;
                    for (var fs : fIng.getStacks()) {
                        if (subtractKey(fluidMap, AEFluidKey.of(fs.getFluid(), fs.getTag()), need)) {
                            got = true;
                            break;
                        }
                    }
                    if (!got) {
                        if (optional) continue;
                        return false;
                    }
                } else if (obj instanceof com.lowdragmc.lowdraglib.side.fluid.FluidStack ldStack) {
                    if (!subtractKey(fluidMap, AEFluidKey.of(ldStack.getFluid(), ldStack.getTag()),
                            scaled(ldStack.getAmount(), scale))) {
                        if (optional) continue;
                        return false;
                    }
                } else if (obj instanceof net.minecraftforge.fluids.FluidStack stack) {
                    if (!subtractKey(fluidMap, AEFluidKey.of(stack.getFluid()), scaled(stack.getAmount(), scale))) {
                        if (optional) continue;
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (obj instanceof ItemStack stack) {
                    if (!subtractKey(itemMap, AEItemKey.of(stack), scaled(stack.getCount(), scale))) {
                        if (optional) continue;
                        return false;
                    }
                } else if (obj instanceof Ingredient ingredient) {

                    if (isCircuitIngredient(ingredient)) continue;
                    long amount = getIngredientAmount(ingredient);
                    if (amount < 0L) return false;
                    if (!subtractIngredient(itemMap, ingredient, scaled(amount, scale))) {
                        if (optional) continue;
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean consumeRecipeOutputs(List<Content> contents, Map<AEItemKey, Long> itemMap,
                                                Map<AEFluidKey, Long> fluidMap, boolean isFluid, int scale) {
        if (contents == null) return true;
        for (Content content : contents) {
            Object obj = content.getContent();
            boolean optional = isOptionalContent(content);
            if (isFluid) {
                if (obj instanceof com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient fIng) {
                    long need = scaled(fIng.getAmount(), scale);
                    boolean got = false;
                    for (var fs : fIng.getStacks()) {
                        if (subtractKey(fluidMap, AEFluidKey.of(fs.getFluid(), fs.getTag()), need)) {
                            got = true;
                            break;
                        }
                    }
                    if (!got) {
                        if (optional) continue;
                        return false;
                    }
                } else if (obj instanceof com.lowdragmc.lowdraglib.side.fluid.FluidStack ldStack) {
                    if (!subtractKey(fluidMap, AEFluidKey.of(ldStack.getFluid(), ldStack.getTag()),
                            scaled(ldStack.getAmount(), scale))) {
                        if (optional) continue;
                        return false;
                    }
                } else if (obj instanceof net.minecraftforge.fluids.FluidStack stack) {
                    if (!subtractKey(fluidMap, AEFluidKey.of(stack.getFluid()), scaled(stack.getAmount(), scale))) {
                        if (optional) continue;
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (obj instanceof ItemStack stack) {
                    if (!subtractKey(itemMap, AEItemKey.of(stack), scaled(stack.getCount(), scale))) {
                        if (optional) continue;
                        return false;
                    }
                } else if (obj instanceof Ingredient ingredient) {
                    if (isCircuitIngredient(ingredient)) continue;
                    long amount = getIngredientAmount(ingredient);
                    if (amount < 0L) return false;
                    if (!subtractIngredient(itemMap, ingredient, scaled(amount, scale))) {
                        if (optional) continue;
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    private static <K> boolean subtractKey(Map<K, Long> map, K key, long amount) {
        Long left = map.get(key);
        if (left == null || left < amount) return false;
        long rest = left - amount;
        if (rest > 0) map.put(key, rest);
        else map.remove(key);
        return true;
    }

    private static boolean subtractIngredient(Map<AEItemKey, Long> itemMap, Ingredient ingredient, long amount) {
        var it = itemMap.entrySet().iterator();
        while (it.hasNext()) {
            var entry = it.next();
            if (!ingredient.test(entry.getKey().toStack())) continue;
            long left = entry.getValue();
            if (left < amount) return false;
            long rest = left - amount;
            if (rest > 0) entry.setValue(rest);
            else it.remove();
            return true;
        }
        return false;
    }

    private static long getIngredientAmount(Ingredient ingredient) {
        if (ingredient instanceof IntProviderIngredient) return -1L;
        if (ingredient instanceof SizedIngredient sized) return sized.getAmount();
        return 1L;
    }

    private static boolean isCircuitIngredient(Ingredient ingredient) {
        if (ingredient instanceof IntCircuitIngredient) return true;
        return ingredient instanceof SizedIngredient sized && sized.getInner() instanceof IntCircuitIngredient;
    }

    private static boolean allConsumed(Map<AEItemKey, Long> itemMap, Map<AEFluidKey, Long> fluidMap) {
        for (long v : itemMap.values()) if (v != 0L) return false;
        for (long v : fluidMap.values()) if (v != 0L) return false;
        return true;
    }
}
