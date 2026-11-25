package com.gtlsupb.gtlsupb.common.machine.multiblock;

import org.gtlcore.gtlcore.api.machine.trait.ILockRecipe;
import org.gtlcore.gtlcore.api.machine.trait.IRecipeStatus;
import org.gtlcore.gtlcore.api.recipe.RecipeText;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;

import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;

import com.gtlsupb.gtlsupb.api.machine.logic.MutableRecipesLogic;
import com.gtlsupb.gtlsupb.api.machine.mutable.MutableCoilElectricMultiblockMachine;

import java.util.List;

public class AlchemicalForgeMachine extends MutableCoilElectricMultiblockMachine {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            AlchemicalForgeMachine.class,
            WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    public static final double BASE_PARALLEL = 256.0;
    public static final double COIL_TEMP_DIVISOR = 900.0;

    public AlchemicalForgeMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    private double gtlsupb$parallelRaw() {
        return BASE_PARALLEL * Math.pow(4.0, gtlsupb$coilTemperature() / COIL_TEMP_DIVISOR);
    }

    @Override
    public int getMaxParallel() {
        return (int) Math.min(gtlsupb$parallelRaw(), (double) Integer.MAX_VALUE);
    }

    @Override
    public long getMaxParallelLong() {
        double raw = gtlsupb$parallelRaw();
        if (raw >= (double) Long.MAX_VALUE) return Long.MAX_VALUE;
        return Math.max(1L, (long) raw);
    }

    @Override
    public RecipeLogic createRecipeLogic(Object... args) {
        return new MutableRecipesLogic<MutableCoilElectricMultiblockMachine>(this) {

            @Override
            protected boolean gtlsupb$limitTotalEu() {
                return false;
            }
        };
    }

    @Override
    @SuppressWarnings("unchecked")
    public MutableRecipesLogic<MutableCoilElectricMultiblockMachine> getRecipeLogic() {
        return (MutableRecipesLogic<MutableCoilElectricMultiblockMachine>) super.getRecipeLogic();
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        if (isFormed()) {
            addMachineModeDisplay(textList);

            textList.add(
                    Component.translatable(
                            "gtlsupb.machine.max_voltage_tier",
                            Component.literal(GTValues.VN[getTier()]).withStyle(ChatFormatting.LIGHT_PURPLE),
                            Component.literal(String.valueOf(GTValues.V[getTier()])).withStyle(ChatFormatting.YELLOW))
                            .withStyle(ChatFormatting.GRAY));
            addWorkingStatus(textList);

            IRecipeStatus status = getRecipeLogic() instanceof IRecipeStatus ? (IRecipeStatus) getRecipeLogic() : null;
            if (status != null) {
                if (status.getRecipeStatus() != null && status.getRecipeStatus().reason() != null) {
                    textList.add(status.getRecipeStatus().reason().copy().withStyle(ChatFormatting.RED));
                }
                if (status.getWorkingStatus() != null && status.getWorkingStatus().reason() != null) {
                    textList.add(status.getWorkingStatus().reason().copy().withStyle(ChatFormatting.RED));
                }
            }
        } else {
            textList.add(
                    Component.translatable("gtceu.multiblock.invalid_structure")
                            .withStyle(
                                    Style.EMPTY.withColor(ChatFormatting.RED)
                                            .withHoverEvent(
                                                    new HoverEvent(
                                                            HoverEvent.Action.SHOW_TEXT,
                                                            Component.translatable("gtceu.multiblock.invalid_structure.tooltip")
                                                                    .withStyle(ChatFormatting.GRAY)))));
        }

        if (isFormed()) {

            long parallel = getMaxParallelLong();
            int coilTemp = (int) gtlsupb$coilTemperature();

            textList.add(
                    Component.translatable(
                            "gtlsupb.machine.alchemical_forge.coil_temp",
                            Component.literal(coilTemp + " K").withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.GRAY));

            Component parallelLine = Component.translatable(
                    "gtlsupb.machine.parallel_info",
                    Component.literal(String.valueOf(parallel)).withStyle(ChatFormatting.AQUA)).withStyle(ChatFormatting.GRAY);
            textList.add(parallelLine);
        }

        ILockRecipe lockRecipe = getRecipeLogic() instanceof ILockRecipe ? (ILockRecipe) getRecipeLogic() : null;
        if (lockRecipe != null) {
            addRecipeLockDisplay(textList, lockRecipe);
        }
        getDefinition().getAdditionalDisplay().accept(this, textList);
        getParts().forEach(it -> it.addMultiText(textList));
    }

    private void addMachineModeDisplay(List<Component> textList) {
        textList.add(
                Component.translatable(
                        "gtceu.gui.machinemode",
                        Component.translatable(getRecipeType().registryName.toLanguageKey())).withStyle(ChatFormatting.AQUA));
    }

    private void addWorkingStatus(List<Component> textList) {
        if (!isWorkingEnabled()) {
            textList.add(Component.translatable("gtceu.multiblock.work_paused").withStyle(ChatFormatting.GOLD));
        } else if (isActive()) {
            textList.add(Component.translatable("gtceu.multiblock.running").withStyle(ChatFormatting.GREEN));
        } else {
            textList.add(Component.translatable("gtceu.multiblock.idling"));
        }
    }

    private void addRecipeLockDisplay(List<Component> textList, ILockRecipe iLockRecipe) {
        Component text;
        if (iLockRecipe.isLock() && iLockRecipe.getLockRecipe() != null) {
            text = Component.translatable("gui.gtlcore.recipe_lock.recipe").withStyle(style -> style.withHoverEvent(
                    new HoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            RecipeText.getRecipeInputText(iLockRecipe.getLockRecipe())
                                    .append(RecipeText.getRecipeOutputText(iLockRecipe.getLockRecipe())))));
        } else {
            text = Component.translatable("gui.gtlcore.recipe_lock.no_recipe");
        }
        textList.add(text);
    }
}
