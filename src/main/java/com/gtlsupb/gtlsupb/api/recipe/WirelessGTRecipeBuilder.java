package com.gtlsupb.gtlsupb.api.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import net.minecraft.resources.ResourceLocation;

import java.math.BigInteger;
import java.util.List;

public class WirelessGTRecipeBuilder extends GTRecipeBuilder {

    public BigInteger wirelessEut;

    public WirelessGTRecipeBuilder(ResourceLocation id, GTRecipeType recipeType) {
        super(id, recipeType);
    }

    public WirelessGTRecipeBuilder(GTRecipe toCopy, GTRecipeType recipeType) {
        super(toCopy, recipeType);
    }

    public WirelessGTRecipeBuilder setWirelessEut(BigInteger wirelessEut) {
        this.wirelessEut = wirelessEut;
        return this;
    }

    public WirelessGTRecipeBuilder output(RecipeCapability<?> cap, List<Content> contents) {
        output.put(cap, contents);
        return this;
    }

    @Override
    public WirelessGTRecipeBuilder duration(int duration) {
        this.duration = duration;
        return this;
    }

    @Override
    public WirelessGTRecipe buildRawRecipe() {
        return new WirelessGTRecipe(
                recipeType,
                id.withPrefix(recipeType.registryName.getPath() + "/"),
                input,
                output,
                tickInput,
                tickOutput,
                inputChanceLogic,
                outputChanceLogic,
                tickInputChanceLogic,
                tickOutputChanceLogic,
                conditions,
                List.of(),
                data,
                duration,
                isFuel,
                wirelessEut);
    }

    public static WirelessGTRecipeBuilder ofRaw() {
        return ofRaw(GTRecipeTypes.DUMMY_RECIPES);
    }

    public static WirelessGTRecipeBuilder ofRaw(GTRecipeType recipeType) {
        return new WirelessGTRecipeBuilder(GTCEu.id("raw"), recipeType);
    }
}
