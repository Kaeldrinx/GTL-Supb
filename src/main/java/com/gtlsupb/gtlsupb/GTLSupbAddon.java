package com.gtlsupb.gtlsupb;

import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;

import com.gtlsupb.gtlsupb.api.registry.GTLSupbRegistration;
import com.gtlsupb.gtlsupb.common.block.BlockMap;
import com.gtlsupb.gtlsupb.common.data.GTLSupbBedrockFluids;
import com.gtlsupb.gtlsupb.common.data.GTLSupbBlocks;
import com.gtlsupb.gtlsupb.common.data.GTLSupbCovers;
import com.gtlsupb.gtlsupb.common.data.GTLSupbElements;
import com.gtlsupb.gtlsupb.common.data.GTLSupbItems;
import com.gtlsupb.gtlsupb.common.data.GTLSupbSoundEntries;
import com.gtlsupb.gtlsupb.common.data.GTLSupbTagPrefix;
import com.gtlsupb.gtlsupb.data.recipe.GTLSupbMachineRecipes;
import com.gtlsupb.gtlsupb.data.recipe.RemoveRecipe;

import java.util.function.Consumer;

@GTAddon
public class GTLSupbAddon implements IGTAddon {

    @Override
    public String addonModId() {
        return GTLSupb.MOD_ID;
    }

    @Override
    public GTRegistrate getRegistrate() {
        return GTLSupbRegistration.REGISTRATE;
    }

    @Override
    public boolean requiresHighTier() {
        return true;
    }

    @Override
    public void initializeAddon() {
        GTLSupbItems.init();
        GTLSupbBlocks.init();
        BlockMap.init();
    }

    @Override
    public void registerSounds() {
        GTLSupbSoundEntries.init();
    }

    @Override
    public void registerCovers() {
        GTLSupbCovers.init();
    }

    @Override
    public void registerElements() {
        GTLSupbElements.init();
    }

    @Override
    public void registerTagPrefixes() {
        GTLSupbTagPrefix.init();
    }

    @Override
    public void addRecipes(Consumer<FinishedRecipe> provider) {
        GTLSupbMachineRecipes.init(provider);
    }

    @Override
    public void removeRecipes(Consumer<ResourceLocation> consumer) {
        RemoveRecipe.init(consumer);
    }

    @Override
    public void registerFluidVeins() {
        GTLSupbBedrockFluids.init();
    }
}
