package com.gtlsupb.gtlsupb.common.item;

import org.gtlcore.gtlcore.api.machine.multiblock.IModularMachineHost;
import org.gtlcore.gtlcore.api.machine.multiblock.IModularMachineModule;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.item.component.IItemUIFactory;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockState;
import com.gregtechceu.gtceu.api.pattern.error.PatternError;
import com.gregtechceu.gtceu.api.pattern.error.PatternStringError;
import com.gregtechceu.gtceu.api.pattern.error.SinglePredicateError;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.utils.BlockInfo;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.crafting.CraftingSubmitErrorCode;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.crafting.ICraftingSimulationRequester;
import appeng.api.networking.crafting.ICraftingSubmitResult;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.me.helpers.PlayerSource;
import com.gtlsupb.gtlsupb.api.gui.BlockMapSelectorWidget;
import com.gtlsupb.gtlsupb.api.gui.ExtendLabelWidget;
import com.gtlsupb.gtlsupb.api.pattern.AdvancedBlockPattern;
import com.gtlsupb.gtlsupb.common.block.BlockMap;
import com.hepdd.gtmthings.api.gui.widget.TerminalInputWidget;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class UltimateTerminalBehavior implements IItemUIFactory {

    public static final UltimateTerminalBehavior INSTANCE = new UltimateTerminalBehavior();

    private static final ThreadLocal<Boolean> AE_CRAFTING_GUARD = ThreadLocal.withInitial(() -> false);

    private static class ModuleItemResult {

        final ItemStack first;
        final boolean second;
        final int third;

        ModuleItemResult(ItemStack first, boolean second, int third) {
            this.first = first;
            this.second = second;
            this.third = third;
        }
    }

    private static class ModuleCacheKey {

        final Block block;
        final Class<?> hostType;

        ModuleCacheKey(Block block, Class<?> hostType) {
            this.block = block;
            this.hostType = hostType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ModuleCacheKey)) return false;
            ModuleCacheKey that = (ModuleCacheKey) o;
            return block == that.block && hostType == that.hostType;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(block) * 31 + hostType.hashCode();
        }
    }

    private final Map<ModuleCacheKey, Boolean> moduleValidCache = Collections.synchronizedMap(new WeakHashMap<>());

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        if (player.isShiftKeyDown()) {
            Level level = context.getLevel();
            BlockPos blockPos = context.getClickedPos();

            if (!level.isClientSide()) {
                if (AdvancedBlockPattern.getGridNode(level.getBlockEntity(blockPos)) != null) {
                    ItemStack handItem = player.getMainHandItem();
                    CompoundTag tag = handItem.getOrCreateTag();
                    CompoundTag aePos = new CompoundTag();
                    aePos.putInt("X", blockPos.getX());
                    aePos.putInt("Y", blockPos.getY());
                    aePos.putInt("Z", blockPos.getZ());
                    aePos.putString("dim", level.dimension().location().toString());
                    tag.put("BoundAE", aePos);
                    handItem.setTag(tag);
                    player.sendSystemMessage(Component.translatable("gtlsupb.terminal.ae_bound", blockPos.toShortString()));
                    return InteractionResult.SUCCESS;
                }

                MetaMachine metaMachine = MetaMachine.getMachine(level, blockPos);
                AutoBuildSetting autoBuildSetting = getAutoBuildSetting(player.getMainHandItem());

                if (autoBuildSetting.isDismantleMode && metaMachine instanceof IMultiController dismantleTarget) {
                    dismantleMultiblock(dismantleTarget, player);
                    return InteractionResult.sidedSuccess(level.isClientSide());
                }

                if ((autoBuildSetting.isModuleMode || autoBuildSetting.isModuleFormMode) && metaMachine instanceof IModularMachineHost) {
                    placeModules((IModularMachineHost) metaMachine, player, autoBuildSetting);
                    return InteractionResult.sidedSuccess(level.isClientSide());
                }

                if (autoBuildSetting.isModuleFormMode && metaMachine instanceof IMultiController moduleController && moduleController.getPattern() != null) {
                    formSingleModule(level, player, moduleController, autoBuildSetting, blockPos);
                    return InteractionResult.sidedSuccess(level.isClientSide());
                }

                if (autoBuildSetting.isDetectMode && metaMachine instanceof IMultiController) {
                    checkStructure((IMultiController) metaMachine, player, autoBuildSetting);
                    return InteractionResult.sidedSuccess(level.isClientSide());
                }

                if (metaMachine instanceof IMultiController) {
                    IMultiController controller = (IMultiController) metaMachine;
                    if (autoBuildSetting.isDismantleMode) {
                        dismantleMultiblock(controller, player);
                    } else {
                        AdvancedBlockPattern.AutoBuildResult result = null;
                        if (!controller.isFormed()) {
                            AdvancedBlockPattern advPattern = AdvancedBlockPattern.getAdvancedBlockPattern(controller.getPattern());
                            if (advPattern != null) result = advPattern.autoBuild(player, controller.getMultiblockState(), autoBuildSetting);
                        } else if (controller instanceof WorkableMultiblockMachine && autoBuildSetting.isReplaceMode) {
                            AdvancedBlockPattern advPattern = AdvancedBlockPattern.getAdvancedBlockPattern(controller.getPattern());
                            if (advPattern != null) result = advPattern.autoBuild(player, controller.getMultiblockState(), autoBuildSetting);
                            ((WorkableMultiblockMachine) controller).onPartUnload();
                        }
                        if (result != null) gtlsupb$showBuildEffects(result, player);
                        List<ItemStack> missingItems = result == null ? Collections.emptyList() : result.missingItems;

                        if (!missingItems.isEmpty() && autoBuildSetting.isAeMode) {
                            handleMissingItemsAECrafting(missingItems, player, autoBuildSetting);
                        } else if (!missingItems.isEmpty()) {

                            player.sendSystemMessage(
                                    Component.translatable("gtlsupb.terminal.missing_blocks", missingItems.size())
                                            .withStyle(ChatFormatting.YELLOW));
                        }
                    }
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return InteractionResult.PASS;
    }

    private void gtlsupb$showBuildEffects(AdvancedBlockPattern.AutoBuildResult result, Player player) {
        if (player.level().isClientSide()) return;
        if (player instanceof ServerPlayer serverPlayer) {
            ServerLevel level = serverPlayer.serverLevel();
            for (BlockPos pos : result.placedPositions) {
                level.sendParticles(serverPlayer, ParticleTypes.HAPPY_VILLAGER, true,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        4, 0.25, 0.25, 0.25, 0.03);
            }
            for (BlockPos pos : result.missingPositions) {
                level.sendParticles(serverPlayer, ParticleTypes.SMOKE, true,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        6, 0.2, 0.3, 0.2, 0.02);
            }
            if (!result.placedPositions.isEmpty() || !result.missingPositions.isEmpty()) {
                serverPlayer.sendSystemMessage(Component.translatable("gtlsupb.terminal.build_summary",
                        result.placedPositions.size(), result.missingPositions.size()).withStyle(ChatFormatting.GREEN));
            }
            if (!result.missingPositions.isEmpty()) {
                BlockPos first = result.missingPositions.get(0);
                serverPlayer.sendSystemMessage(Component.translatable("gtlsupb.terminal.build_missing_first",
                        first.toShortString()).withStyle(ChatFormatting.RED));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void placeModules(IModularMachineHost host, Player player, AutoBuildSetting setting) {
        Level level = player.level();
        if (level.isClientSide()) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        if (!setting.isModuleMode) {
            formConnectedModules(host, player, setting);
            return;
        }

        BlockPos[] scanPositions = host.getModuleScanPositions();
        Set<BlockPos> connectedModules = new HashSet<>();
        for (var module : host.getModules()) {
            if (module instanceof MetaMachine metaMachine) {
                connectedModules.add(metaMachine.getPos());
            }
        }

        if (!(host instanceof MetaMachine hostMachine)) return;
        BlockPos hostPos = hostMachine.getPos();
        Direction hostFacing = hostMachine.getFrontFacing();

        Direction hostUpwardsFacing;
        if (hostMachine instanceof WorkableMultiblockMachine) {
            hostUpwardsFacing = ((WorkableMultiblockMachine) hostMachine).getUpwardsFacing();
        } else {
            hostUpwardsFacing = Direction.UP;
        }

        List<?> moduleRenderInfos = getModuleRenderInfos(host);

        Set<Integer> exhaustedSlots = new HashSet<>();

        int placed = 0;
        for (BlockPos pos : scanPositions) {

            if (connectedModules.contains(pos)) continue;

            if (!level.isEmptyBlock(pos)) {
                MetaMachine existingMachine = MetaMachine.getMachine(level, pos);
                if (existingMachine instanceof IModularMachineModule) continue;

                net.minecraft.world.level.block.state.BlockState blockState = level.getBlockState(pos);
                List<ItemStack> drops = Block.getDrops(blockState, serverLevel, pos, level.getBlockEntity(pos), player, player.getMainHandItem());
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                for (ItemStack drop : drops) {

                    if (setting.isAeMode) {
                        if (!insertItemToAE(player, setting, drop)) {

                            if (!player.getInventory().add(drop)) {
                                player.drop(drop, false);
                            }
                        }
                    } else {

                        if (!player.getInventory().add(drop)) {
                            player.drop(drop, false);
                        }
                    }
                }
            }

            ModuleItemResult result = findAndConsumeModuleItem(host, player, setting, exhaustedSlots);
            if (result == null) continue;

            ItemStack moduleItem = result.first;
            boolean fromAE = result.second;
            int consumedSlot = result.third;

            if (!(moduleItem.getItem() instanceof BlockItem blockItem)) continue;
            BlockPlaceContext context = new BlockPlaceContext(
                    level, player, InteractionHand.MAIN_HAND, moduleItem,
                    BlockHitResult.miss(player.getEyePosition(0f), Direction.UP, pos));
            InteractionResult placeResult = blockItem.place(context);
            if (placeResult != InteractionResult.FAIL) {
                placed++;

                Direction moduleFacing = getModuleFacingFromPreview(moduleRenderInfos, pos, hostPos, hostFacing, hostUpwardsFacing);
                if (moduleFacing == null) moduleFacing = calculateModuleFacing(pos, hostPos, hostFacing);
                fixModuleFacing(level, pos, moduleFacing);

                if (setting.isModuleFormMode) {
                    MetaMachine placedMachine = MetaMachine.getMachine(level, pos);
                    if (placedMachine instanceof IMultiController && ((IMultiController) placedMachine).getPattern() != null) {
                        AutoBuildSetting moduleSetting = getAutoBuildSetting(player.getMainHandItem());
                        AdvancedBlockPattern advPattern = AdvancedBlockPattern.getAdvancedBlockPattern(((IMultiController) placedMachine).getPattern());
                        AdvancedBlockPattern.AutoBuildResult moduleResult = advPattern != null ? advPattern.autoBuild(player, ((IMultiController) placedMachine).getMultiblockState(), moduleSetting) : null;
                        if (moduleResult != null) gtlsupb$showBuildEffects(moduleResult, player);
                        List<ItemStack> moduleMissing = moduleResult == null ? Collections.emptyList() : moduleResult.missingItems;
                        if (!moduleMissing.isEmpty()) {
                            player.sendSystemMessage(
                                    Component.translatable("gtlsupb.terminal.module_missing_blocks",
                                            Component.translatable(blockItem.getDescriptionId()),
                                            moduleMissing.size()).withStyle(ChatFormatting.YELLOW));
                        }
                    }
                }

                player.sendSystemMessage(
                        Component.translatable("gtlsupb.terminal.module_placed",
                                Component.translatable(blockItem.getDescriptionId()),
                                pos.toShortString()).withStyle(ChatFormatting.GREEN));
            } else {

                if (!fromAE && consumedSlot >= 0) {
                    ItemStack slotStack = player.getInventory().getItem(consumedSlot);
                    if (slotStack.isEmpty()) {
                        player.getInventory().setItem(consumedSlot, moduleItem.copy());
                        exhaustedSlots.remove(consumedSlot);
                    } else {
                        slotStack.grow(1);
                    }
                } else if (fromAE) {
                    returnItemToAE(player, setting, moduleItem);
                }
            }
        }

        if (placed == 0) {
            player.sendSystemMessage(
                    Component.translatable("gtlsupb.terminal.no_module_to_place")
                            .withStyle(ChatFormatting.YELLOW));
        } else {
            player.sendSystemMessage(
                    Component.translatable("gtlsupb.terminal.modules_placed", placed)
                            .withStyle(ChatFormatting.GREEN));

            serverLevel.getServer().tell(new TickTask(serverLevel.getServer().getTickCount() + 2, () -> {
                host.scanAndConnectModules();
            }));
        }
    }

    private void formConnectedModules(IModularMachineHost host, Player player, AutoBuildSetting setting) {
        Level level = player.level();
        if (!(level instanceof ServerLevel serverLevel)) return;

        try {
            host.scanAndConnectModules();
        } catch (Throwable ignored) {

        }

        if (host instanceof MetaMachine hostMachine) {
            forcePatternRecheck(hostMachine);
        }

        for (var module : host.getModules()) {
            if (!(module instanceof MetaMachine moduleMachine)) continue;
            BlockPos pos = moduleMachine.getPos();

            MetaMachine placedMachine = MetaMachine.getMachine(level, pos);
            if (placedMachine == null) placedMachine = moduleMachine;

            if (placedMachine instanceof IMultiController controller && controller.getPattern() != null) {
                try {
                    AdvancedBlockPattern advPattern = AdvancedBlockPattern.getAdvancedBlockPattern(controller.getPattern());
                    if (advPattern != null) {
                        advPattern.autoBuild(player, controller.getMultiblockState(), setting);
                    }
                } catch (Throwable ignored) {

                }
            }

            forcePatternRecheck(placedMachine);
            try {
                simulateWrenchRightClick(serverLevel, player, pos);
            } catch (Throwable ignored) {

            }
        }
    }

    private Direction calculateModuleFacing(BlockPos modulePos, BlockPos hostPos, Direction hostFacing) {
        int dx = hostPos.getX() - modulePos.getX();
        int dz = hostPos.getZ() - modulePos.getZ();
        int adx = Math.abs(dx);
        int adz = Math.abs(dz);
        if (adx >= adz && adx > 0) return dx > 0 ? Direction.EAST : Direction.WEST;
        if (adz > 0) return dz > 0 ? Direction.SOUTH : Direction.NORTH;
        return hostFacing;
    }

    private List<?> getModuleRenderInfos(IModularMachineHost host) {
        try {
            java.lang.reflect.Method method = host.getClass().getMethod("getModulesForRendering");
            return (List<?>) method.invoke(host);
        } catch (Exception e) {
            return null;
        }
    }

    private Direction getModuleFacingFromPreview(List<?> renderInfos, BlockPos modulePos, BlockPos hostPos, Direction hostFacing, Direction hostUpwardsFacing) {
        if (renderInfos == null) return null;
        BlockPos moduleOffset = modulePos.subtract(hostPos);
        for (Object info : renderInfos) {
            try {
                Class<?> infoClass = info.getClass();
                java.lang.reflect.Method calcOffsetMethod = infoClass.getMethod(
                        "calculateModuleOffset", Direction.class, Direction.class);
                BlockPos calculatedOffset = (BlockPos) calcOffsetMethod.invoke(info, hostFacing, hostUpwardsFacing);
                if (calculatedOffset.equals(moduleOffset)) {
                    java.lang.reflect.Method calcFrontMethod = infoClass.getMethod(
                            "calculateModuleFront", Direction.class, Direction.class);
                    return (Direction) calcFrontMethod.invoke(info, hostFacing, hostUpwardsFacing);
                }
            } catch (Exception e) {

            }
        }
        return null;
    }

    private void fixModuleFacing(Level level, BlockPos pos, Direction targetFacing) {
        MetaMachine placedMachine = MetaMachine.getMachine(level, pos);
        if (placedMachine == null) return;

        try {
            if (placedMachine.isFacingValid(targetFacing)) {
                placedMachine.setFrontFacing(targetFacing);
            }
        } catch (Exception e) {}

        if (placedMachine.getFrontFacing() != targetFacing) {
            try {
                Class<?> clazz = placedMachine.getClass();
                while (clazz != null) {
                    try {
                        java.lang.reflect.Field field = clazz.getDeclaredField("frontFacing");
                        field.setAccessible(true);
                        field.set(placedMachine, targetFacing);
                        break;
                    } catch (NoSuchFieldException e) {}
                    clazz = clazz.getSuperclass();
                }
            } catch (Exception e) {}
        }

        net.minecraft.world.level.block.state.BlockState state = level.getBlockState(pos);
        for (net.minecraft.world.level.block.state.properties.Property<?> prop : state.getProperties()) {
            if (prop.getName().equals("facing") && prop.getValueClass() == Direction.class) {
                @SuppressWarnings("unchecked")
                net.minecraft.world.level.block.state.properties.DirectionProperty dirProp = (net.minecraft.world.level.block.state.properties.DirectionProperty) prop;
                try {
                    if (dirProp.getPossibleValues().contains(targetFacing)) {
                        net.minecraft.world.level.block.state.BlockState newState = state.setValue(dirProp, targetFacing);
                        if (newState != state) {

                            level.setBlock(pos, newState, 2);
                        }
                    }
                } catch (Exception e) {}
                break;
            }
        }
    }

    private ModuleItemResult findAndConsumeModuleItem(IModularMachineHost host, Player player, AutoBuildSetting setting, Set<Integer> exhaustedSlots) {
        Class<?> hostType = host.getClass();

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (exhaustedSlots.contains(i)) continue;
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;
            Item item = stack.getItem();
            if (!(item instanceof BlockItem)) continue;
            if (!(((BlockItem) item).getBlock() instanceof MetaMachineBlock)) continue;
            if (isValidModuleForHost(((BlockItem) item).getBlock(), hostType)) {
                ItemStack resultItem = stack.copy();
                resultItem.setCount(1);
                stack.shrink(1);
                if (stack.isEmpty()) {
                    exhaustedSlots.add(i);
                }
                return new ModuleItemResult(resultItem, false, i);
            }
        }

        if (setting.boundAE != null) {
            IGridNode gridNode = AdvancedBlockPattern.getGridNode(
                    player.level().getBlockEntity(setting.boundAE.pos()));
            IGrid grid = gridNode != null ? gridNode.getGrid() : null;
            appeng.api.storage.MEStorage aeInventory = grid != null ? grid.getStorageService().getInventory() : null;
            IActionSource source = IActionSource.ofPlayer(player);
            if (aeInventory != null) {

                for (ItemStack candidate : getModuleBlocksForHost(hostType)) {
                    AEItemKey aeKey = AEItemKey.of(candidate);
                    if (aeKey != null && aeInventory.extract(aeKey, 1, Actionable.MODULATE, source) > 0) {
                        return new ModuleItemResult(candidate, true, -1);
                    }
                }

                KeyCounter availableStacks = aeInventory.getAvailableStacks();
                for (AEKey key : availableStacks.keySet()) {
                    if (key instanceof AEItemKey) {
                        AEItemKey aeItemKey = (AEItemKey) key;
                        Item item = aeItemKey.getItem();
                        if (item instanceof BlockItem && ((BlockItem) item).getBlock() instanceof MetaMachineBlock) {
                            if (isValidModuleForHost(((BlockItem) item).getBlock(), hostType)) {
                                if (aeInventory.extract(key, 1, Actionable.MODULATE, source) > 0) {
                                    return new ModuleItemResult(aeItemKey.toStack(), true, -1);
                                }
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    private void returnItemToAE(Player player, AutoBuildSetting setting, ItemStack itemStack) {
        insertItemToAE(player, setting, itemStack);
    }

    private boolean insertItemToAE(Player player, AutoBuildSetting setting, ItemStack itemStack) {
        if (setting.boundAE == null) return false;
        IGridNode gridNode = AdvancedBlockPattern.getGridNode(
                player.level().getBlockEntity(setting.boundAE.pos()));
        IGrid grid = gridNode != null ? gridNode.getGrid() : null;
        if (grid == null) return false;
        appeng.api.storage.MEStorage aeInventory = grid.getStorageService().getInventory();
        IActionSource source = IActionSource.ofPlayer(player);
        AEItemKey aeKey = AEItemKey.of(itemStack);
        aeInventory.insert(aeKey, (long) itemStack.getCount(), Actionable.MODULATE, source);
        return true;
    }

    private boolean isValidModuleForHost(Block block, Class<?> hostType) {
        if (!(block instanceof MetaMachineBlock)) return false;
        ModuleCacheKey cacheKey = new ModuleCacheKey(block, hostType);
        Boolean cached = moduleValidCache.get(cacheKey);
        if (cached != null) return cached;

        boolean result = checkIsValidModule(block, hostType);
        moduleValidCache.put(cacheKey, result);
        return result;
    }

    private boolean checkIsValidModule(Block block, Class<?> hostType) {
        MachineDefinition definition = null;
        for (var d : GTRegistries.MACHINES) {
            if (d.getBlock() == block) {
                definition = d;
                break;
            }
        }
        if (definition == null) return false;
        try {
            net.minecraft.world.level.block.entity.BlockEntityType<?> bet = definition.getBlockEntityType();
            if (bet == null) return false;
            net.minecraft.world.level.block.entity.BlockEntity be = bet.create(BlockPos.ZERO, block.defaultBlockState());
            if (be == null) return false;
            if (be instanceof IMachineBlockEntity) {
                MetaMachine machine = ((IMachineBlockEntity) be).getMetaMachine();
                if (machine instanceof IModularMachineModule) {
                    Class<?> moduleHostType = ((IModularMachineModule<?, ?>) machine).getHostType();
                    return moduleHostType.isAssignableFrom(hostType) || hostType.isAssignableFrom(moduleHostType);
                }
            }
        } catch (Exception e) {}
        return false;
    }

    private List<ItemStack> getModuleBlocksForHost(Class<?> hostType) {
        List<ItemStack> result = new ArrayList<>();
        for (var d : GTRegistries.MACHINES) {
            if (d.getRecipeTypes() != null || d instanceof MultiblockMachineDefinition) continue;
            Block block = d.getBlock();
            if (isValidModuleForHost(block, hostType)) {
                result.add(block.asItem().getDefaultInstance());
            }
        }
        return result;
    }

    private IGrid getBoundAEGrid(Player player, AutoBuildSetting setting) {
        if (setting.boundAE == null) return null;
        IGridNode gridNode = AdvancedBlockPattern.getGridNode(
                player.level().getBlockEntity(setting.boundAE.pos()));
        return gridNode != null ? gridNode.getGrid() : null;
    }

    private void handleMissingItemsAECrafting(List<ItemStack> missingItems, Player player, AutoBuildSetting setting) {
        if (AE_CRAFTING_GUARD.get()) return;
        AE_CRAFTING_GUARD.set(true);
        try {
            handleMissingItemsAECraftingInner(missingItems, player, setting);
        } finally {
            AE_CRAFTING_GUARD.set(false);
        }
    }

    private void handleMissingItemsAECraftingInner(List<ItemStack> missingItems, Player player, AutoBuildSetting setting) {
        Level level = player.level();
        if (!(level instanceof ServerLevel)) return;

        GlobalPos boundAE = setting.boundAE;
        if (boundAE == null) {
            player.sendSystemMessage(
                    Component.translatable("gtlsupb.terminal.no_ae_network")
                            .withStyle(ChatFormatting.RED));
            return;
        }

        IGridNode gridNode = AdvancedBlockPattern.getGridNode(level.getBlockEntity(boundAE.pos()));
        IGrid grid = gridNode != null ? gridNode.getGrid() : null;
        if (grid == null) {
            player.sendSystemMessage(
                    Component.translatable("gtlsupb.terminal.no_ae_network")
                            .withStyle(ChatFormatting.RED));
            return;
        }

        ICraftingService craftingService = grid.getCraftingService();
        appeng.api.storage.MEStorage aeInventory = grid.getStorageService().getInventory();
        IActionSource source = IActionSource.ofPlayer(player);

        LinkedHashMap<AEItemKey, Long> itemCounts = new LinkedHashMap<>();
        for (ItemStack item : missingItems) {
            AEItemKey aeKey = AEItemKey.of(item);
            if (aeKey != null) {
                itemCounts.merge(aeKey, 1L, Long::sum);
            }
        }

        List<Map.Entry<AEItemKey, Long>> storedItems = new ArrayList<>();
        List<Map.Entry<AEItemKey, Long>> craftableItems = new ArrayList<>();
        List<Map.Entry<AEItemKey, Long>> noPatternItems = new ArrayList<>();

        for (Map.Entry<AEItemKey, Long> entry : itemCounts.entrySet()) {
            AEItemKey aeKey = entry.getKey();
            long count = entry.getValue();

            long stored = aeInventory.extract(aeKey, count, Actionable.SIMULATE, source);
            if (stored >= count) {

                aeInventory.extract(aeKey, count, Actionable.MODULATE, source);
                storedItems.add(Map.entry(aeKey, count));
            } else if (stored > 0) {

                aeInventory.extract(aeKey, stored, Actionable.MODULATE, source);
                storedItems.add(Map.entry(aeKey, stored));
                long remaining = count - stored;
                Set<AEKey> craftables = craftingService.getCraftables(aeKey::equals);
                if (!craftables.isEmpty()) {
                    craftableItems.add(Map.entry(aeKey, remaining));
                } else {
                    noPatternItems.add(Map.entry(aeKey, remaining));
                }
            } else {

                Set<AEKey> craftables = craftingService.getCraftables(aeKey::equals);
                if (!craftables.isEmpty()) {
                    craftableItems.add(Map.entry(aeKey, count));
                } else {
                    noPatternItems.add(Map.entry(aeKey, count));
                }
            }
        }

        if (!storedItems.isEmpty()) {
            player.sendSystemMessage(
                    Component.translatable("gtlsupb.terminal.ae_in_storage", storedItems.size())
                            .withStyle(ChatFormatting.GREEN));
            for (Map.Entry<AEItemKey, Long> entry : storedItems) {
                MutableComponent itemComp = Component.empty().append(entry.getKey().toStack().getDisplayName());
                if (entry.getValue() > 1) itemComp.append(Component.literal(" \u00d7" + entry.getValue()));
                player.sendSystemMessage(
                        Component.literal("  ").append(itemComp).withStyle(ChatFormatting.GREEN));
            }
        }

        if (!craftableItems.isEmpty() && player instanceof ServerPlayer) {
            IActionHost actionHost = findActionHostFromGrid(grid);
            IActionSource actionSource = actionHost != null ? new PlayerSource((ServerPlayer) player, actionHost) : IActionSource.ofPlayer(player);

            ICraftingSimulationRequester simRequester = () -> actionSource;

            int autoCrafted = 0;
            int missingMaterials = 0;

            for (Map.Entry<AEItemKey, Long> entry : craftableItems) {
                AEItemKey aeKey = entry.getKey();
                long count = entry.getValue();
                try {

                    var future = craftingService.beginCraftingCalculation(
                            level, simRequester, aeKey, count, CalculationStrategy.REPORT_MISSING_ITEMS);
                    ICraftingPlan plan = future.get(10, TimeUnit.SECONDS);

                    KeyCounter missing = plan.missingItems();
                    boolean recursive = false;
                    for (var missingEntry : missing) {
                        if (itemCounts.containsKey(missingEntry.getKey())) {
                            recursive = true;
                            break;
                        }
                    }
                    if (recursive) {
                        MutableComponent itemComp = Component.empty().append(aeKey.toStack().getDisplayName());
                        if (count > 1) itemComp.append(Component.literal(" \u00d7" + count));
                        player.sendSystemMessage(
                                Component.translatable("gtlsupb.terminal.ae_recursive_plan", itemComp)
                                        .withStyle(ChatFormatting.RED));
                        missingMaterials++;
                        continue;
                    }
                    if (!missing.isEmpty()) {
                        missingMaterials++;
                        MutableComponent itemComp = Component.empty().append(aeKey.toStack().getDisplayName());
                        if (count > 1) itemComp.append(Component.literal(" \u00d7" + count));
                        player.sendSystemMessage(
                                Component.translatable("gtlsupb.terminal.ae_missing_materials", itemComp)
                                        .withStyle(ChatFormatting.RED));
                        for (var missingEntry : missing) {
                            AEKey missingKey = missingEntry.getKey();
                            long missingCount = missingEntry.getValue();
                            if (missingKey instanceof AEItemKey) {
                                player.sendSystemMessage(
                                        Component.literal("  \u00a7c\u2716 \u00a77")
                                                .append(((AEItemKey) missingKey).toStack().getDisplayName().copy())
                                                .append(" \u00a7c\u00d7" + missingCount));
                            } else if (missingKey instanceof AEFluidKey) {
                                player.sendSystemMessage(
                                        Component.literal("  \u00a7c\u2716 \u00a77")
                                                .append(((AEFluidKey) missingKey).toStack((int) missingCount).getDisplayName().copy())
                                                .append(" \u00a7c\u00d7" + missingCount));
                            } else {
                                player.sendSystemMessage(
                                        Component.literal("  \u00a7c\u2716 \u00a77" + missingKey + " \u00a7c\u00d7" + missingCount));
                            }
                        }
                        continue;
                    }

                    ICraftingSubmitResult result = craftingService.submitJob(plan, null, null, false, actionSource);

                    if (result.successful()) {
                        autoCrafted++;
                        player.sendSystemMessage(
                                Component.translatable("gtlsupb.terminal.ae_auto_crafted",
                                        aeKey.toStack().getDisplayName(), count)
                                        .withStyle(ChatFormatting.GREEN));
                    } else {

                        Component errorMsg;
                        if (result.errorCode() == CraftingSubmitErrorCode.NO_CPU_FOUND) {
                            errorMsg = Component.translatable("gtlsupb.terminal.ae_error.no_cpu_found");
                        } else if (result.errorCode() == CraftingSubmitErrorCode.NO_SUITABLE_CPU_FOUND) {
                            errorMsg = Component.translatable("gtlsupb.terminal.ae_error.no_suitable_cpu");
                        } else if (result.errorCode() == CraftingSubmitErrorCode.CPU_BUSY) {
                            errorMsg = Component.translatable("gtlsupb.terminal.ae_error.cpu_busy");
                        } else if (result.errorCode() == CraftingSubmitErrorCode.CPU_OFFLINE) {
                            errorMsg = Component.translatable("gtlsupb.terminal.ae_error.cpu_offline");
                        } else if (result.errorCode() == CraftingSubmitErrorCode.CPU_TOO_SMALL) {
                            errorMsg = Component.translatable("gtlsupb.terminal.ae_error.cpu_too_small");
                        } else if (result.errorCode() == CraftingSubmitErrorCode.MISSING_INGREDIENT) {
                            Object detail = result.errorDetail();
                            if (detail instanceof GenericStack) {
                                AEKey detailKey = ((GenericStack) detail).what();
                                long detailCount = ((GenericStack) detail).amount();
                                errorMsg = Component.translatable("gtlsupb.terminal.ae_error.missing_ingredient",
                                        detailKey instanceof AEItemKey ? ((AEItemKey) detailKey).toStack().getDisplayName() : Component.literal(detailKey.toString()),
                                        detailCount);
                            } else {
                                errorMsg = Component.translatable("gtlsupb.terminal.ae_error.missing_ingredient_simple");
                            }
                        } else if (result.errorCode() == CraftingSubmitErrorCode.INCOMPLETE_PLAN) {
                            errorMsg = Component.translatable("gtlsupb.terminal.ae_error.incomplete_plan");
                        } else {
                            errorMsg = Component.translatable("gtlsupb.terminal.ae_error.unknown", result.errorCode());
                        }
                        player.sendSystemMessage(
                                Component.translatable("gtlsupb.terminal.ae_craft_failed",
                                        aeKey.toStack().getDisplayName(), errorMsg)
                                        .withStyle(ChatFormatting.RED));
                    }
                } catch (java.util.concurrent.TimeoutException e) {
                    player.sendSystemMessage(
                            Component.translatable("gtlsupb.terminal.ae_craft_timeout",
                                    aeKey.toStack().getDisplayName())
                                    .withStyle(ChatFormatting.YELLOW));
                } catch (Exception e) {
                    player.sendSystemMessage(
                            Component.translatable("gtlsupb.terminal.ae_craft_error",
                                    aeKey.toStack().getDisplayName(), e.getMessage() != null ? e.getMessage() : "unknown")
                                    .withStyle(ChatFormatting.RED));
                }
            }

            if (autoCrafted > 0) {
                player.sendSystemMessage(
                        Component.translatable("gtlsupb.terminal.ae_auto_crafted_summary", autoCrafted)
                                .withStyle(ChatFormatting.GREEN));
            }
            if (missingMaterials > 0) {
                player.sendSystemMessage(
                        Component.translatable("gtlsupb.terminal.ae_missing_summary", missingMaterials)
                                .withStyle(ChatFormatting.RED));
            }
        }

        if (!noPatternItems.isEmpty()) {
            player.sendSystemMessage(
                    Component.translatable("gtlsupb.terminal.ae_no_pattern", noPatternItems.size())
                            .withStyle(ChatFormatting.RED));
            for (Map.Entry<AEItemKey, Long> entry : noPatternItems) {
                MutableComponent itemComp = Component.empty().append(entry.getKey().toStack().getDisplayName());
                if (entry.getValue() > 1) itemComp.append(Component.literal(" \u00d7" + entry.getValue()));
                player.sendSystemMessage(
                        Component.literal("  ").append(itemComp).withStyle(ChatFormatting.RED));
            }
        }
    }

    private IActionHost findActionHostFromGrid(IGrid grid) {
        for (IGridNode node : grid.getNodes()) {
            Object owner = node.getOwner();
            if (owner instanceof IActionHost) return (IActionHost) owner;
        }
        IGridNode anyNode = grid.getNodes().iterator().next();
        return () -> anyNode;
    }

    private AutoBuildSetting getAutoBuildSetting(ItemStack mainHandItem) {
        AutoBuildSetting autoBuildSetting = new AutoBuildSetting();
        CompoundTag tag = mainHandItem.getOrCreateTag();
        if (!tag.isEmpty()) {
            autoBuildSetting.tier = tag.getInt("Tier");
            autoBuildSetting.repeatCount = tag.getInt("RepeatCount");
            autoBuildSetting.isNoHatchMode = tag.getBoolean("NoHatchMode");
            autoBuildSetting.isReplaceMode = tag.getBoolean("ReplaceMode");
            autoBuildSetting.isFlipped = tag.getBoolean("IsFlipped");
            autoBuildSetting.isDismantleMode = tag.getBoolean("DismantleMode");
            autoBuildSetting.isAeMode = tag.getBoolean("AEMode");
            autoBuildSetting.isModuleMode = tag.getBoolean("ModuleMode");
            autoBuildSetting.isDetectMode = tag.getBoolean("DetectMode");
            autoBuildSetting.isModuleFormMode = tag.getBoolean("ModuleFormMode");
            if (tag.contains("BoundAE")) {
                CompoundTag aePos = tag.getCompound("BoundAE");
                int x = aePos.getInt("X");
                int y = aePos.getInt("Y");
                int z = aePos.getInt("Z");
                ResourceKey<net.minecraft.world.level.Level> dimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(aePos.getString("dim")));
                autoBuildSetting.boundAE = GlobalPos.of(dimension, new BlockPos(x, y, z));
            }
            String block = tag.getString("blocks");
            if (!block.isEmpty()) {
                var lazy = BlockMap.tierBlockMap.get(block);
                autoBuildSetting.tierBlock = lazy != null ? lazy.get() : null;
                autoBuildSetting.blocks = autoBuildSetting.tierBlock != null ? new ObjectOpenHashSet<>(Arrays.asList(autoBuildSetting.tierBlock)) : Collections.emptySet();
            }
        }
        return autoBuildSetting;
    }

    @Override
    public ModularUI createUI(HeldItemUIFactory.HeldItemHolder heldItemHolder, Player player) {
        return new ModularUI(200, 220, heldItemHolder, player).widget(createWidget(player));
    }

    private Widget createWidget(Player player) {
        ItemStack handItem = player.getMainHandItem();
        WidgetGroup group = new WidgetGroup(0, 0, 200, 216);
        DraggableScrollableWidgetGroup contain = new DraggableScrollableWidgetGroup(4, 4, 192, 156)
                .setBackground(GuiTextures.DISPLAY)
                .setYScrollBarWidth(2)
                .setYBarStyle(null, ColorPattern.T_WHITE.rectTexture().setRadius(1.0F));

        contain.addWidget(new ExtendLabelWidget(65, 8, Component.translatable("gui.gtlsupb.super_terminal_settings")))
                .addWidget(new ExtendLabelWidget(50, 26, Component.translatable("gui.gtlsupb.tier_blocks")))
                .addWidget(new ExtendLabelWidget(14, 46, Component.translatable("gui.gtlsupb.repeat_count")))
                .addWidget(new ExtendLabelWidget(14, 66, Component.translatable("gui.gtlsupb.no_hatch_mode"))
                        .setHoverTooltips(Component.translatable("tooltip.gtlsupb.no_hatch_mode")))
                .addWidget(new ExtendLabelWidget(14, 86, Component.translatable("gui.gtlsupb.replace_mode"))
                        .setHoverTooltips(Component.translatable("tooltip.gtlsupb.replace_mode")))
                .addWidget(new ExtendLabelWidget(14, 106, Component.translatable("gui.gtlsupb.mirror_mode"))
                        .setHoverTooltips(Component.translatable("tooltip.gtlsupb.mirror_mode")))
                .addWidget(new TerminalInputWidget(140, 45, 36, 12,
                        () -> getRepeatCount(handItem), v -> setRepeatCount(v, handItem)).setMax(648).setMin(0))
                .addWidget(new SwitchWidget(140, 63, 36, 14, (widget, b) -> setIsBuildHatches(!getIsBuildHatches(handItem), handItem))
                        .setPressed(getIsBuildHatches(handItem))
                        .setTexture(
                                new GuiTextureGroup(GuiTextures.BUTTON, new TextTexture(Component.translatable("gtlsupb.gui.toggle.off").getString())),
                                new GuiTextureGroup(GuiTextures.BUTTON, new TextTexture(Component.translatable("gtlsupb.gui.toggle.on").getString()))))
                .addWidget(new SwitchWidget(140, 83, 36, 14, (widget, b) -> setReplaceMode(!getReplaceMode(handItem), handItem))
                        .setPressed(getReplaceMode(handItem))
                        .setTexture(
                                new GuiTextureGroup(GuiTextures.BUTTON, new TextTexture(Component.translatable("gtlsupb.gui.toggle.off").getString())),
                                new GuiTextureGroup(GuiTextures.BUTTON, new TextTexture(Component.translatable("gtlsupb.gui.toggle.on").getString()))))
                .addWidget(new SwitchWidget(140, 103, 36, 14, (widget, b) -> setIsFlip(!getIsFlip(handItem), handItem))
                        .setPressed(getIsFlip(handItem))
                        .setTexture(
                                new GuiTextureGroup(GuiTextures.BUTTON, new TextTexture(Component.translatable("gtlsupb.gui.toggle.off").getString())),
                                new GuiTextureGroup(GuiTextures.BUTTON, new TextTexture(Component.translatable("gtlsupb.gui.toggle.on").getString()))))

                .addWidget(new ExtendLabelWidget(14, 126, Component.translatable("gui.gtlsupb.ae_mode")))
                .addWidget(new SwitchWidget(140, 123, 36, 14, (widget, b) -> setAEMode(!getAEMode(handItem), handItem))
                        .setPressed(getAEMode(handItem))
                        .setTexture(
                                new GuiTextureGroup(GuiTextures.BUTTON, new TextTexture(Component.translatable("gtlsupb.gui.toggle.off").getString())),
                                new GuiTextureGroup(GuiTextures.BUTTON, new TextTexture(Component.translatable("gtlsupb.gui.toggle.on").getString()))))

                .addWidget(new ExtendLabelWidget(14, 146, Component.translatable("gui.gtlsupb.dismantle_mode")))
                .addWidget(new SwitchWidget(140, 143, 36, 14, (widget, b) -> setDismantleMode(!getDismantleMode(handItem), handItem))
                        .setPressed(getDismantleMode(handItem))
                        .setTexture(
                                new GuiTextureGroup(GuiTextures.BUTTON, new TextTexture(Component.translatable("gtlsupb.gui.toggle.off").getString())),
                                new GuiTextureGroup(GuiTextures.BUTTON, new TextTexture(Component.translatable("gtlsupb.gui.toggle.on").getString()))))

                .addWidget(new ExtendLabelWidget(14, 166, Component.translatable("gui.gtlsupb.module_mode"))
                        .setHoverTooltips(Component.translatable("tooltip.gtlsupb.module_mode")))
                .addWidget(new SwitchWidget(140, 163, 36, 14, (widget, b) -> setModuleMode(!getModuleMode(handItem), handItem))
                        .setPressed(getModuleMode(handItem))
                        .setTexture(
                                new GuiTextureGroup(GuiTextures.BUTTON, new TextTexture(Component.translatable("gtlsupb.gui.toggle.off").getString())),
                                new GuiTextureGroup(GuiTextures.BUTTON, new TextTexture(Component.translatable("gtlsupb.gui.toggle.on").getString()))))

                .addWidget(new ExtendLabelWidget(14, 186, Component.translatable("gui.gtlsupb.detect_mode"))
                        .setHoverTooltips(Component.translatable("tooltip.gtlsupb.detect_mode")))
                .addWidget(new SwitchWidget(140, 183, 36, 14, (widget, b) -> setDetectMode(!getDetectMode(handItem), handItem))
                        .setPressed(getDetectMode(handItem))
                        .setTexture(
                                new GuiTextureGroup(GuiTextures.BUTTON, new TextTexture(Component.translatable("gtlsupb.gui.toggle.off").getString())),
                                new GuiTextureGroup(GuiTextures.BUTTON, new TextTexture(Component.translatable("gtlsupb.gui.toggle.on").getString()))))

                .addWidget(new ExtendLabelWidget(14, 206, Component.translatable("gui.gtlsupb.module_form_mode"))
                        .setHoverTooltips(Component.translatable("tooltip.gtlsupb.module_form_mode")))
                .addWidget(new SwitchWidget(140, 203, 36, 14, (widget, b) -> setModuleFormMode(!getModuleFormMode(handItem), handItem))
                        .setPressed(getModuleFormMode(handItem))
                        .setTexture(
                                new GuiTextureGroup(GuiTextures.BUTTON, new TextTexture(Component.translatable("gtlsupb.gui.toggle.off").getString())),
                                new GuiTextureGroup(GuiTextures.BUTTON, new TextTexture(Component.translatable("gtlsupb.gui.toggle.on").getString()))));

        ExtendLabelWidget blockLabel = new ExtendLabelWidget(86, 26, getBlockComponent(handItem));
        BlockMapSelectorWidget blockMap = new BlockMapSelectorWidget(160 + 4, contain.getSizeWidth(), (s, i) -> {
            CompoundTag tag = handItem.getOrCreateTag();
            tag.putString("blocks", s);
            tag.putInt("Tier", i);
            handItem.setTag(tag);
            var lazy = BlockMap.tierBlockMap.get(s);
            Component tierComponent;
            if (lazy != null) {
                Block[] blocks = lazy.get();
                if (blocks != null && i >= 0 && i < blocks.length) {
                    tierComponent = Component.translatable(blocks[i].asItem().getDescriptionId());
                } else {
                    tierComponent = Component.literal("?");
                }
            } else {
                tierComponent = Component.literal("?");
            }
            blockLabel.setComponent(
                    Component.literal("(").append(BlockMapSelectorWidget.getBlock(s))
                            .append(Component.literal(" : "))
                            .append(tierComponent)
                            .append(Component.literal(")")));
        });
        blockMap.setInit(handItem);
        SwitchWidget open = new SwitchWidget(14, 26, 30, 16, (widget, f) -> blockMap.showType(f));
        open.setHoverTooltips(Component.translatable("gui.gtlsupb.open.config.map"));
        contain.addWidget(open).addWidget(blockLabel);
        group.addWidget(contain).addWidget(blockMap).setBackground(GuiTextures.BACKGROUND_INVERSE);
        return group;
    }

    private Component getBlockComponent(ItemStack itemStack) {
        CompoundTag tag = itemStack.getOrCreateTag();
        if (!tag.isEmpty()) {
            String block = tag.getString("blocks");
            if (!block.isEmpty()) {
                int tier = tag.getInt("Tier");
                var lazy = BlockMap.tierBlockMap.get(block);
                Component tierComponent;
                if (lazy != null) {
                    Block[] blocks = lazy.get();
                    if (blocks != null && tier >= 0 && tier < blocks.length) {
                        tierComponent = Component.translatable(blocks[tier].asItem().getDescriptionId());
                    } else {
                        tierComponent = Component.literal("?");
                    }
                } else {
                    tierComponent = Component.literal("?");
                }
                return Component.literal("(").append(BlockMapSelectorWidget.getBlock(block))
                        .append(Component.literal(" : "))
                        .append(tierComponent)
                        .append(Component.literal(")"));
            }
        }
        return Component.literal("");
    }

    private int getRepeatCount(ItemStack itemStack) {
        CompoundTag tag = itemStack.getOrCreateTag();
        return !tag.isEmpty() ? tag.getInt("RepeatCount") : 0;
    }

    private boolean getIsFlip(ItemStack itemStack) {
        CompoundTag tag = itemStack.getOrCreateTag();
        return !tag.isEmpty() && tag.getBoolean("IsFlipped");
    }

    private boolean getDismantleMode(ItemStack itemStack) {
        CompoundTag tag = itemStack.getOrCreateTag();
        return !tag.isEmpty() && tag.getBoolean("DismantleMode");
    }

    private void setDismantleMode(boolean isDismantle, ItemStack itemStack) {
        CompoundTag tag = itemStack.getOrCreateTag();
        tag.putBoolean("DismantleMode", isDismantle);
        itemStack.setTag(tag);
    }

    private boolean getAEMode(ItemStack itemStack) {
        CompoundTag tag = itemStack.getOrCreateTag();
        return !tag.isEmpty() && tag.getBoolean("AEMode");
    }

    private void setAEMode(boolean isAEMode, ItemStack itemStack) {
        CompoundTag tag = itemStack.getOrCreateTag();
        tag.putBoolean("AEMode", isAEMode);
        itemStack.setTag(tag);
    }

    private boolean getModuleMode(ItemStack itemStack) {
        CompoundTag tag = itemStack.getOrCreateTag();
        return !tag.isEmpty() && tag.getBoolean("ModuleMode");
    }

    private void setModuleMode(boolean isModuleMode, ItemStack itemStack) {
        CompoundTag tag = itemStack.getOrCreateTag();
        tag.putBoolean("ModuleMode", isModuleMode);
        itemStack.setTag(tag);
    }

    private boolean getDetectMode(ItemStack itemStack) {
        CompoundTag tag = itemStack.getOrCreateTag();
        return !tag.isEmpty() && tag.getBoolean("DetectMode");
    }

    private void setDetectMode(boolean isDetectMode, ItemStack itemStack) {
        CompoundTag tag = itemStack.getOrCreateTag();
        tag.putBoolean("DetectMode", isDetectMode);
        itemStack.setTag(tag);
    }

    private boolean getModuleFormMode(ItemStack itemStack) {
        CompoundTag tag = itemStack.getOrCreateTag();
        return !tag.isEmpty() && tag.getBoolean("ModuleFormMode");
    }

    private void setModuleFormMode(boolean isModuleFormMode, ItemStack itemStack) {
        CompoundTag tag = itemStack.getOrCreateTag();
        tag.putBoolean("ModuleFormMode", isModuleFormMode);
        itemStack.setTag(tag);
    }

    private void setRepeatCount(int repeatCount, ItemStack itemStack) {
        CompoundTag tag = itemStack.getOrCreateTag();
        tag.putInt("RepeatCount", repeatCount);
        itemStack.setTag(tag);
    }

    private boolean getIsBuildHatches(ItemStack itemStack) {
        CompoundTag tag = itemStack.getOrCreateTag();
        return tag.isEmpty() || tag.getBoolean("NoHatchMode");
    }

    private void setIsBuildHatches(boolean isBuildHatches, ItemStack itemStack) {
        CompoundTag tag = itemStack.getOrCreateTag();
        tag.putBoolean("NoHatchMode", isBuildHatches);
        itemStack.setTag(tag);
    }

    private boolean getReplaceMode(ItemStack itemStack) {
        CompoundTag tag = itemStack.getOrCreateTag();
        return !tag.isEmpty() && tag.getBoolean("ReplaceMode");
    }

    private void setReplaceMode(boolean isReplace, ItemStack itemStack) {
        CompoundTag tag = itemStack.getOrCreateTag();
        tag.putBoolean("ReplaceMode", isReplace);
        itemStack.setTag(tag);
    }

    private void setIsFlip(boolean isFlip, ItemStack itemStack) {
        CompoundTag tag = itemStack.getOrCreateTag();
        tag.putBoolean("IsFlipped", isFlip);
        itemStack.setTag(tag);
    }

    private void dismantleMultiblock(IMultiController controller, Player player) {
        Level level = player.level();
        if (level.isClientSide()) return;
        BlockPattern pattern = controller.getPattern();
        if (pattern == null) return;
        AdvancedBlockPattern advancedPattern = AdvancedBlockPattern.getAdvancedBlockPattern(pattern);
        if (advancedPattern == null) return;
        ItemStack handItem = player.getMainHandItem();
        AutoBuildSetting setting = getAutoBuildSetting(handItem);

        ItemStack cell = ItemStack.EMPTY;
        List<ItemStack> inventoryBefore = null;
        try {
            cell = findSuperTerminalCell(player);

            if (cell.isEmpty() && !setting.isAeMode) {
                var entry = com.gtlsupb.gtlsupb.common.data.GTLSupbItems.SUPER_TERMINAL_CELL;
                if (entry != null) {
                    cell = new ItemStack(entry.get());
                    player.getInventory().add(cell);
                    player.displayClientMessage(
                            Component.translatable("item.gtlsupb.super_terminal_cell")
                                    .append(Component.literal(" ×1")),
                            true);
                }
            }

            if (!cell.isEmpty() && setting.isAeMode) {
                SuperTerminalCellItem.setCollectEnabled(cell, false);
            }
            if (!cell.isEmpty() && !setting.isAeMode) {
                inventoryBefore = snapshotInventory(player);
            }
        } catch (Throwable ignored) {

        }

        advancedPattern.dismantleMultiblock(
                controller, player, setting.repeatCount, setting.isFlipped, setting.isAeMode, setting.boundAE);

        if (inventoryBefore != null && !cell.isEmpty() && !setting.isAeMode) {
            moveInventoryDeltaInto(inventoryBefore, player, cell);
        }

        if (!cell.isEmpty() && !setting.isAeMode && SuperTerminalCellItem.isCollectEnabled(cell) && level instanceof ServerLevel serverLevel) {
            final ItemStack collectTarget = cell;
            final BlockPos center = controller.self().getPos();
            serverLevel.getServer().tell(new TickTask(
                    serverLevel.getServer().getTickCount() + 2,
                    () -> collectDropsInto(collectTarget, serverLevel, center, player)));
        }
    }

    private static void forcePatternRecheck(Object controllerLike) {
        if (controllerLike == null) return;
        for (String name : new String[] { "checkPatternWithLock", "checkPattern" }) {
            try {
                var method = controllerLike.getClass().getMethod(name);
                method.invoke(controllerLike);
                return;
            } catch (Throwable ignored) {

            }
        }
    }

    private static void simulateWrenchRightClick(ServerLevel level, Player player, BlockPos pos) {
        var fakePlayer = net.minecraftforge.common.util.FakePlayerFactory.getMinecraft(level);
        fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, wrenchStack());
        Direction side = Direction.UP;
        var hit = new BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(pos), side, pos, false);
        level.getBlockState(pos).use(level, fakePlayer, InteractionHand.MAIN_HAND, hit);
        fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
    }

    private static ItemStack wrenchStack() {
        try {
            Class<?> gtItems = Class.forName("com.gregtechceu.gtceu.common.data.GTItems");
            Object entry = gtItems.getField("WRENCH").get(null);
            Object item = entry.getClass().getMethod("get").invoke(entry);
            if (item instanceof net.minecraft.world.item.Item wrench) {
                return new ItemStack(wrench);
            }
        } catch (Throwable ignored) {

        }
        return ItemStack.EMPTY;
    }

    private static void formSingleModule(Level level, Player player, IMultiController controller,
                                         AutoBuildSetting setting, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        try {
            AdvancedBlockPattern advPattern = AdvancedBlockPattern.getAdvancedBlockPattern(controller.getPattern());
            if (advPattern != null) {
                advPattern.autoBuild(player, controller.getMultiblockState(), setting);
            }
        } catch (Throwable ignored) {

        }
        try {
            if (!controller.isFormed()) {
                simulateWrenchRightClick(serverLevel, player, pos);
            }
        } catch (Throwable ignored) {

        }
    }

    private static List<ItemStack> snapshotInventory(Player player) {
        List<ItemStack> out = new ArrayList<>();
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty()) out.add(stack.copy());
        }
        return out;
    }

    private static void moveInventoryDeltaInto(List<ItemStack> before, Player player, ItemStack cell) {
        if (cell.isEmpty()) return;

        List<ItemStack> allowance = new ArrayList<>();
        for (ItemStack snapshot : before) {
            allowance.add(snapshot.copy());
        }
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;
            int newCount = stack.getCount();
            for (ItemStack old : allowance) {
                if (newCount <= 0) break;
                if (old.isEmpty() || old.getCount() <= 0) continue;
                if (!ItemStack.isSameItemSameTags(old, stack)) continue;
                int consume = Math.min(old.getCount(), newCount);
                old.shrink(consume);
                newCount -= consume;
            }
            if (newCount <= 0) continue;
            ItemStack move = stack.copyWithCount(newCount);
            List<ItemStack> leftover = SuperTerminalCellItem.insertItems(cell, List.of(move));
            int notMoved = leftover.isEmpty() ? 0 : leftover.get(0).getCount();
            int moved = newCount - notMoved;
            if (moved > 0) stack.shrink(moved);
        }
    }

    public static ItemStack findSuperTerminalCell(Player player) {
        ItemStack offhand = player.getOffhandItem();
        if (offhand.getItem() instanceof SuperTerminalCellItem) return offhand;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof SuperTerminalCellItem) return stack;
        }
        return ItemStack.EMPTY;
    }

    private static void collectDropsInto(ItemStack cell, ServerLevel level, BlockPos center, Player player) {
        if (cell.isEmpty() || !(cell.getItem() instanceof SuperTerminalCellItem)) return;
        double radius = 12.0D;
        for (ItemEntity entity : level.getEntitiesOfClass(ItemEntity.class, new AABB(center).inflate(radius))) {
            ItemStack stack = entity.getItem();
            if (stack.isEmpty()) continue;
            List<ItemStack> leftover = SuperTerminalCellItem.insertItems(cell, List.of(stack.copy()));
            if (leftover.isEmpty()) entity.discard();
        }
    }

    private void checkStructure(IMultiController controller, Player player, AutoBuildSetting setting) {
        Level level = player.level();
        if (level.isClientSide()) return;

        if (controller.isFormed()) {
            player.sendSystemMessage(
                    Component.translatable("gtlsupb.terminal.detect.formed")
                            .withStyle(ChatFormatting.GREEN));
            return;
        }

        BlockPattern pattern = controller.getPattern();
        if (pattern == null) return;
        var self = controller.self();
        BlockPos centerPos = self.getPos();
        Direction frontFacing = self.getFrontFacing();

        Direction[] facings;
        if (controller.hasFrontFacing()) {
            facings = new Direction[] { frontFacing };
        } else {
            facings = new Direction[] { Direction.SOUTH, Direction.NORTH, Direction.EAST, Direction.WEST };
        }

        Block block = self.getBlockState().getBlock();
        if (block instanceof MetaMachineBlock && ((MetaMachineBlock) block).getRotationState() == RotationState.NONE) {
            facings = new Direction[] { frontFacing };
        }

        Direction upwardsFacing = self.getUpwardsFacing();
        boolean isFlipped = setting.isFlipped;
        MultiblockState worldState = new MultiblockState(self.getLevel(), self.getPos());

        List<PatternError> errors = new ArrayList<>();
        for (Direction direction : facings) {
            pattern.checkPatternAt(worldState, centerPos, direction, upwardsFacing, isFlipped, false);
            if (worldState.hasError()) {
                errors.add(worldState.error);
            }
        }

        if (errors.isEmpty()) {
            player.sendSystemMessage(
                    Component.translatable("gtlsupb.terminal.detect.no_error")
                            .withStyle(ChatFormatting.GREEN));
        } else {

            Set<BlockPos> errorPositions = new HashSet<>();
            for (PatternError error : errors) {
                showStructureError(player, error, isFlipped);
                errorPositions.add(error.getPos());
            }

            sendHighlightPackets(player, errorPositions, level);
        }
    }

    private void sendHighlightPackets(Player player, Set<BlockPos> positions, Level level) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        var dim = level.dimension();
        long expireTime = System.currentTimeMillis() + 15000;
        for (BlockPos pos : positions) {
            try {
                var packet = new org.gtlcore.gtlcore.network.packet.SStructureDetectHighlight(pos, dim, expireTime);
                org.gtlcore.gtlcore.network.GTLNetworkHandler.INSTANCE.sendTo(packet, serverPlayer);
            } catch (Exception e) {

            }
        }
    }

    @SuppressWarnings("deprecation")
    private void showStructureError(Player player, PatternError error, boolean flip) {
        if (error instanceof PatternStringError) {
            player.sendSystemMessage(((PatternStringError) error).getErrorInfo());
            return;
        }

        BlockPos pos = error.getPos();
        Component posComponent = Component.translatable(
                "gtlsupb.terminal.detect.error.2",
                pos.getX(), pos.getY(), pos.getZ(),
                flip ? Component.translatable("gtlsupb.terminal.detect.error.3").withStyle(ChatFormatting.GREEN) : Component.translatable("gtlsupb.terminal.detect.error.4").withStyle(ChatFormatting.YELLOW));

        List<List<ItemStack>> candidates = error.getCandidates();
        if (error instanceof SinglePredicateError) {
            player.sendSystemMessage(
                    Component.translatable("gtlsupb.terminal.detect.error.1", posComponent));
            if (!candidates.isEmpty() && !candidates.get(0).isEmpty()) {
                player.sendSystemMessage(
                        Component.literal(" - ").append(candidates.get(0).get(0).getHoverName()).append(error.getErrorInfo()));
            }
        } else {
            player.sendSystemMessage(
                    Component.translatable("gtlsupb.terminal.detect.error.0", posComponent));

            Set<net.minecraft.resources.ResourceLocation> shownItems = new HashSet<>();
            for (List<ItemStack> candidate : candidates) {
                for (ItemStack stack : candidate) {
                    if (stack.isEmpty()) continue;
                    net.minecraft.resources.ResourceLocation itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
                    if (shownItems.add(itemId)) {
                        MutableComponent prefix = Component.translatable("gtlsupb.terminal.detect.candidate")
                                .withStyle(ChatFormatting.GRAY);
                        player.sendSystemMessage(prefix.append(stack.getHoverName()));
                    }
                }
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Item item, Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (player instanceof ServerPlayer) {
            HeldItemUIFactory.INSTANCE.openUI((ServerPlayer) player, usedHand);
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }

    public static class AutoBuildSetting {

        public Block[] tierBlock = null;
        public Set<Block> blocks = Collections.emptySet();
        public int tier = 0;
        public int repeatCount = 0;
        public boolean isNoHatchMode = true;
        public boolean isReplaceMode = false;
        public boolean isFlipped = false;
        public boolean isDismantleMode = false;
        public boolean isAeMode = false;
        public boolean isModuleMode = false;
        public boolean isDetectMode = false;
        public boolean isModuleFormMode = false;
        public GlobalPos boundAE = null;

        public List<ItemStack> apply(BlockInfo[] blockInfos) {
            List<ItemStack> candidates = new ArrayList<>();
            if (blockInfos != null) {
                for (BlockInfo info : blockInfos) {
                    if (info == null) continue;
                    if (tierBlock != null && tier >= 0 && blockInfos.length > 1 &&
                            blocks.contains(info.getBlockState().getBlock())) {
                        candidates.add(tierBlock[Math.min(tier, blockInfos.length - 1)].asItem().getDefaultInstance());
                        return candidates;
                    }
                    Block block = info.getBlockState().getBlock();
                    if (block instanceof net.minecraft.world.level.block.LiquidBlock) {
                        candidates.add(((net.minecraft.world.level.block.LiquidBlock) block).getFluid().getBucket().getDefaultInstance());
                    } else if (block != Blocks.AIR) {
                        candidates.add(info.getItemStackForm());
                    }
                }
            }
            return candidates;
        }

        public boolean isPlaceHatch(BlockInfo[] blockInfos) {
            if (!isNoHatchMode) return true;
            if (blockInfos != null && blockInfos.length > 0) {
                BlockInfo blockInfo = blockInfos[0];
                if (blockInfo == null) return true;
                Block block = blockInfo.getBlockState().getBlock();
                if (block instanceof MetaMachineBlock) {
                    return !HatchSet.contains(block);
                }
            }
            return true;
        }

        private static class HatchSet {

            private static final ObjectOpenHashSet<Block> content = new ObjectOpenHashSet<>();

            static {
                for (var d : GTRegistries.MACHINES) {
                    if (d.getRecipeTypes() != null || d instanceof MultiblockMachineDefinition) continue;
                    Block block = d.getBlock();
                    net.minecraft.world.level.block.entity.BlockEntityType<?> bet = d.getBlockEntityType();
                    if (bet == null) continue;
                    try {
                        net.minecraft.world.level.block.entity.BlockEntity be = bet.create(BlockPos.ZERO, block.defaultBlockState());
                        if (be == null) continue;
                        if (be instanceof IMachineBlockEntity && ((IMachineBlockEntity) be).getMetaMachine() instanceof MultiblockPartMachine) {
                            content.add(block);
                        }
                    } catch (Exception e) {
                        continue;
                    }
                }
            }

            static boolean contains(Block block) {
                return content.contains(block);
            }
        }
    }
}
