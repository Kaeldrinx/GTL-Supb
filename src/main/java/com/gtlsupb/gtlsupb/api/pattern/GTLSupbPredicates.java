package com.gtlsupb.gtlsupb.api.pattern;

import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.pattern.error.PatternStringError;

import com.lowdragmc.lowdraglib.utils.BlockInfo;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

import java.util.function.Supplier;

public final class GTLSupbPredicates {

    private GTLSupbPredicates() {}

    public static TraceabilityPredicate tierCasings(Int2ObjectMap<Supplier<Block>> map, String tierType) {
        BlockInfo[] blockInfos = new BlockInfo[map.size()];
        int index = 0;
        for (Supplier<Block> blockSupplier : map.values()) {
            Block block = blockSupplier.get();
            blockInfos[index] = BlockInfo.fromBlockState(block.defaultBlockState());
            ++index;
        }
        return new TraceabilityPredicate(blockWorldState -> {
            BlockState blockState = blockWorldState.getBlockState();
            for (Int2ObjectMap.Entry<Supplier<Block>> entry : map.int2ObjectEntrySet()) {
                if (!blockState.is(entry.getValue().get())) continue;
                int stats = entry.getIntKey();
                Object currentTier = blockWorldState.getMatchContext().getOrPut(tierType, stats);
                if (!currentTier.equals(stats)) {
                    blockWorldState.setError(new PatternStringError("gtceu.multiblock.pattern.error.tier"));
                    return false;
                }
                return true;
            }
            return false;
        }, () -> blockInfos).addTooltips(Component.translatable("gtceu.multiblock.pattern.error.tier"));
    }
}
