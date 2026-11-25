package com.gtlsupb.gtlsupb.mixin.gtlcore;

import org.gtlcore.gtlcore.api.machine.trait.IMERecipeHandlerTrait;
import org.gtlcore.gtlcore.api.recipe.ingredient.LongIngredient;
import org.gtlcore.gtlcore.common.machine.multiblock.part.ae.MEPatternBufferPartMachine;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;

import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

import com.gtlsupb.gtlsupb.api.item.tool.ae2.Ae2PatternHelper;
import com.gtlsupb.gtlsupb.api.machine.IPatternAssemblyHatch;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = MEPatternBufferPartMachine.class, remap = false)
@Implements(@Interface(iface = IPatternAssemblyHatch.class, prefix = "gtlsupb$binding$"))
public abstract class MEPatternBufferBindingMixin {

    @Accessor(value = "patternInventory", remap = false)
    abstract ItemStackTransfer gtlsupb$accessPatternInventory();

    @Shadow(remap = false)
    public abstract Pair<IMERecipeHandlerTrait<Ingredient, ItemStack>, IMERecipeHandlerTrait<FluidIngredient, FluidStack>> getMERecipeHandlerTraits();

    public ItemStackTransfer gtlsupb$binding$getPatternInventory() {
        return gtlsupb$accessPatternInventory();
    }

    public void gtlsupb$binding$onPatternInserted(int slot, ItemStack stack) {}

    public List<GTRecipeType> gtlsupb$binding$getAvailableRecipeTypes() {
        List<GTRecipeType> types = new ArrayList<>();
        for (GTRecipeType type : gtlsupb$binding$getBoundRecipeTypes()) {
            if (!types.contains(type)) types.add(type);
        }
        return types;
    }

    public int gtlsupb$binding$getActiveRecipeTypeIndex() {
        return 0;
    }

    public void gtlsupb$binding$setActiveRecipeTypeIndex(int index) {}

    public List<GTRecipe> gtlsupb$binding$getBoundRecipes() {
        return new ArrayList<>(gtlsupb$binding$getSlotBoundRecipes().values());
    }

    public List<GTRecipeType> gtlsupb$binding$getBoundRecipeTypes() {
        List<GTRecipeType> types = new ArrayList<>();
        for (GTRecipe recipe : gtlsupb$binding$getBoundRecipes()) {
            GTRecipeType type = recipe.recipeType;
            if (type != null && !types.contains(type)) types.add(type);
        }
        return types;
    }

    public Int2ObjectMap<GTRecipe> gtlsupb$binding$getSlotBoundRecipes() {
        Int2ObjectMap<GTRecipe> map = new Int2ObjectOpenHashMap<>();
        Level level = ((MetaMachine) (Object) this).getLevel();
        if (level == null) return map;
        ItemStackTransfer inventory = gtlsupb$accessPatternInventory();
        if (inventory == null) return map;
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.isEmpty()) continue;
            GTRecipe recipe = Ae2PatternHelper.readBindingRecipe(stack, level);
            if (recipe != null) map.put(slot, recipe);
        }
        return map;
    }

    public long gtlsupb$binding$getSlotBoundRecipeParallel(int slot, GTRecipe recipe, long limit) {
        Pair<IMERecipeHandlerTrait<Ingredient, ItemStack>, IMERecipeHandlerTrait<FluidIngredient, FluidStack>> pair = getMERecipeHandlerTraits();
        if (pair == null) return 0L;
        long p = limit;

        java.util.List<Content> itemInputs = recipe.inputs.get(ItemRecipeCapability.CAP);
        if (itemInputs != null && !itemInputs.isEmpty()) {
            Object2LongMap<Ingredient> need = gtlsupb$buildItemNeed(itemInputs);
            if (!need.isEmpty()) {
                Object2LongMap<ItemStack> stock = pair.left().getSingleSlotStackMap(slot);
                for (var needed : Object2LongMaps.fastIterable(need)) {
                    long amount = needed.getLongValue();
                    if (amount <= 0L) continue;
                    long avail = 0L;
                    for (var it = Object2LongMaps.fastIterator(stock); it.hasNext();) {
                        var st = it.next();
                        if (needed.getKey().test(st.getKey())) {
                            avail = st.getLongValue();
                            break;
                        }
                    }
                    if (avail <= 0L) return 0L;
                    p = Math.min(p, avail / amount);
                }
            }
        }

        java.util.List<Content> fluidInputs = recipe.inputs.get(FluidRecipeCapability.CAP);
        if (fluidInputs != null && !fluidInputs.isEmpty()) {
            Object2LongMap<FluidIngredient> need = gtlsupb$buildFluidNeed(fluidInputs);
            if (!need.isEmpty()) {
                Object2LongMap<FluidStack> stock = pair.right().getSingleSlotStackMap(slot);
                for (var needed : Object2LongMaps.fastIterable(need)) {
                    long amount = needed.getLongValue();
                    if (amount <= 0L) continue;
                    long avail = 0L;
                    for (var it = Object2LongMaps.fastIterator(stock); it.hasNext();) {
                        var st = it.next();
                        if (needed.getKey().test(st.getKey())) {
                            avail = st.getLongValue();
                            break;
                        }
                    }
                    if (avail <= 0L) return 0L;
                    p = Math.min(p, avail / amount);
                }
            }
        }
        return p;
    }

    public boolean gtlsupb$binding$verifySlotBoundRecipes(Int2ObjectMap<GTRecipe> paralleledBySlot) {
        return gtlsupb$handleSlotBoundRecipes(paralleledBySlot, true);
    }

    public boolean gtlsupb$binding$commitSlotBoundRecipes(Int2ObjectMap<GTRecipe> paralleledBySlot) {
        return gtlsupb$handleSlotBoundRecipes(paralleledBySlot, false);
    }

    private boolean gtlsupb$handleSlotBoundRecipes(Int2ObjectMap<GTRecipe> paralleledBySlot, boolean simulate) {
        Pair<IMERecipeHandlerTrait<Ingredient, ItemStack>, IMERecipeHandlerTrait<FluidIngredient, FluidStack>> pair = getMERecipeHandlerTraits();
        if (pair == null) return false;
        for (var entry : Int2ObjectMaps.fastIterable(paralleledBySlot)) {
            int slot = entry.getIntKey();
            GTRecipe recipe = entry.getValue();

            java.util.List<Content> itemInputs = recipe.inputs.get(ItemRecipeCapability.CAP);
            if (itemInputs != null && !itemInputs.isEmpty()) {
                ObjectArrayList<Object> contents = new ObjectArrayList<>();
                for (Content c : itemInputs) {
                    if (c.chance <= 0) continue;
                    contents.add(ItemRecipeCapability.CAP.of(c.content));
                }
                if (!contents.isEmpty()) {
                    pair.left().initMEHandleContents(recipe, contents, simulate);
                    if (!pair.left().meHandleRecipe(recipe, simulate, slot)) return false;
                }
            }

            java.util.List<Content> fluidInputs = recipe.inputs.get(FluidRecipeCapability.CAP);
            if (fluidInputs != null && !fluidInputs.isEmpty()) {
                ObjectArrayList<Object> contents = new ObjectArrayList<>();
                for (Content c : fluidInputs) {
                    if (c.chance <= 0) continue;
                    contents.add(FluidRecipeCapability.CAP.of(c.content));
                }
                if (!contents.isEmpty()) {
                    pair.right().initMEHandleContents(recipe, contents, simulate);
                    if (!pair.right().meHandleRecipe(recipe, simulate, slot)) return false;
                }
            }
        }
        return true;
    }

    private static Object2LongMap<Ingredient> gtlsupb$buildItemNeed(java.util.List<Content> contents) {
        Object2LongOpenHashMap<Ingredient> need = new Object2LongOpenHashMap<>();
        for (Content c : contents) {
            if (c.chance <= 0) continue;
            Ingredient ing = ItemRecipeCapability.CAP.of(c.content);
            if (ing == null || gtlsupb$isCircuitIngredient(ing)) continue;
            long amount;
            if (ing instanceof LongIngredient longIngredient) amount = longIngredient.getActualAmount();
            else if (ing instanceof com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient sized) amount = sized.getAmount();
            else amount = 1L;
            need.addTo(ing, amount);
        }
        return need;
    }

    private static Object2LongMap<FluidIngredient> gtlsupb$buildFluidNeed(java.util.List<Content> contents) {
        Object2LongOpenHashMap<FluidIngredient> need = new Object2LongOpenHashMap<>();
        for (Content c : contents) {
            if (c.chance <= 0) continue;
            FluidIngredient fluid = FluidRecipeCapability.CAP.of(c.content);
            if (fluid == null) continue;
            long amount = fluid.getAmount();
            if (amount <= 0L) continue;
            need.addTo(fluid, amount);
        }
        return need;
    }

    private static boolean gtlsupb$isCircuitIngredient(Ingredient ing) {
        for (ItemStack stack : ing.getItems()) {
            if (IntCircuitBehaviour.isIntegratedCircuit(stack)) return true;
        }
        return false;
    }
}
