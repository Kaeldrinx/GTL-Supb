package com.gtlsupb.gtlsupb.common.item;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.item.component.IItemUIFactory;

import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class PatternDebuggerBehavior
                                     implements IItemUIFactory,
                                     com.gregtechceu.gtceu.api.item.component.IInteractionItem {

    @Override
    public net.minecraft.world.InteractionResult useOn(net.minecraft.world.item.context.UseOnContext context) {
        net.minecraft.world.entity.player.Player player = context.getPlayer();
        net.minecraft.world.level.Level level = context.getLevel();
        if (player == null) return net.minecraft.world.InteractionResult.PASS;
        if (level.isClientSide) return net.minecraft.world.InteractionResult.SUCCESS;
        if (!player.isShiftKeyDown()) return net.minecraft.world.InteractionResult.PASS;
        if (!(level.getBlockEntity(context.getClickedPos()) instanceof com.gregtechceu.gtceu.api.machine.IMachineBlockEntity holder)) {
            return net.minecraft.world.InteractionResult.PASS;
        }
        if (!(holder.getMetaMachine() instanceof com.gtlsupb.gtlsupb.api.machine.IPatternAssemblyHatch assembly)) {
            return net.minecraft.world.InteractionResult.PASS;
        }
        var inv = assembly.getPatternInventory();
        if (inv == null) {
            return net.minecraft.world.InteractionResult.PASS;
        }
        if (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) {
            return net.minecraft.world.InteractionResult.PASS;
        }
        net.minecraft.world.item.ItemStack tool = context.getItemInHand();
        var tag = tool.getOrCreateTag();
        String type = tag.getString("debug_type");
        if (type.isEmpty()) {
            String resolved = PatternDebuggerItem.resolveTypeFromInventory(player);
            if (resolved != null) type = resolved;
        }
        if (type.isEmpty()) {
            player.displayClientMessage(
                    Component.translatable("gtlsupb.pattern_debugger.bad_type", ""), false);
            return net.minecraft.world.InteractionResult.CONSUME;
        }
        java.util.List<net.minecraft.world.item.ItemStack> patterns = PatternDebuggerItem.buildPatterns(
                serverPlayer, type,
                tag.getInt("debug_circuit"),
                Math.max(1, tag.getInt("debug_scale")),
                tag.getString("debug_iw"), tag.getString("debug_ib"),
                tag.getString("debug_ow"), tag.getString("debug_ob"));
        int moved = 0;
        for (net.minecraft.world.item.ItemStack stack : patterns) {
            if (stack.isEmpty()) continue;
            for (int slot = 0; slot < inv.getSlots(); slot++) {
                if (!inv.getStackInSlot(slot).isEmpty()) continue;
                inv.setStackInSlot(slot, stack);
                assembly.onPatternInserted(slot, stack);
                moved++;
                break;
            }
        }
        if (moved > 0) {
            player.displayClientMessage(
                    Component.translatable("gtlsupb.pattern_debugger.inserted", moved), false);
            return net.minecraft.world.InteractionResult.CONSUME;
        }
        return net.minecraft.world.InteractionResult.PASS;
    }

    public static final PatternDebuggerBehavior INSTANCE = new PatternDebuggerBehavior();

    private static String[] buildTypes() {
        java.util.List<String> out = new java.util.ArrayList<>();
        try {
            for (var type : com.gregtechceu.gtceu.api.registry.GTRegistries.RECIPE_TYPES.values()) {
                if (type == null || type.registryName == null) continue;
                String ns = type.registryName.getNamespace();
                if (!"gtceu".equals(ns) && !"gtlcore".equals(ns) && !"gtlsupb".equals(ns)) continue;
                out.add(type.registryName.toString());
            }
        } catch (Throwable ignored) {}
        out.sort(String::compareTo);
        if (out.isEmpty()) {
            out.addAll(java.util.List.of("gtceu:assembler", "gtceu:chemical_reactor",
                    "gtceu:electric_furnace", "gtceu:mixer", "gtceu:centrifuge"));
        }
        return out.toArray(new String[0]);
    }

    private static final String[] TYPES = buildTypes();

    private static final String PRESET_CIRCUIT = "gtceu:programmed_circuit";

    private static final String PRESET_MOLD = "gtceu:shaped_mold gtceu:extruder_mold";

    private PatternDebuggerBehavior() {}

    private static CompoundTag tag(ItemStack stack) {
        return stack.getOrCreateTag();
    }

    private static String get(ItemStack stack, String key, String fallback) {
        CompoundTag t = tag(stack);
        return t.contains(key) ? t.getString(key) : fallback;
    }

    private static int getInt(ItemStack stack, String key, int fallback) {
        CompoundTag t = tag(stack);
        return t.contains(key) ? t.getInt(key) : fallback;
    }

    private static void set(ItemStack stack, String key, String value) {
        tag(stack).putString(key, value == null ? "" : value);
    }

    private static void setInt(ItemStack stack, String key, int value) {
        tag(stack).putInt(key, value);
    }

    private static Component typeName(String type) {
        ResourceLocation id = ResourceLocation.tryParse(type.contains(":") ? type : "gtceu:" + type);
        return id == null ? Component.literal(type) : Component.translatable(id.toLanguageKey());
    }

    @Override
    public ModularUI createUI(HeldItemUIFactory.HeldItemHolder holder, Player player) {
        ItemStack stack = holder.getHeld();
        WidgetGroup root = new WidgetGroup(0, 0, 220, 216);
        root.setBackground(GuiTextures.BACKGROUND);
        root.addWidget(new ImageWidget(4, 4, 212, 208, GuiTextures.DISPLAY));

        WidgetGroup main = buildMainPanel(holder, stack);
        WidgetGroup picker = buildPicker(holder, stack, main);
        picker.setVisible(false);
        this.pageMain = main;
        this.pagePicker = picker;
        root.addWidget(main);
        root.addWidget(picker);

        return new ModularUI(220, 216, holder, player).widget(root);
    }

    private WidgetGroup buildMainPanel(HeldItemUIFactory.HeldItemHolder holder, ItemStack stack) {
        WidgetGroup main = new WidgetGroup(0, 0, 220, 216);
        int circuit = getInt(stack, "debug_circuit", 0);
        int scale = Math.max(1, getInt(stack, "debug_scale", 1));

        main.addWidget(new LabelWidget(8, 8,
                Component.translatable("gtlsupb.pattern_debugger.title").getString()));

        main.addWidget(new ButtonWidget(8, 20, 204, 16,
                new GuiTextureGroup(GuiTextures.BUTTON, new TextTexture(
                        Component.translatable("gtlsupb.pattern_debugger.pick_short").getString())),
                c -> switchPage(true)));
        main.addWidget(new LabelWidget(8, 36, () -> "▶ " + typeName(get(stack, "debug_type", TYPES[0])).getString()));

        main.addWidget(new LabelWidget(8, 50, () -> Component.translatable("gtlsupb.pattern_debugger.info_circuit",
                getInt(stack, "debug_circuit", 0)).getString()));
        main.addWidget(new ButtonWidget(146, 48, 26, 14,
                new GuiTextureGroup(GuiTextures.BUTTON, new TextTexture("-")),
                c -> setInt(stack, "debug_circuit", Math.max(0, getInt(stack, "debug_circuit", 0) - 1))));
        main.addWidget(new ButtonWidget(180, 48, 26, 14,
                new GuiTextureGroup(GuiTextures.BUTTON, new TextTexture("+")),
                c -> setInt(stack, "debug_circuit", Math.min(24, getInt(stack, "debug_circuit", 0) + 1))));

        main.addWidget(new LabelWidget(8, 64, () -> Component.translatable("gtlsupb.pattern_debugger.generatable",
                PatternDebuggerItem.gtlsupb$countGeneratable(holder.getPlayer(),
                        get(stack, "debug_type", TYPES[0]),
                        getInt(stack, "debug_circuit", 0),
                        get(stack, "debug_iw", ""), get(stack, "debug_ib", ""),
                        get(stack, "debug_ow", ""), get(stack, "debug_ob", "")))
                .getString()));

        listRow(main, holder, stack, 8, 88, "debug_iw", "gtlsupb.pattern_debugger.in_white", false);
        listRow(main, holder, stack, 8, 110, "debug_ib", "gtlsupb.pattern_debugger.in_black", true);
        listRow(main, holder, stack, 8, 132, "debug_ow", "gtlsupb.pattern_debugger.out_white", false);
        listRow(main, holder, stack, 8, 154, "debug_ob", "gtlsupb.pattern_debugger.out_black", true);

        main.addWidget(new LabelWidget(8, 188,
                Component.translatable("gtlsupb.pattern_debugger.insert_hint").getString()));

        return main;
    }

    private WidgetGroup buildPicker(HeldItemUIFactory.HeldItemHolder holder, ItemStack stack, WidgetGroup main) {
        WidgetGroup page = new WidgetGroup(0, 0, 220, 216);
        page.addWidget(new LabelWidget(8, 8,
                Component.translatable("gtlsupb.pattern_debugger.pick_title").getString()));

        DraggableScrollableWidgetGroup list = new DraggableScrollableWidgetGroup(8, 20, 204, 164);
        list.setBackground(GuiTextures.DISPLAY);
        String current = get(stack, "debug_type", TYPES[0]);
        for (int i = 0; i < TYPES.length; i++) {
            String type = TYPES[i];
            boolean active = type.equals(current);
            list.addWidget(new ButtonWidget(2, i * 18, 196, 16,
                    new GuiTextureGroup(GuiTextures.BUTTON,
                            new TextTexture((active ? "▶ " : "") + typeName(type).getString())),
                    c -> {
                        set(stack, "debug_type", type);

                        page.setVisible(false);
                        main.setVisible(true);
                    }));
        }
        page.addWidget(list);

        page.addWidget(new ButtonWidget(8, 190, 204, 16,
                new GuiTextureGroup(GuiTextures.BUTTON,
                        new TextTexture(Component.translatable("gtlsupb.pattern_debugger.back").getString())),
                c -> {
                    page.setVisible(false);
                    main.setVisible(true);
                }));
        return page;
    }

    private WidgetGroup pageMain;

    private WidgetGroup pagePicker;

    private void switchPage(boolean toPicker) {
        if (pageMain != null) pageMain.setVisible(!toPicker);
        if (pagePicker != null) pagePicker.setVisible(toPicker);
    }

    private void listRow(WidgetGroup parent, HeldItemUIFactory.HeldItemHolder holder, ItemStack stack,
                         int x, int y, String key, String labelKey, boolean withPreset) {
        parent.addWidget(new LabelWidget(x, y + 1, Component.translatable(labelKey).getString()));
        parent.addWidget(new com.gregtechceu.gtceu.integration.ae2.gui.widget.AETextInputButtonWidget(
                x + 60, y, 82, 12)
                .setText(get(stack, key, ""))
                .setOnConfirm(s -> set(stack, key, s.trim()))
                .setButtonTooltips(Component.translatable(labelKey)));

        parent.addWidget(new ButtonWidget(x + 148, y, 16, 14,
                new GuiTextureGroup(GuiTextures.BUTTON, new TextTexture("+")),
                c -> {
                    ItemStack main = holder.getPlayer().getMainHandItem();
                    if (main.isEmpty()) return;
                    String id = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(main.getItem()).toString();
                    String old = get(stack, key, "");
                    set(stack, key, old.isBlank() ? id : old + " " + id);
                    switchPage(false);
                }).setHoverTooltips(Component.translatable("gtlsupb.pattern_debugger.add_hand.tip")));

        if (withPreset) {
            parent.addWidget(new ButtonWidget(x + 166, y, 20, 14,
                    new GuiTextureGroup(GuiTextures.BUTTON, new TextTexture(
                            Component.translatable("gtlsupb.pattern_debugger.preset_circuit").getString())),
                    c -> {
                        set(stack, key, PRESET_CIRCUIT);
                        switchPage(false);
                    }));
            parent.addWidget(new ButtonWidget(x + 188, y, 20, 14,
                    new GuiTextureGroup(GuiTextures.BUTTON, new TextTexture(
                            Component.translatable("gtlsupb.pattern_debugger.preset_mold").getString())),
                    c -> {
                        set(stack, key, PRESET_MOLD);
                        switchPage(false);
                    }));
        } else {
            parent.addWidget(new ButtonWidget(x + 166, y, 46, 14,
                    new GuiTextureGroup(GuiTextures.BUTTON, new TextTexture(
                            Component.translatable("gtlsupb.pattern_debugger.clear").getString())),
                    c -> {
                        set(stack, key, "");
                        switchPage(false);
                    }));
        }
    }
}
