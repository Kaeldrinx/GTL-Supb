package com.gtlsupb.gtlsupb.common.machine.multiblock;

import org.gtlcore.gtlcore.common.data.GTLRecipeTypes;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;

import net.minecraft.server.level.ServerLevel;

public class FragmentWorldCollector extends ZeroEnergyMultiTypeMachine {

    public FragmentWorldCollector(IMachineBlockEntity holder) {
        super(holder, GTLRecipeTypes.FRAGMENT_WORLD_COLLECTION);
    }

    @Override
    public RecipeLogic createRecipeLogic(Object... args) {
        return new FragmentWorldCollectorLogic(this);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().execute(() -> {
                if (!isRemote() && isFormed()) {
                    onChanged();
                }
            });
        }
    }
}
