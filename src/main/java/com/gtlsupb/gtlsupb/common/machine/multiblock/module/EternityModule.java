package com.gtlsupb.gtlsupb.common.machine.multiblock.module;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;

public class EternityModule extends MicrocosmFoundryModuleBase {

    public EternityModule(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public boolean isEternityModule() {
        return true;
    }

    @Override
    public GTRecipeType[] getModuleRecipeTypes() {
        return new GTRecipeType[] {};
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        textList.add(
                Component.literal("§d§k||§r §6§l超因果永恒熵逆 §d§k||§r")
                        .withStyle(ChatFormatting.GOLD));
    }
}
