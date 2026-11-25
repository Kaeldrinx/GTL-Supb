package com.gtlsupb.gtlsupb.mixin.gtlcore;

import org.gtlcore.gtlcore.common.data.GTLMachines;
import org.gtlcore.gtlcore.common.data.machines.MultiBlockMachineA;
import org.gtlcore.gtlcore.common.machine.multiblock.steam.LargeSteamParallelMultiblockMachine;

import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.steam.SteamEnergyRecipeHandler;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;

import com.gtlsupb.gtlsupb.common.machine.multiblock.part.HugeSteamHatchPartMachine;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mixin(value = LargeSteamParallelMultiblockMachine.class, priority = 2001, remap = false)
public abstract class LargeSteamParallelMultiblockMachineMixin extends WorkableMultiblockMachine {

    public LargeSteamParallelMultiblockMachineMixin(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Unique
    private boolean gtlsupb$isLarge;
    @Unique
    private boolean gtlsupb$isHuge;
    @Unique
    private boolean gtlsupb$meHatch;

    @Shadow(remap = false)
    private boolean isOC;
    @Shadow(remap = false)
    private int amountOC;
    @Shadow(remap = false)
    @Final
    private int max_parallels;

    private static final String HUGE_STEAM_HATCH_ID = "gtladditions:huge_steam_input_hatch";

    /**
     * Overwrites the recipe modifier to apply steam-machine parallel and voltage limits.
     *
     * @author GTLSupb
     * @reason Steam machines require custom parallel and voltage handling.
     */
    @Overwrite(remap = false)
    public static GTRecipe recipeModifier(MetaMachine machine, @NotNull GTRecipe recipe, double reductionDuration) {
        if (machine instanceof LargeSteamParallelMultiblockMachineMixin machine1) {
            boolean isHuge = machine1.gtlsupb$isHuge;
            boolean isLarge = machine1.gtlsupb$isLarge;
            boolean meHatch = machine1.gtlsupb$meHatch;
            long maxEUt = meHatch || isHuge ? 512 : (isLarge ? 128 : 32);
            if (RecipeHelper.getInputEUt(recipe) > maxEUt) return null;
            GTRecipe result = GTRecipeModifiers.accurateParallel(machine, recipe, machine1.max_parallels, false).getFirst();
            recipe = result == recipe ? result.copy() : result;
            if (isHuge) reductionDuration = 0.0;
            if (machine1.getDefinition() == MultiBlockMachineA.DIMENSIONALLY_TRANSCENDENT_STEAM_OVEN)
                recipe.tickInputs.put(EURecipeCapability.CAP, List.of(new Content((long) Math.max(1.0, (double) RecipeHelper.getInputEUt(recipe) * 0.01), ChanceLogic.getMaxChancedValue(), ChanceLogic.getMaxChancedValue(), 0, null, null)));
            double durationDivider = (isLarge || meHatch) ? Math.pow(2.0, machine1.amountOC) : 1.0;
            recipe.duration = (int) Math.max(1.0, (double) recipe.duration * reductionDuration / durationDivider);
        }
        return recipe;
    }

    /**
     * Overwrites structure formation to inject the steam parallel handler.
     *
     * @author GTLSupb
     * @reason Steam machines need an extra parallel handler on structure formation.
     */
    @Overwrite(remap = false)
    public void onStructureFormed() {
        super.onStructureFormed();
        List<IRecipeHandler<?>> handlers = this.capabilitiesProxy.get(IO.IN, FluidRecipeCapability.CAP);
        if (handlers != null) {
            Iterator<IRecipeHandler<?>> itr = handlers.iterator();
            while (itr.hasNext()) {
                IRecipeHandler<?> handler = itr.next();
                if (handler instanceof NotifiableFluidTank tank) {

                    if (tank.getMachine() instanceof HugeSteamHatchPartMachine) {
                        this.gtlsupb$meHatch = true;
                        this.isOC = true;
                        itr.remove();
                        addSteamEnergyHandler(tank, 0.5d * Math.pow(3.0d, this.amountOC));
                        return;
                    }
                    if (tank.getFluidInTank(0).isFluidEqual(GTMaterials.Steam.getFluid(1L))) {
                        MachineDefinition definition = tank.getMachine().getDefinition();
                        this.gtlsupb$isLarge = definition == GTLMachines.LARGE_STEAM_HATCH;
                        this.gtlsupb$isHuge = definition.getId() != null && HUGE_STEAM_HATCH_ID.equals(definition.getId().toString());
                        this.isOC = this.gtlsupb$isLarge || this.gtlsupb$isHuge;
                        Object2IntMap<RecipeCapability<?>> recipeOutputLimits = new Object2IntOpenHashMap<>();
                        recipeOutputLimits.put(ItemRecipeCapability.CAP, this.gtlsupb$isHuge ? 3 : 1);
                        MultiBlockMachineA.LARGE_STEAM_MACERATOR.setRecipeOutputLimits(recipeOutputLimits);
                        itr.remove();
                        addSteamEnergyHandler(tank, 0.5d * (this.gtlsupb$isHuge ? 250.0 : (this.gtlsupb$isLarge ? Math.pow(3.0, this.amountOC) : 1.0)));
                        return;
                    }
                }
            }
        }
    }

    @Unique
    private void addSteamEnergyHandler(NotifiableFluidTank tank, double rate) {
        if (!this.capabilitiesProxy.contains(IO.IN, EURecipeCapability.CAP)) {
            this.capabilitiesProxy.put(IO.IN, EURecipeCapability.CAP, new ArrayList<>());
        }
        this.capabilitiesProxy.get(IO.IN, EURecipeCapability.CAP).add(new SteamEnergyRecipeHandler(tank, rate));
    }
}
