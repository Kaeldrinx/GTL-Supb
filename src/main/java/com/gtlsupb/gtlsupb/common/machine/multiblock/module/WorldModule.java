package com.gtlsupb.gtlsupb.common.machine.multiblock.module;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GCyMRecipeTypes;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;

public class WorldModule extends ForgingShipModuleBase {

    public WorldModule(IMachineBlockEntity holder) {
        super(holder, GCyMRecipeTypes.ALLOY_BLAST_RECIPES);
    }

    @Override
    public GTRecipeType[] getModuleRecipeTypes() {
        return new GTRecipeType[] { GCyMRecipeTypes.ALLOY_BLAST_RECIPES };
    }

    @Override
    protected void addMachineModeDisplay(List<Component> textList) {
        textList.add(
                Component.translatable(
                        "gtlsupb.gui.machine_mode.multitype",
                        Component.translatable("gtlsupb.machine_mode.world")).withStyle(ChatFormatting.AQUA));
    }
}
