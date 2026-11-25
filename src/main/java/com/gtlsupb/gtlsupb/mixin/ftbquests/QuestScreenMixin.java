package com.gtlsupb.gtlsupb.mixin.ftbquests;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import com.gtlsupb.gtlsupb.client.ftbquests.AEQuestSubmitScreen;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestButton;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestPanel;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.task.FluidTask;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import dev.ftb.mods.ftbquests.quest.task.Task;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = QuestScreen.class, remap = false)
public abstract class QuestScreenMixin {

    @Shadow
    @Final
    public QuestPanel questPanel;

    @Shadow
    public abstract Quest getViewedQuest();

    @Unique
    private boolean gtlsupb$altWasDown = false;

    @Inject(method = "tick", at = @At("HEAD"))
    private void gtlsupb$openSubmitOnAltHover(CallbackInfo ci) {
        boolean altDown = Screen.hasAltDown();
        if (!altDown) {
            gtlsupb$altWasDown = false;
            return;
        }

        if (gtlsupb$altWasDown) {
            return;
        }
        gtlsupb$altWasDown = true;

        if (getViewedQuest() != null) {
            return;
        }

        QuestButton hovered = ((QuestPanelAccessor) questPanel).getMouseOverQuest();
        if (hovered == null) {
            return;
        }

        Quest quest = ((QuestButtonAccessor) hovered).getQuest();
        if (quest == null || !quest.canBeRepeated() || !gtlsupb$hasConsumableTask(quest)) {
            return;
        }

        final long questId = quest.id;
        final Component title = quest.getTitle();
        Minecraft mc = Minecraft.getInstance();

        mc.tell(() -> mc.setScreen(new AEQuestSubmitScreen(questId, title)));
    }

    @Unique
    private static boolean gtlsupb$hasConsumableTask(Quest quest) {
        for (Task task : quest.getTasks()) {
            if (task instanceof ItemTask || task instanceof FluidTask) {
                if (task.consumesResources()) return true;
            }
        }
        return false;
    }
}
