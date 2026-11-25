package com.gtlsupb.gtlsupb.common.modify;

import org.gtlcore.gtlcore.common.data.GTLRecipeTypes;

import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import net.minecraft.data.recipes.FinishedRecipe;

import com.gtlsupb.gtlsupb.GTLSupb;
import com.gtlsupb.gtlsupb.common.machine.multiblock.GTLSupbNoEnergyIntegratedProcessor;
import com.gtlsupb.gtlsupb.common.recipe.GTLSupbRecipeTypes;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class RecipesModify {

    private RecipesModify() {}

    @SuppressWarnings("unchecked")
    public static void init() {
        GTRecipeType target = GTLRecipeTypes.INTEGRATED_ORE_PROCESSOR;
        BiConsumer<GTRecipeBuilder, Consumer<FinishedRecipe>>[] chained = new BiConsumer[1];
        target.prepareBuilder(builder -> chained[0] = builder.onSave);
        target.onRecipeBuild((builder, provider) -> {
            BiConsumer<GTRecipeBuilder, Consumer<FinishedRecipe>> previous = chained[0];
            if (previous != null) {
                previous.accept(builder, provider);
            }
            copyToSteam(builder, provider);
        });
    }

    private static void copyToSteam(GTRecipeBuilder builder, Consumer<FinishedRecipe> provider) {
        long originalEut = sumEu(builder);
        GTRecipeBuilder steamBuilder = GTLSupbRecipeTypes.STEAM_SPACE_ORE_PROCESSOR
                .copyFrom(builder)
                .EUt(0);

        if (builder.id != null) {
            steamBuilder.id(GTLSupb.id("steam_" + builder.id.getPath()));
        }
        steamBuilder.input.remove(FluidRecipeCapability.CAP);
        if (originalEut > 0L) {
            steamBuilder.data.putLong(GTLSupbNoEnergyIntegratedProcessor.STEAM_EU_KEY, originalEut);
        }
        steamBuilder.save(provider);
    }

    private static long sumEu(GTRecipeBuilder builder) {
        long total = 0L;
        total += sumContents(builder.tickInput.get(EURecipeCapability.CAP));
        total += sumContents(builder.input.get(EURecipeCapability.CAP));
        return total;
    }

    private static long sumContents(List<Content> contents) {
        if (contents == null) return 0L;
        long total = 0L;
        for (Content content : contents) {
            if (content.content instanceof Number number) {
                total += number.longValue();
            }
        }
        return total;
    }
}
