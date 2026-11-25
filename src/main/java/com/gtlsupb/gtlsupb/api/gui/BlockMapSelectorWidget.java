package com.gtlsupb.gtlsupb.api.gui;

import com.gregtechceu.gtceu.api.gui.GuiTextures;

import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.util.Lazy;

import com.gtlsupb.gtlsupb.common.block.BlockMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

public class BlockMapSelectorWidget extends WidgetGroup {

    public static final Component SC = Component.translatable("gui.gtlsupb.stellar_thermal_container");
    public static final Component SEPM = Component.translatable("gui.gtlsupb.space_elevator_module");
    public static final Component CAL = Component.translatable("gui.gtlsupb.component_assembly_casing");
    public static final Component COIL = Component.translatable("gui.gtlsupb.coil");
    public static final Component CASING = Component.translatable("gui.gtlsupb.machine_casing");

    public static Component getBlock(String string) {
        switch (string) {
            case "sc":
                return SC;
            case "sepm":
                return SEPM;
            case "cal":
                return CAL;
            case "coil":
                return COIL;
            case "casing":
                return CASING;
            default:
                return Component.literal(string);
        }
    }

    private final BiConsumer<String, Integer> onChanged;
    private List<Block> blocks = null;
    private String currentType = null;
    private WidgetGroup typeGroup = null;
    private WidgetGroup tierGroup = null;

    public BlockMapSelectorWidget(int y, int width, BiConsumer<String, Integer> onChanged) {
        super(0, y, width, 56);
        this.onChanged = onChanged;
    }

    public void setInit(ItemStack itemStack) {
        var tag = itemStack.getOrCreateTag();
        String block = tag.getString("blocks");
        if (!block.isEmpty()) {
            Lazy<Block[]> lazy = BlockMap.tierBlockMap.get(block);
            if (lazy != null) {
                this.blocks = new ArrayList<>(Arrays.asList(lazy.get()));
            }
            this.currentType = block;
        }
    }

    public void showType(boolean isShow) {
        if (!isShow) {
            this.setVisible(false);
            return;
        }

        if (typeGroup != null) {
            this.removeWidget(typeGroup);
        }
        if (tierGroup != null) {
            this.removeWidget(tierGroup);
        }
        tierGroup = null;

        WidgetGroup group = new WidgetGroup(0, 2, 80, 52);
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        DraggableScrollableWidgetGroup blockType = new DraggableScrollableWidgetGroup(4, 4, 72, 44);
        blockType.setYScrollBarWidth(2);
        blockType.setYBarStyle(null, ColorPattern.T_WHITE.rectTexture().setRadius(1.0F));
        blockType.setBackground(GuiTextures.DISPLAY);
        int y = 0;
        for (String key : BlockMap.tierBlockMap.keySet()) {
            String currentKey = key;
            Lazy<Block[]> lazy = BlockMap.tierBlockMap.get(currentKey);
            List<Block> blocksForKey;
            if (lazy != null) {
                blocksForKey = new ArrayList<>(Arrays.asList(lazy.get()));
            } else {
                blocksForKey = new ArrayList<>();
            }
            blockType.addWidget(new WidgetGroup(2, 2 + y, 66, 15)
                    .addWidget(new ExtendLabelWidget(0, 0, getBlock(key)))
                    .addWidget(new ButtonWidget(0, 0, 64, 15, (clickData) -> {
                        showTier(blocksForKey);
                        currentType = currentKey;
                    })));
            y += 15;
        }
        typeGroup = group.addWidget(blockType);
        this.addWidget(typeGroup);
        this.setVisible(true);
        if (this.blocks != null && !this.blocks.isEmpty()) {
            showTier(this.blocks);
        }
    }

    private void showTier(List<Block> blockList) {
        if (blockList.isEmpty()) return;
        this.blocks = blockList;

        if (tierGroup != null) {
            this.removeWidget(tierGroup);
        }

        WidgetGroup group = new WidgetGroup(80, 2, 120, 52);
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        DraggableScrollableWidgetGroup blockTier = new DraggableScrollableWidgetGroup(4, 4, 112, 44);
        blockTier.setYScrollBarWidth(2);
        blockTier.setYBarStyle(null, ColorPattern.T_WHITE.rectTexture().setRadius(1.0F));
        blockTier.setBackground(GuiTextures.DISPLAY);
        for (int i = 0; i < blockList.size(); i++) {
            int finalI = i;
            Block block = blockList.get(finalI);
            blockTier.addWidget(new WidgetGroup(2, 2 + i * 20, 110, 22)
                    .addWidget(new ImageWidget(2, 0, 18, 18, new ItemStackTexture(block.asItem().getDefaultInstance())))
                    .addWidget(new ExtendLabelWidget(20, 4, Component.translatable(block.getDescriptionId())))
                    .addWidget(new ButtonWidget(20, 0, 88, 18, (clickData) -> {
                        String type = currentType;
                        if (type != null) {
                            onChanged.accept(type, finalI);
                        }
                    })));
        }
        tierGroup = group.addWidget(blockTier);
        this.addWidget(tierGroup);
    }
}
