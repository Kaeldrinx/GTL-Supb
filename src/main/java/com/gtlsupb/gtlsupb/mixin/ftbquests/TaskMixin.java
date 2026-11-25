package com.gtlsupb.gtlsupb.mixin.ftbquests;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import com.gtlsupb.gtlsupb.integration.ftbquests.AEQuestSubmitHelper;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.FluidTask;
import dev.ftb.mods.ftbquests.quest.task.Task;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Task.class, remap = false)
public class TaskMixin {

    @Inject(method = "submitTask(Ldev/ftb/mods/ftbquests/quest/TeamData;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At("HEAD"),
            remap = false)
    private void gtlSupb$submitFluidFromAE(TeamData teamData, ServerPlayer player, ItemStack craftedItem,
                                           CallbackInfo ci) {
        Task self = (Task) (Object) this;

        if (!(self instanceof FluidTask fluidTask)) return;
        if (teamData.isCompleted(self)) return;
        if (!fluidTask.consumesResources()) return;

        AEQuestSubmitHelper.submitFromAE(teamData, player, self);
    }
}
