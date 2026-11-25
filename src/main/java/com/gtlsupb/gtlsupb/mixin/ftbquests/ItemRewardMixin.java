package com.gtlsupb.gtlsupb.mixin.ftbquests;

import net.minecraft.server.level.ServerPlayer;

import com.gtlsupb.gtlsupb.integration.ftbquests.AEQuestRewardHandler;
import dev.ftb.mods.ftbquests.quest.reward.ItemReward;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ItemReward.class, remap = false)
public class ItemRewardMixin {

    @Inject(method = "claim(Lnet/minecraft/server/level/ServerPlayer;Z)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    private void gtlSupb$redirectRewardToAE(ServerPlayer player, boolean notify,
                                            CallbackInfo ci) {
        ItemReward self = (ItemReward) (Object) this;

        if (AEQuestRewardHandler.tryClaimRewardToAE(player, self)) {
            ci.cancel();
        }
    }
}
