package com.gtlsupb.gtlsupb.common.block;

import org.gtlcore.gtlcore.utils.Registries;

import com.gregtechceu.gtceu.api.GTCEuAPI;

import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.util.Lazy;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Comparator;
import java.util.function.Supplier;

public interface BlockMap {

    Object2ObjectOpenHashMap<String, Lazy<Block[]>> tierBlockMap = new Object2ObjectOpenHashMap<>(4);

    Int2ObjectOpenHashMap<Supplier<?>> scMap = new Int2ObjectOpenHashMap<>(4);

    Int2ObjectOpenHashMap<Supplier<?>> sepmMap = new Int2ObjectOpenHashMap<>(8);

    Int2ObjectOpenHashMap<Supplier<?>> calMap = new Int2ObjectOpenHashMap<>(16);

    static void init() {
        scMap.put(1, () -> Registries.getBlock("gtlcore:stellar_containment_casing"));
        scMap.put(2, () -> Registries.getBlock("gtlcore:advanced_stellar_containment_casing"));
        scMap.put(3, () -> Registries.getBlock("gtlcore:ultimate_stellar_containment_casing"));

        for (int i = 1; i <= 7; i++) {
            String name = i == 1 ? "power_module" : "power_module_" + i;
            sepmMap.put(i, () -> Registries.getBlock("gtlcore:" + name));
        }

        String[] voltageNames = { "lv", "mv", "hv", "ev", "iv", "luv", "zpm", "uv", "uhv", "uev", "uiv", "uxv", "opv", "max" };
        for (int i = 0; i < voltageNames.length; i++) {
            int tier = i + 1;
            String name = voltageNames[i];
            calMap.put(tier, () -> Registries.getBlock("gtlcore:component_assembly_line_casing_" + name));
        }

        tierBlockMap.put("sc", Lazy.of(() -> scMap.int2ObjectEntrySet().stream()
                .sorted(Comparator.comparingInt(it -> it.getIntKey()))
                .map(it -> it.getValue())
                .map(it -> (Block) it.get())
                .toArray(Block[]::new)));
        tierBlockMap.put("sepm", Lazy.of(() -> sepmMap.int2ObjectEntrySet().stream()
                .sorted(Comparator.comparingInt(it -> it.getIntKey()))
                .map(it -> it.getValue())
                .map(it -> (Block) it.get())
                .toArray(Block[]::new)));
        tierBlockMap.put("cal", Lazy.of(() -> calMap.int2ObjectEntrySet().stream()
                .sorted(Comparator.comparingInt(it -> it.getIntKey()))
                .map(it -> it.getValue())
                .map(it -> (Block) it.get())
                .toArray(Block[]::new)));
        tierBlockMap.put("coil", Lazy.of(() -> GTCEuAPI.HEATING_COILS.entrySet().stream()
                .sorted(Comparator.comparingInt(it -> it.getKey().getCoilTemperature()))
                .map(it -> it.getValue())
                .map(it -> it.get())
                .toArray(Block[]::new)));
        tierBlockMap.put("casing", Lazy.of(() -> {
            Block[] casings = new Block[com.gregtechceu.gtceu.api.GTValues.MAX + 1];
            for (int tier = 0; tier <= com.gregtechceu.gtceu.api.GTValues.MAX; tier++) {
                casings[tier] = Registries.getBlock("gtceu:" +
                        com.gregtechceu.gtceu.api.GTValues.VN[tier].toLowerCase(java.util.Locale.ROOT) +
                        "_machine_casing");
            }
            return casings;
        }));
    }
}
