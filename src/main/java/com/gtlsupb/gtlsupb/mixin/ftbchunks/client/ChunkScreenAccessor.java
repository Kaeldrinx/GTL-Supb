package com.gtlsupb.gtlsupb.mixin.ftbchunks.client;

import dev.ftb.mods.ftbchunks.client.gui.ChunkScreen;
import dev.ftb.mods.ftblibrary.math.XZ;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(value = ChunkScreen.class, remap = false)
public interface ChunkScreenAccessor {

    @Accessor("selectedChunks")
    Set<XZ> getSelectedChunks();
}
