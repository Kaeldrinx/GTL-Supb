package com.gtlsupb.gtlsupb.mixin.ftbquests;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import com.gtlsupb.gtlsupb.integration.ftbquests.AEQuestSubmitHelper;
import dev.ftb.mods.ftbquests.item.MissingItem;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ItemTask.class, remap = false)
public class ItemTaskMixin {

    @Inject(method = "submitTask(Ldev/ftb/mods/ftbquests/quest/TeamData;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At("HEAD"),
            remap = false)
    private void gtlSupb$submitFromAE(TeamData teamData, ServerPlayer player, ItemStack craftedItem,
                                      CallbackInfo ci) {
        ItemTask self = (ItemTask) (Object) this;

        if (!self.consumesResources()) return;
        if (teamData.isCompleted(self)) return;
        if (self.isTaskScreenOnly()) return;
        ItemStack taskItem = self.getItemStack();
        if (taskItem != null && taskItem.getItem() instanceof MissingItem) return;

        AEQuestSubmitHelper.submitFromAE(teamData, player, self);
    }
}
