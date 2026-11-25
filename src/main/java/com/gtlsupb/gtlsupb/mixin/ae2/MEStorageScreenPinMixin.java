package com.gtlsupb.gtlsupb.mixin.ae2;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;

import appeng.api.stacks.AEKey;
import appeng.client.gui.me.common.MEStorageScreen;
import appeng.client.gui.me.common.PinnedKeys;
import appeng.client.gui.me.common.Repo;
import appeng.client.gui.me.common.RepoSlot;
import appeng.menu.me.common.GridInventoryEntry;
import com.gtlsupb.gtlsupb.GTLSupb;
import com.gtlsupb.gtlsupb.client.ae.ManualPinnedKeys;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MEStorageScreen.class)
public abstract class MEStorageScreenPinMixin {

    @Shadow(remap = false)
    @Final
    protected Repo repo;

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void gtlsupb$onMouseClicked(double mouseX, double mouseY, int button,
                                        CallbackInfoReturnable<Boolean> cir) {
        if (button != 0) return;
        if (!Screen.hasAltDown()) return;

        Slot slot = ((MEStorageScreen<?>) (Object) this).getSlotUnderMouse();
        if (!(slot instanceof RepoSlot repoSlot)) return;

        GridInventoryEntry entry = repoSlot.getEntry();
        if (entry == null) return;
        AEKey key = entry.getWhat();
        if (key == null) return;

        if (PinnedKeys.isPinned(key)) {
            PinnedKeys.unpin(key);
            ManualPinnedKeys.remove(key);
        } else {

            if (PinnedKeys.getPinnedKeys().size() >= PinnedKeys.MAX_PINNED) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    mc.player.displayClientMessage(Component.translatable("gtlsupb.ae_pin.full"), true);
                }
                cir.setReturnValue(true);
                return;
            }
            try {

                PinnedKeys.pinKey(key, PinnedKeys.PinReason.CRAFTING);
            } catch (Throwable t) {

                GTLSupb.LOGGER.warn("[PinMixin] pinKey 失败（置顶栏可能已满）: {}", t.getMessage());
            }
            ManualPinnedKeys.add(key);
        }

        this.repo.updateView();
        cir.setReturnValue(true);
    }

    @Inject(method = "removed", at = @At("RETURN"))
    private void gtlsupb$protectManualPins(CallbackInfo ci) {
        for (AEKey key : ManualPinnedKeys.all()) {
            PinnedKeys.PinInfo info = PinnedKeys.getPinInfo(key);
            if (info != null) {
                info.canPrune = false;
            }
        }
    }
}
