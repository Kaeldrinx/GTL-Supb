package com.gtlsupb.gtlsupb.common.data;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import java.util.List;

public class ParallelData {

    public final List<GTRecipe> originRecipeList;
    public final long[] parallels;
    public final boolean shouldProcess;
    public final List<GTRecipe> processedRecipeList;

    public ParallelData(List<GTRecipe> originRecipeList, long[] parallels) {
        this(originRecipeList, parallels, true, null);
    }

    public ParallelData(List<GTRecipe> originRecipeList, long[] parallels, boolean shouldProcess) {
        this(originRecipeList, parallels, shouldProcess, null);
    }

    public ParallelData(List<GTRecipe> originRecipeList, long[] parallels, boolean shouldProcess,
                        List<GTRecipe> processedRecipeList) {
        this.originRecipeList = originRecipeList;
        this.parallels = parallels;
        this.shouldProcess = shouldProcess;
        this.processedRecipeList = processedRecipeList;
    }
}
