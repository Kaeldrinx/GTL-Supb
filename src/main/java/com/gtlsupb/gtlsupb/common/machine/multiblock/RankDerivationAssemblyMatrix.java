package com.gtlsupb.gtlsupb.common.machine.multiblock;

import org.gtlcore.gtlcore.common.data.GTLRecipeTypes;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;

public class RankDerivationAssemblyMatrix extends ZeroEnergyMultiTypeMachine {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            RankDerivationAssemblyMatrix.class,
            ZeroEnergyMultiTypeMachine.MANAGED_FIELD_HOLDER);

    public RankDerivationAssemblyMatrix(IMachineBlockEntity holder) {
        super(holder,
                GTRecipeTypes.ASSEMBLER_RECIPES,
                GTRecipeTypes.CIRCUIT_ASSEMBLER_RECIPES,
                GTLRecipeTypes.PRECISION_ASSEMBLER_RECIPES,
                GTLRecipeTypes.COMPONENT_ASSEMBLY_LINE_RECIPES,
                GTLRecipeTypes.CIRCUIT_ASSEMBLY_LINE_RECIPES,
                GTRecipeTypes.ASSEMBLY_LINE_RECIPES,
                GTLRecipeTypes.SUPRACHRONAL_ASSEMBLY_LINE_RECIPES,
                GTLRecipeTypes.ASSEMBLER_MODULE_RECIPES);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public RecipeLogic createRecipeLogic(Object... args) {
        return new RankDerivationAssemblyMatrixLogic(this);
    }

    @Override
    protected void addMachineModeDisplay(List<Component> textList) {
        textList.add(
                Component.translatable(
                        "gtlsupb.gui.machine_mode.multitype",
                        Component.translatable("gtlsupb.machine_mode.order_derivation")).withStyle(ChatFormatting.AQUA));
    }
}
