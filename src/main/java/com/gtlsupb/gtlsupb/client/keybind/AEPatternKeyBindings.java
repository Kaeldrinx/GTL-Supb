package com.gtlsupb.gtlsupb.client.keybind;

import net.minecraft.client.KeyMapping;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.settings.KeyConflictContext;

import appeng.api.stacks.AEKey;
import appeng.client.gui.me.common.MEStorageScreen;
import appeng.client.gui.me.common.RepoSlot;
import appeng.menu.me.common.GridInventoryEntry;
import com.gtlsupb.gtlsupb.network.C2SAEPatternProviderPacket;
import com.gtlsupb.gtlsupb.network.GTLSupbNetworkHandler;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class AEPatternKeyBindings {

    public static final String CATEGORY = "key.categories.gtlsupb";
    public static final String KEY_OPEN_PATTERN_PROVIDER = "key.gtlsupb.ae_pattern_provider";

    public static KeyMapping openPatternProvider;

    public static void register(RegisterKeyMappingsEvent event) {
        openPatternProvider = new KeyMapping(
                KEY_OPEN_PATTERN_PROVIDER,
                KeyConflictContext.GUI,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                CATEGORY);
        event.register(openPatternProvider);
    }

    @OnlyIn(Dist.CLIENT)
    public static void onScreenKeyPressed(ScreenEvent.KeyPressed.Pre event) {
        if (openPatternProvider == null) return;
        var screen = event.getScreen();
        if (!(screen instanceof MEStorageScreen<?> meScreen)) return;
        if (!openPatternProvider.matches(event.getKeyCode(), event.getScanCode())) return;

        Slot slot = meScreen.getSlotUnderMouse();
        if (!(slot instanceof RepoSlot repoSlot)) return;
        GridInventoryEntry entry = repoSlot.getEntry();
        if (entry == null) return;

        if (!entry.isCraftable()) return;
        AEKey key = entry.getWhat();
        if (key == null) return;

        int containerId = meScreen.getMenu().containerId;
        GTLSupbNetworkHandler.CHANNEL.sendToServer(new C2SAEPatternProviderPacket(containerId, key));
        event.setCanceled(true);
    }
}
