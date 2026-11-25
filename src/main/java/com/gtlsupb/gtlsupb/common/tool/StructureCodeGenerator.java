package com.gtlsupb.gtlsupb.common.tool;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class StructureCodeGenerator {

    private StructureCodeGenerator() {}

    private static final char[] CHAR_POOL;
    static {
        StringBuilder pool = new StringBuilder();
        for (char c = 'A'; c <= 'Z'; c++) pool.append(c);
        for (char c = 'a'; c <= 'z'; c++) pool.append(c);
        for (char c = '0'; c <= '9'; c++) pool.append(c);
        pool.append("!@#$%^&*-+=_|:;<>?/.");
        CHAR_POOL = pool.toString().toCharArray();
    }

    public static class ExportResult {

        public final List<Component> messages;
        public final boolean success;

        public ExportResult(List<Component> messages, boolean success) {
            this.messages = messages;
            this.success = success;
        }
    }

    public static ExportResult export(Level level, BlockPos pos1, BlockPos pos2, String playerName) {
        List<Component> messages = new ArrayList<>();

        int minX = Math.min(pos1.getX(), pos2.getX());
        int minY = Math.min(pos1.getY(), pos2.getY());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());
        int maxX = Math.max(pos1.getX(), pos2.getX());
        int maxY = Math.max(pos1.getY(), pos2.getY());
        int maxZ = Math.max(pos1.getZ(), pos2.getZ());

        int width = maxX - minX + 1;
        int height = maxY - minY + 1;
        int length = maxZ - minZ + 1;

        long totalBlocks = (long) width * height * length;
        if (totalBlocks > 1_000_000) {
            messages.add(Component.translatable("gtlsupb.structure_exporter.too_large",
                    width, height, length, totalBlocks).withStyle(ChatFormatting.RED));
            return new ExportResult(messages, false);
        }

        messages.add(Component.translatable("gtlsupb.structure_exporter.scanning",
                width, height, length).withStyle(ChatFormatting.GRAY));

        LinkedHashMap<String, Character> blockToChar = new LinkedHashMap<>();
        blockToChar.put("minecraft:air", ' ');

        int poolIdx = 0;

        for (int z = 0; z < length; z++) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    BlockPos pos = new BlockPos(minX + x, minY + y, minZ + z);
                    BlockState state = level.getBlockState(pos);
                    String blockId = getBlockId(state.getBlock());

                    if (!blockToChar.containsKey(blockId)) {
                        if (blockId.equals("minecraft:air")) continue;

                        if (poolIdx >= CHAR_POOL.length) {
                            messages.add(Component.translatable("gtlsupb.structure_exporter.too_many_blocks",
                                    CHAR_POOL.length).withStyle(ChatFormatting.RED));
                            return new ExportResult(messages, false);
                        }
                        blockToChar.put(blockId, CHAR_POOL[poolIdx++]);
                    }
                }
            }
        }

        int uniqueBlocks = blockToChar.size() - 1;
        messages.add(Component.translatable("gtlsupb.structure_exporter.unique_blocks",
                uniqueBlocks).withStyle(ChatFormatting.GRAY));

        List<String[]> layers = new ArrayList<>();

        for (int z = 0; z < length; z++) {
            String[] layerRows = new String[height];
            for (int y = 0; y < height; y++) {
                StringBuilder row = new StringBuilder();
                for (int x = 0; x < width; x++) {
                    BlockPos pos = new BlockPos(minX + x, minY + y, minZ + z);
                    BlockState state = level.getBlockState(pos);
                    String blockId = getBlockId(state.getBlock());
                    Character ch = blockToChar.get(blockId);
                    row.append(ch != null ? ch : ' ');
                }
                layerRows[y] = row.toString();
            }
            layers.add(layerRows);
        }

        String classPrefix = "ExportedStructure";
        String packageName = "com.gtlsupb.gtlsupb.common.data.machines.multiblock";

        String layersCode = generateLayersCode(packageName, classPrefix, layers);
        String patternCode = generatePatternCode(packageName, classPrefix, layers.size(), blockToChar);

        Path outputDir = Paths.get("config", "gtlsupb", "exported_patterns");
        try {
            Files.createDirectories(outputDir);

            Path layersFile = outputDir.resolve(classPrefix + "PatternLayers.java");
            Files.writeString(layersFile, layersCode);

            Path patternFile = outputDir.resolve(classPrefix + "Pattern.java");
            Files.writeString(patternFile, patternCode);

            messages.add(Component.translatable("gtlsupb.structure_exporter.export_success",
                    classPrefix + "PatternLayers.java",
                    classPrefix + "Pattern.java").withStyle(ChatFormatting.GREEN));
            messages.add(Component.translatable("gtlsupb.structure_exporter.output_dir",
                    outputDir.toAbsolutePath().toString()).withStyle(ChatFormatting.AQUA));

            messages.add(Component.literal("=== where ===").withStyle(ChatFormatting.YELLOW));
            for (Map.Entry<String, Character> e : blockToChar.entrySet()) {
                char ch = e.getValue();
                if (ch == ' ') continue;
                messages.add(Component.literal(
                        String.format(
                                ".where('%c', Predicates.blocks(BuiltInRegistries.BLOCK.get(new ResourceLocation(\"%s\"))))",
                                ch, e.getKey()))
                        .withStyle(ChatFormatting.WHITE));
            }
            messages.add(Component.literal(".where(' ', Predicates.any())")
                    .withStyle(ChatFormatting.WHITE));
            messages.add(Component.literal(".build()").withStyle(ChatFormatting.WHITE));

        } catch (IOException e) {
            messages.add(Component.translatable("gtlsupb.structure_exporter.export_failed",
                    e.getMessage()).withStyle(ChatFormatting.RED));
            return new ExportResult(messages, false);
        }

        return new ExportResult(messages, true);
    }

    private static String generateLayersCode(String packageName, String classPrefix,
                                             List<String[]> layers) {
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(packageName).append(";\n\n");
        sb.append("public final class ").append(classPrefix).append("PatternLayers {\n\n");
        sb.append("    private ").append(classPrefix).append("PatternLayers() {}\n\n");

        for (int i = 0; i < layers.size(); i++) {
            String layerName = String.format("LAYER_%03d", i + 1);
            sb.append("    public static final String[] ").append(layerName).append(" = new String[] {\n");
            for (String row : layers.get(i)) {
                sb.append("        \"").append(row).append("\",\n");
            }
            sb.append("    };\n\n");
        }

        sb.append("}\n");
        return sb.toString();
    }

    private static String generatePatternCode(String packageName, String classPrefix,
                                              int layerCount,
                                              LinkedHashMap<String, Character> blockToChar) {
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(packageName).append(";\n\n");
        sb.append("import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;\n");
        sb.append("import com.gregtechceu.gtceu.api.pattern.Predicates;\n");
        sb.append("import net.minecraft.core.registries.BuiltInRegistries;\n");
        sb.append("import net.minecraft.resources.ResourceLocation;\n\n");
        sb.append("public final class ").append(classPrefix).append("Pattern {\n\n");
        sb.append("    private ").append(classPrefix).append("Pattern() {}\n\n");
        sb.append("    public static final FactoryBlockPattern PATTERN = FactoryBlockPattern.start()\n");

        for (int i = 0; i < layerCount; i++) {
            String layerRef = String.format("%sPatternLayers.LAYER_%03d", classPrefix, i + 1);
            sb.append("            .aisle(").append(layerRef).append(")\n");
        }

        for (Map.Entry<String, Character> e : blockToChar.entrySet()) {
            char ch = e.getValue();
            if (ch == ' ') continue;
            sb.append(String.format(
                    "            .where('%c', Predicates.blocks(BuiltInRegistries.BLOCK.get(new ResourceLocation(\"%s\"))))\n",
                    ch, e.getKey()));
        }
        sb.append("            .where(' ', Predicates.any())\n");
        sb.append("            .build();\n\n");
        sb.append("}\n");
        return sb.toString();
    }

    private static String getBlockId(Block block) {
        ResourceLocation rl = BuiltInRegistries.BLOCK.getKey(block);
        return rl.toString();
    }
}
