package com.gtlsupb.gtlsupb.mixin.gtlcore;

import org.gtlcore.gtlcore.api.machine.trait.RecipeHandlePart;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.IRecipeHandlerTrait;

import com.gtlsupb.gtlsupb.common.machine.multiblock.part.OverlimitIntegratedHatchPartMachine;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

@Mixin(value = WorkableMultiblockMachine.class, priority = 500, remap = false)
public class IntegratedHatchRecipeHandlerMixin {

    @Inject(method = "handleNormalPart", at = @At("TAIL"), remap = false)
    private void gtlsupb$collectIntegratedHatchInputs(IMultiPart part, boolean isDistinct,
                                                      List<IRecipeHandler<?>> sharedInputHandlers,
                                                      CallbackInfo ci) {
        if (!(part instanceof OverlimitIntegratedHatchPartMachine)) return;
        List<IRecipeHandler<?>> inputs = new ObjectArrayList<>();
        for (IRecipeHandler<?> handler : part.getRecipeHandlers()) {
            if (handler == null || handler.isProxy()) continue;
            if (!(handler instanceof IRecipeHandlerTrait<?> trait) || trait.getHandlerIO() != IO.IN) continue;
            inputs.add(handler);
        }
        if (inputs.isEmpty()) return;
        if (!isDistinct) {
            sharedInputHandlers.addAll(inputs);
            return;
        }
        Map<IO, List<RecipeHandlePart>> capabilities = gtlsupb$normalCapabilities();
        if (capabilities == null) return;
        capabilities.computeIfAbsent(IO.IN, ignore -> new ObjectArrayList<>())
                .add(RecipeHandlePart.of(IO.IN, inputs));
    }

    @SuppressWarnings("unchecked")
    private Map<IO, List<RecipeHandlePart>> gtlsupb$normalCapabilities() {
        Class<?> type = getClass();
        while (type != null && type != WorkableMultiblockMachine.class) {
            type = type.getSuperclass();
        }
        if (type == null) return null;
        try {
            Field field = type.getDeclaredField("normalCapabilities");
            field.setAccessible(true);
            return (Map<IO, List<RecipeHandlePart>>) field.get(this);
        } catch (Throwable ignored) {
            return null;
        }
    }
}
