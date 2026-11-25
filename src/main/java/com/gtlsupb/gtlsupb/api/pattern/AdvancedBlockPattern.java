package com.gtlsupb.gtlsupb.api.pattern;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockState;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.pattern.predicates.SimplePredicate;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;

import com.lowdragmc.lowdraglib.utils.BlockInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;

import appeng.api.config.Actionable;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import com.gtlsupb.gtlsupb.common.item.UltimateTerminalBehavior;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class AdvancedBlockPattern extends BlockPattern {

    private final TraceabilityPredicate[][][] blockMatches;
    private final int fingerLength;
    private final int thumbLength;
    private final int palmLength;
    private final int[] centerOffset;

    private static final Direction[] FACINGS = { Direction.SOUTH, Direction.NORTH, Direction.WEST, Direction.EAST, Direction.UP, Direction.DOWN };
    private static final Direction[] FACINGS_H = { Direction.SOUTH, Direction.NORTH, Direction.WEST, Direction.EAST };

    public AdvancedBlockPattern(TraceabilityPredicate[][][] predicatesIn, RelativeDirection[] structureDir, int[][] aisleRepetitions, int[] centerOffset) {
        super(predicatesIn, structureDir, aisleRepetitions, centerOffset);
        this.blockMatches = predicatesIn;
        this.fingerLength = predicatesIn.length;
        this.thumbLength = fingerLength > 0 ? predicatesIn[0].length : 0;
        this.palmLength = thumbLength > 0 ? predicatesIn[0][0].length : 0;
        this.centerOffset = centerOffset;
    }

    private static class FoundItemResult {

        final ItemStack first;
        final IItemHandler second;
        final int third;

        FoundItemResult(ItemStack first, IItemHandler second, int third) {
            this.first = first;
            this.second = second;
            this.third = third;
        }
    }

    private static Field F_BLOCK_MATCHES;
    private static Field F_STRUCTURE_DIR;
    private static Field F_AISLE_REPETITIONS;
    private static Field F_CENTER_OFFSET;
    private static boolean FIELDS_RESOLVED;

    private static void gtlsupb$resolveFields() {
        if (FIELDS_RESOLVED) return;
        FIELDS_RESOLVED = true;
        try {
            Field f = BlockPattern.class.getDeclaredField("blockMatches");
            f.setAccessible(true);
            F_BLOCK_MATCHES = f;
            f = BlockPattern.class.getDeclaredField("structureDir");
            f.setAccessible(true);
            F_STRUCTURE_DIR = f;
            f = BlockPattern.class.getDeclaredField("aisleRepetitions");
            f.setAccessible(true);
            F_AISLE_REPETITIONS = f;
            f = BlockPattern.class.getDeclaredField("centerOffset");
            f.setAccessible(true);
            F_CENTER_OFFSET = f;
        } catch (Throwable ignored) {}
    }

    @SuppressWarnings("unchecked")
    public static AdvancedBlockPattern getAdvancedBlockPattern(BlockPattern blockPattern) {
        try {
            gtlsupb$resolveFields();
            if (F_BLOCK_MATCHES == null || F_STRUCTURE_DIR == null || F_AISLE_REPETITIONS == null || F_CENTER_OFFSET == null) {
                return null;
            }
            TraceabilityPredicate[][][] blockMatches = (TraceabilityPredicate[][][]) F_BLOCK_MATCHES.get(blockPattern);
            RelativeDirection[] structureDir = (RelativeDirection[]) F_STRUCTURE_DIR.get(blockPattern);
            int[][] aisleRepetitions = (int[][]) F_AISLE_REPETITIONS.get(blockPattern);
            int[] centerOffset = (int[]) F_CENTER_OFFSET.get(blockPattern);
            return new AdvancedBlockPattern(blockMatches, structureDir, aisleRepetitions, centerOffset);
        } catch (Exception e) {
            return null;
        }
    }

    public static IGridNode getGridNode(BlockEntity be) {
        if (be == null) return null;
        try {
            Method m = be.getClass().getMethod("getGridNode", Direction.class);
            for (Direction direction : Direction.values()) {
                IGridNode node = (IGridNode) m.invoke(be, direction);
                if (node != null) return node;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private static Fluid getFluid(ItemStack stack) {
        if (stack.getItem() instanceof BlockItem) {
            BlockItem blockItem = (BlockItem) stack.getItem();
            if (blockItem.getBlock() instanceof LiquidBlock) {
                return ((LiquidBlock) blockItem.getBlock()).getFluid();
            }
        } else if (stack.getItem() instanceof BucketItem) {
            return ((BucketItem) stack.getItem()).getFluid();
        }
        return null;
    }

    public static final class AutoBuildResult {

        public final List<ItemStack> missingItems;
        public final List<BlockPos> placedPositions = new ArrayList<>();
        public final List<BlockPos> missingPositions = new ArrayList<>();

        AutoBuildResult(List<ItemStack> missingItems) {
            this.missingItems = missingItems;
        }
    }

    public AutoBuildResult autoBuild(Player player, MultiblockState worldState, UltimateTerminalBehavior.AutoBuildSetting autoBuildSetting) {
        Level world = player.level();
        int minZ = -centerOffset[4];
        clearWorldState(worldState);
        IMultiController controller = worldState.getController();
        BlockPos centerPos = controller.self().getPos();
        Direction facing = controller.self().getFrontFacing();
        Direction upwardsFacing = controller.self().getUpwardsFacing();
        boolean isFlipped = autoBuildSetting.isFlipped;
        boolean aeMode = autoBuildSetting.isAeMode;

        GlobalPos boundCoord = autoBuildSetting.boundAE;
        appeng.api.networking.IGrid grid = aeMode ? findBestGrid(world, boundCoord) : null;
        appeng.api.storage.MEStorage aeInventory = grid != null ? grid.getStorageService().getInventory() : null;
        IActionSource source = IActionSource.ofPlayer(player);

        Object2IntOpenHashMap<SimplePredicate> cacheGlobal = new Object2IntOpenHashMap<>(worldState.getGlobalCount());
        Object2IntOpenHashMap<SimplePredicate> cacheLayer = new Object2IntOpenHashMap<>(worldState.getLayerCount());
        Object2ObjectOpenHashMap<BlockPos, Object> blocks = new Object2ObjectOpenHashMap<>();
        ObjectOpenHashSet<BlockPos> placeBlockPos = new ObjectOpenHashSet<>();
        List<BlockPos> missingPositions = new ArrayList<>();
        List<ItemStack> missingItems = new ArrayList<>();
        Map<SimplePredicate, BlockInfo[]> candidateCache = new IdentityHashMap<>();
        Map<BlockInfo[], List<ItemStack>> appliedCache = new IdentityHashMap<>();
        Map<ItemStack, Fluid> fluidCache = new IdentityHashMap<>();
        IItemHandler playerHandler = player.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve().orElse(null);
        blocks.put(centerPos, controller);
        if (controller.isFormed() && autoBuildSetting.isReplaceMode) controller.onStructureInvalid();

        int[] repeat = new int[fingerLength];
        for (int h = 0; h < fingerLength; h++) {
            int minH = aisleRepetitions[h][0];
            int maxH = aisleRepetitions[h][1];
            repeat[h] = minH != maxH ? Math.max(minH, Math.min(maxH, autoBuildSetting.repeatCount)) : minH;
        }

        int z = minZ;
        for (int c = 0; c < fingerLength; c++) {
            for (int r = 0; r < repeat[c]; r++) {
                cacheLayer.clear();
                int y = -centerOffset[1];
                for (int b = 0; b < thumbLength; b++) {
                    int x = -centerOffset[0];
                    for (int a = 0; a < palmLength; a++) {
                        TraceabilityPredicate predicate = blockMatches[c][b][a];
                        if (predicate.isAny()) {
                            x++;
                            continue;
                        }
                        BlockPos pos = setActualRelativeOffset(x, y, z, facing, upwardsFacing, isFlipped)
                                .offset(centerPos.getX(), centerPos.getY(), centerPos.getZ());
                        updateWorldState(worldState, pos, predicate);

                        ItemStack itemStack = null;
                        BlockState existingState = world.getBlockState(pos);
                        if (!existingState.isAir()) {
                            Block block = existingState.getBlock();
                            if (autoBuildSetting.blocks.contains(block) && autoBuildSetting.isReplaceMode) {
                                itemStack = block.asItem().getDefaultInstance();
                            } else {
                                blocks.put(pos, existingState);
                                for (SimplePredicate limit : predicate.limited) limit.testLimited(worldState);
                                x++;
                                continue;
                            }
                        }

                        boolean find = false;
                        List<BlockInfo> infosList = new ArrayList<>();
                        for (SimplePredicate limit : predicate.limited) {
                            BlockInfo[] limitCandidates = gtlsupb$candidates(limit, candidateCache);
                            if (limitCandidates != null && !autoBuildSetting.isPlaceHatch(limitCandidates)) continue;
                            if (limit.minLayerCount > 0) {
                                int curr = cacheLayer.getInt(limit);
                                if (curr < limit.minLayerCount && (limit.maxLayerCount == -1 || curr < limit.maxLayerCount)) {
                                    cacheLayer.addTo(limit, 1);
                                    if (limitCandidates != null) infosList.addAll(Arrays.asList(limitCandidates));
                                    find = true;
                                    break;
                                }
                            }
                        }
                        if (!find) {
                            for (SimplePredicate limit : predicate.limited) {
                                BlockInfo[] limitCandidates = gtlsupb$candidates(limit, candidateCache);
                                if (limitCandidates != null && !autoBuildSetting.isPlaceHatch(limitCandidates)) continue;
                                if (limit.minCount > 0) {
                                    int curr = cacheGlobal.getInt(limit);
                                    if (curr < limit.minCount && (limit.maxCount == -1 || curr < limit.maxCount)) {
                                        cacheGlobal.addTo(limit, 1);
                                        if (limitCandidates != null) infosList.addAll(Arrays.asList(limitCandidates));
                                        find = true;
                                        break;
                                    }
                                }
                            }
                        }
                        if (!find) {
                            for (SimplePredicate limit : predicate.limited) {
                                BlockInfo[] limitCandidates = gtlsupb$candidates(limit, candidateCache);
                                if (limitCandidates != null && !autoBuildSetting.isPlaceHatch(limitCandidates)) continue;
                                if (limit.maxLayerCount != -1 && cacheLayer.getOrDefault(limit, Integer.MAX_VALUE) == limit.maxLayerCount) continue;
                                if (limit.maxCount != -1 && cacheGlobal.getOrDefault(limit, Integer.MAX_VALUE) == limit.maxCount) continue;
                                cacheLayer.addTo(limit, 1);
                                cacheGlobal.addTo(limit, 1);
                                if (limitCandidates != null) infosList.addAll(Arrays.asList(limitCandidates));
                            }
                            for (SimplePredicate common : predicate.common) {
                                BlockInfo[] commonCandidates = gtlsupb$candidates(common, candidateCache);
                                if (commonCandidates != null && predicate.common.size() > 1 && !autoBuildSetting.isPlaceHatch(commonCandidates)) continue;
                                if (commonCandidates != null) infosList.addAll(Arrays.asList(commonCandidates));
                            }
                        }

                        BlockInfo[] infos = infosList.toArray(new BlockInfo[infosList.size()]);
                        List<ItemStack> candidates = gtlsupb$appliedCandidates(autoBuildSetting, infos, appliedCache);

                        if (autoBuildSetting.isReplaceMode && itemStack != null) {
                            ItemStack finalItemStack = itemStack;
                            boolean hasMatch = false;
                            for (ItemStack cand : candidates) {
                                if (ItemStack.isSameItem(cand, finalItemStack)) {
                                    hasMatch = true;
                                    break;
                                }
                            }
                            if (hasMatch) {
                                x++;
                                continue;
                            }
                        }

                        IItemHandler holderHandler = null;
                        int holderSlot = -1;
                        boolean useAEForOldBlock = false;
                        if (autoBuildSetting.isReplaceMode && itemStack != null) {
                            if (aeInventory != null && autoBuildSetting.isAeMode) {

                                useAEForOldBlock = true;
                            } else {

                                IntObjectPair<IItemHandler> holderResult = foundHolderSlot(player, itemStack);
                                holderHandler = holderResult.second();
                                holderSlot = holderResult.leftInt();
                            }
                        }

                        ItemStack found = null;
                        Fluid fluidToPlace = null;
                        boolean fromAE = false;
                        IItemHandler handler = null;
                        int foundSlot = -1;

                        for (ItemStack candidate : candidates) {
                            if (candidate.isEmpty()) continue;
                            Fluid fluid = gtlsupb$fluid(candidate, fluidCache);
                            if (fluid != null) {

                                if (aeInventory != null) {
                                    if (aeInventory.extract(AEFluidKey.of(fluid), 1000, Actionable.MODULATE, source) >= 1000) {
                                        fluidToPlace = fluid;
                                        fromAE = true;
                                        break;
                                    }
                                }

                                if (fluidToPlace == null) {
                                    if (extractFluidFromPlayerRecursively(player, fluid)) {
                                        fluidToPlace = fluid;
                                        break;
                                    }
                                }
                            } else {

                                if (aeInventory != null) {
                                    if (aeInventory.extract(AEItemKey.of(candidate), 1, Actionable.MODULATE, source) > 0) {
                                        found = candidate.copy();
                                        fromAE = true;
                                        break;
                                    }
                                }

                                if (found == null) {
                                    FoundItemResult result = foundItem(player, Arrays.asList(candidate), item -> true, playerHandler);
                                    if (result.first != null) {
                                        found = result.first;
                                        handler = result.second;
                                        foundSlot = result.third;
                                        break;
                                    }
                                }
                            }
                        }

                        if (found == null && fluidToPlace == null) {

                            for (ItemStack candidate : candidates) {
                                if (!candidate.isEmpty()) {
                                    missingItems.add(candidate.copy());
                                    break;
                                }
                            }
                            missingPositions.add(pos);
                            x++;
                            continue;
                        }

                        if (autoBuildSetting.isReplaceMode && itemStack != null) {
                            world.removeBlock(pos, true);
                            if (useAEForOldBlock && aeInventory != null) {

                                aeInventory.insert(AEItemKey.of(itemStack), (long) itemStack.getCount(), Actionable.MODULATE, source);
                            } else if (holderHandler != null && holderSlot >= 0) {

                                holderHandler.insertItem(holderSlot, itemStack, false);
                            } else {

                                Block.popResource(world, pos, itemStack);
                            }
                        }

                        if (fluidToPlace != null) {
                            BlockState state = fluidToPlace.defaultFluidState().createLegacyBlock();
                            if (!state.isAir()) {
                                world.setBlock(pos, state, 3);
                                placeBlockPos.add(pos);
                                blocks.put(pos, state);
                            }
                        } else if (found != null && found.getItem() instanceof BlockItem) {
                            BlockItem itemBlock = (BlockItem) found.getItem();
                            BlockPlaceContext context = new BlockPlaceContext(world, player, InteractionHand.MAIN_HAND, found,
                                    BlockHitResult.miss(player.getEyePosition(0f), Direction.UP, pos));
                            InteractionResult interactionResult = itemBlock.place(context);
                            if (interactionResult != InteractionResult.FAIL) {
                                placeBlockPos.add(pos);
                                if (handler != null && !fromAE) handler.extractItem(foundSlot, 1, false);
                            }
                            if (world.getBlockEntity(pos) instanceof IMachineBlockEntity) {
                                blocks.put(pos, ((IMachineBlockEntity) world.getBlockEntity(pos)).getMetaMachine());
                            } else {
                                blocks.put(pos, world.getBlockState(pos));
                            }
                        }
                        x++;
                    }
                    y++;
                }
                z++;
            }
        }

        Direction frontFacing = controller.self().getFrontFacing();
        for (var entry : blocks.object2ObjectEntrySet()) {
            BlockPos pos = entry.getKey();
            Object block = entry.getValue();
            if (!(block instanceof IMultiController)) {
                if (block instanceof BlockState && placeBlockPos.contains(pos)) {
                    resetFacing(pos, (BlockState) block, frontFacing, (p, f) -> {
                        Object obj = blocks.get(p.relative(f));
                        return obj == null || (obj instanceof BlockState && ((BlockState) obj).getBlock() == Blocks.AIR);
                    }, state -> world.setBlock(pos, state, 3));
                } else if (block instanceof MetaMachine) {
                    MetaMachine machine = (MetaMachine) block;
                    resetFacing(pos, machine.getBlockState(), frontFacing, (p, f) -> {
                        Object obj = blocks.get(p.relative(f));
                        if (obj == null || (obj instanceof BlockState && ((BlockState) obj).isAir())) {
                            return machine.isFacingValid(f);
                        } else return false;
                    }, state -> world.setBlock(pos, state, 3));
                }
            }
        }

        AutoBuildResult result = new AutoBuildResult(missingItems);
        result.placedPositions.addAll(placeBlockPos);
        result.missingPositions.addAll(missingPositions);
        return result;
    }

    public void dismantleMultiblock(IMultiController controller, Player player, int repeatCountSetting, boolean isFlipped, boolean aeMode, GlobalPos boundAE) {
        Level level = player.level();
        if (level.isClientSide()) return;

        BlockPos centerPos = controller.self().getPos();
        Direction facing = controller.self().getFrontFacing();
        Direction upwardsFacing = controller.self().getUpwardsFacing();

        appeng.api.networking.IGrid grid = aeMode ? findBestGrid(level, boundAE) : null;
        appeng.api.storage.MEStorage aeInventory = grid != null ? grid.getStorageService().getInventory() : null;
        IActionSource source = IActionSource.ofPlayer(player);

        MultiblockState worldState = controller.getMultiblockState();
        Map<SimplePredicate, BlockInfo[]> candidateCache = new IdentityHashMap<>();
        Map<ItemStack, appeng.api.stacks.AEItemKey> dropKeyCache = new IdentityHashMap<>();
        ItemStack terminalCell = com.gtlsupb.gtlsupb.common.item.UltimateTerminalBehavior.findSuperTerminalCell(player);
        boolean collectToCell = !terminalCell.isEmpty() &&
                com.gtlsupb.gtlsupb.common.item.SuperTerminalCellItem.isCollectEnabled(terminalCell);

        int[] repeat = new int[fingerLength];
        for (int h = 0; h < fingerLength; h++) {
            int minH = aisleRepetitions[h][0];
            int maxH = aisleRepetitions[h][1];
            repeat[h] = minH != maxH ? Math.max(minH, Math.min(maxH, repeatCountSetting)) : minH;
        }

        int minZ = -centerOffset[4];
        int z = minZ;
        for (int c = 0; c < fingerLength; c++) {
            for (int r = 0; r < repeat[c]; r++) {
                int y = -centerOffset[1];
                for (int b = 0; b < thumbLength; b++) {
                    int x = -centerOffset[0];
                    for (int a = 0; a < palmLength; a++) {
                        TraceabilityPredicate predicate = blockMatches[c][b][a];
                        if (predicate.isAny()) {
                            x++;
                            continue;
                        }

                        BlockPos pos = setActualRelativeOffset(x, y, z, facing, upwardsFacing, isFlipped)
                                .offset(centerPos.getX(), centerPos.getY(), centerPos.getZ());
                        if (pos.equals(centerPos)) {
                            x++;
                            continue;
                        }

                        BlockState blockState = level.getBlockState(pos);
                        if (blockState.isAir()) {
                            x++;
                            continue;
                        }

                        if (!gtlsupb$isBlockValid(blockState, pos, predicate, worldState, candidateCache)) {
                            x++;
                            continue;
                        }

                        if (level instanceof ServerLevel) {
                            if (blockState.getBlock() instanceof LiquidBlock) {
                                LiquidBlock liquidBlock = (LiquidBlock) blockState.getBlock();
                                if (aeInventory != null) {
                                    aeInventory.insert(AEFluidKey.of(liquidBlock.getFluid()), 1000, Actionable.MODULATE, source);
                                }
                                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                            } else {
                                List<ItemStack> drops = Block.getDrops(blockState, (ServerLevel) level, pos, level.getBlockEntity(pos), player, player.getMainHandItem());
                                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                                for (ItemStack drop : drops) {
                                    ItemStack remainder = drop;
                                    if (aeInventory != null) {
                                        appeng.api.stacks.AEItemKey dropKey = dropKeyCache.get(remainder);
                                        if (dropKey == null) {
                                            dropKey = AEItemKey.of(remainder);
                                            dropKeyCache.put(remainder, dropKey);
                                        }
                                        long inserted = aeInventory.insert(dropKey, (long) remainder.getCount(), Actionable.MODULATE, source);
                                        if (inserted == (long) remainder.getCount()) {
                                            remainder = ItemStack.EMPTY;
                                        } else {
                                            remainder.shrink((int) inserted);
                                        }
                                    }
                                    if (remainder.isEmpty()) continue;

                                    if (collectToCell) {
                                        java.util.List<ItemStack> cellLeftover = com.gtlsupb.gtlsupb.common.item.SuperTerminalCellItem
                                                .insertItems(terminalCell, java.util.List.of(remainder.copy()));
                                        int notMovedIntoCell = cellLeftover.isEmpty() ? 0 : cellLeftover.get(0).getCount();
                                        int movedIntoCell = remainder.getCount() - notMovedIntoCell;
                                        if (movedIntoCell > 0) remainder.shrink(movedIntoCell);
                                    }
                                    if (remainder.isEmpty()) continue;
                                    remainder = insertIntoNestedItemHandler(player, remainder);
                                    if (!remainder.isEmpty()) {
                                        if (player.getInventory().add(remainder)) remainder = ItemStack.EMPTY;
                                    }
                                    if (!remainder.isEmpty()) {
                                        player.drop(remainder, false);
                                    }
                                }
                            }
                        }
                        x++;
                    }
                    y++;
                }
                z++;
            }
        }

        if (controller instanceof WorkableMultiblockMachine) {
            ((WorkableMultiblockMachine) controller).onPartUnload();
        }
    }

    private static Fluid gtlsupb$fluid(ItemStack stack, Map<ItemStack, Fluid> cache) {
        if (cache.containsKey(stack)) return cache.get(stack);
        Fluid fluid = getFluid(stack);
        cache.put(stack, fluid);
        return fluid;
    }

    private static List<ItemStack> gtlsupb$appliedCandidates(UltimateTerminalBehavior.AutoBuildSetting setting,
                                                             BlockInfo[] infos,
                                                             Map<BlockInfo[], List<ItemStack>> cache) {
        List<ItemStack> cached = cache.get(infos);
        if (cached == null) {
            cached = setting.apply(infos);
            cache.put(infos, cached);
        }
        return cached;
    }

    private static BlockInfo[] gtlsupb$candidates(SimplePredicate predicate,
                                                  Map<SimplePredicate, BlockInfo[]> cache) {
        if (predicate == null || predicate.candidates == null) return null;
        BlockInfo[] cached = cache.get(predicate);
        if (cached == null) {
            cached = predicate.candidates.get();
            cache.put(predicate, cached == null ? new BlockInfo[0] : cached);
        }
        return cached.length == 0 ? null : cached;
    }

    private boolean gtlsupb$isBlockValid(BlockState current, BlockPos pos, TraceabilityPredicate predicate,
                                         MultiblockState tempState,
                                         Map<SimplePredicate, BlockInfo[]> candidateCache) {
        List<SimplePredicate> allPreds = new ArrayList<>();
        if (predicate.common != null) allPreds.addAll(predicate.common);
        if (predicate.limited != null) allPreds.addAll(predicate.limited);

        for (SimplePredicate p : allPreds) {
            BlockInfo[] infos = gtlsupb$candidates(p, candidateCache);
            if (infos != null) {
                for (BlockInfo info : infos) {
                    if (info != null && info.getBlockState() != null && info.getBlockState().is(current.getBlock())) return true;
                }
            } else {
                clearWorldState(tempState);
                updateWorldState(tempState, pos, predicate);
                if (p.test(tempState)) return true;
            }
        }
        return false;
    }

    private appeng.api.networking.IGrid findBestGrid(Level level, GlobalPos boundCoord) {
        if (boundCoord != null && boundCoord.dimension() == level.dimension()) {
            BlockEntity be = level.getBlockEntity(boundCoord.pos());
            IGridNode node = getGridNode(be);
            if (node != null && node.getGrid() != null) return node.getGrid();
        }
        return null;
    }

    private boolean extractFluidFromPlayerRecursively(Player player, Fluid fluid) {
        if (player.isCreative()) return true;
        LazyOptional<IItemHandler> cap = player.getCapability(ForgeCapabilities.ITEM_HANDLER);
        if (!cap.isPresent()) return false;
        return extractFluidRecursive(cap.resolve().orElse(null), fluid);
    }

    private boolean extractFluidRecursive(IItemHandler handler, Fluid fluid) {
        if (handler == null) return false;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            LazyOptional<IFluidHandlerItem> fluidCap = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);
            if (fluidCap.isPresent()) {
                IFluidHandlerItem fluidHandler = fluidCap.resolve().orElse(null);
                if (fluidHandler != null) {
                    FluidStack simulated = fluidHandler.drain(new FluidStack(fluid, 1000), IFluidHandler.FluidAction.SIMULATE);
                    if (simulated.getAmount() == 1000) {
                        fluidHandler.drain(new FluidStack(fluid, 1000), IFluidHandler.FluidAction.EXECUTE);
                        if (handler instanceof IItemHandlerModifiable) {
                            ((IItemHandlerModifiable) handler).setStackInSlot(i, fluidHandler.getContainer());
                        }
                        return true;
                    }
                }
            }
            LazyOptional<IItemHandler> subItemCap = stack.getCapability(ForgeCapabilities.ITEM_HANDLER);
            if (subItemCap.isPresent()) {
                if (extractFluidRecursive(subItemCap.resolve().orElse(null), fluid)) return true;
            }
        }
        return false;
    }

    private ItemStack insertIntoNestedItemHandler(Player player, ItemStack stack) {
        LazyOptional<IItemHandler> cap = player.getCapability(ForgeCapabilities.ITEM_HANDLER);
        if (cap.isPresent()) return insertIntoNestedItemHandlerRecursion(cap.resolve().orElse(null), stack);
        return stack;
    }

    private ItemStack insertIntoNestedItemHandlerRecursion(IItemHandler handler, ItemStack stack) {
        if (handler == null) return stack;
        ItemStack remaining = stack;
        for (int i = 0; i < handler.getSlots(); i++) {
            if (remaining.isEmpty()) return remaining;
            ItemStack inSlot = handler.getStackInSlot(i);
            if (inSlot.isEmpty()) continue;
            LazyOptional<IItemHandler> subCap = inSlot.getCapability(ForgeCapabilities.ITEM_HANDLER);
            if (subCap.isPresent()) {
                IItemHandler subHandler = subCap.resolve().orElse(null);
                if (subHandler != null) {
                    remaining = insertIntoNestedItemHandlerRecursion(subHandler, remaining);
                    if (remaining.isEmpty()) return remaining;
                    remaining = ItemHandlerHelper.insertItemStacked(subHandler, remaining, false);
                }
            }
        }
        return remaining;
    }

    private FoundItemResult foundItem(Player player, List<ItemStack> candidates, Predicate<Item> test) {
        return foundItem(player, candidates, test,
                player.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve().orElse(null));
    }

    private FoundItemResult foundItem(Player player, List<ItemStack> candidates, Predicate<Item> test,
                                      IItemHandler playerHandler) {
        ItemStack found = null;
        IItemHandler handler = null;
        int foundSlot = -1;
        if (!player.isCreative()) {
            IntObjectPair<IItemHandler> foundHandler = playerHandler == null ? null : getMatchStackWithHandler(candidates, playerHandler, test);
            if (foundHandler != null) {
                foundSlot = foundHandler.leftInt();
                handler = foundHandler.second();
                found = handler.getStackInSlot(foundSlot).copy();
            }
        } else {
            for (ItemStack candidate : candidates) {
                found = candidate.copy();
                if (!found.isEmpty() && test.test(found.getItem())) break;
                found = null;
            }
        }
        return new FoundItemResult(found, handler, foundSlot);
    }

    private IntObjectPair<IItemHandler> foundHolderSlot(Player player, ItemStack coilItemStack) {
        if (!player.isCreative()) {
            IntObjectPair<IItemHandler> r = findFirstSlot(player.getCapability(ForgeCapabilities.ITEM_HANDLER), coilItemStack);
            if (r != null) return r;
        }
        return IntObjectPair.of(-1, null);
    }

    private IntObjectPair<IItemHandler> findFirstSlot(LazyOptional<IItemHandler> cap, ItemStack stack) {
        IItemHandler handler = cap.resolve().orElse(null);
        if (handler == null) return null;
        int foundSlot = -1;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack inSlot = handler.getStackInSlot(i);
            LazyOptional<IItemHandler> stackCap = inSlot.getCapability(ForgeCapabilities.ITEM_HANDLER);
            if (stackCap.isPresent()) {
                IntObjectPair<IItemHandler> rt = findFirstSlot(stackCap, stack);
                if (rt != null) return rt;
            } else {
                if (inSlot.isEmpty() && !((handler instanceof PlayerInvWrapper) && i >= 36)) {
                    if (foundSlot < 0) foundSlot = i;
                } else if (ItemStack.isSameItemSameTags(stack, inSlot) && (inSlot.getCount() + 1) <= inSlot.getMaxStackSize()) {
                    foundSlot = i;
                }
                if (foundSlot > 0) break;
            }
        }
        return IntObjectPair.of(foundSlot, handler);
    }

    private void clearWorldState(MultiblockState worldState) {
        try {
            Class<?> clazz = Class.forName("com.gregtechceu.gtceu.api.pattern.MultiblockState");
            Method method = clazz.getDeclaredMethod("clean");
            method.setAccessible(true);
            method.invoke(worldState);
        } catch (Exception e) {}
    }

    private void updateWorldState(MultiblockState worldState, BlockPos posIn, TraceabilityPredicate predicate) {
        try {
            Class<?> clazz = Class.forName("com.gregtechceu.gtceu.api.pattern.MultiblockState");
            Method method = clazz.getDeclaredMethod("update", BlockPos.class, TraceabilityPredicate.class);
            method.setAccessible(true);
            method.invoke(worldState, posIn, predicate);
        } catch (Exception e) {}
    }

    private IntObjectPair<IItemHandler> getMatchStackWithHandler(List<ItemStack> candidates, LazyOptional<IItemHandler> cap, Predicate<Item> test) {
        return getMatchStackWithHandler(candidates, cap.resolve().orElse(null), test);
    }

    private IntObjectPair<IItemHandler> getMatchStackWithHandler(List<ItemStack> candidates, IItemHandler handler, Predicate<Item> test) {
        if (handler == null) return null;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            boolean hasMatch = false;
            for (ItemStack cand : candidates) {
                if (ItemStack.isSameItem(cand, stack)) {
                    hasMatch = true;
                    break;
                }
            }
            if (hasMatch && test.test(stack.getItem())) {
                return IntObjectPair.of(i, handler);
            }
            LazyOptional<IItemHandler> subCap = stack.getCapability(ForgeCapabilities.ITEM_HANDLER);
            if (subCap.isPresent()) {
                IntObjectPair<IItemHandler> result = getMatchStackWithHandler(candidates, subCap, test);
                if (result != null) return result;
            }
        }
        return null;
    }

    private void resetFacing(BlockPos pos, BlockState state, Direction frontFacing, BiPredicate<BlockPos, Direction> isFree, Consumer<BlockState> setBlock) {
        for (net.minecraft.world.level.block.state.properties.Property<?> property : state.getProperties()) {
            if (property.getName().equals("facing") && property.getValueClass() == Direction.class) {
                @SuppressWarnings("unchecked")
                net.minecraft.world.level.block.state.properties.DirectionProperty dirProp = (net.minecraft.world.level.block.state.properties.DirectionProperty) property;

                Direction[] tryFacings;
                if (frontFacing.getAxis() == Direction.Axis.Y) {
                    tryFacings = FACINGS;
                } else {
                    tryFacings = ArrayUtils.insert(0, FACINGS_H.clone(), frontFacing);
                }
                for (Direction dir : tryFacings) {
                    if (isFree.test(pos, dir)) {
                        try {
                            setBlock.accept(state.setValue(dirProp, dir));
                        } catch (Exception e) {}
                        return;
                    }
                }
            }
        }
    }

    private BlockPos setActualRelativeOffset(int x, int y, int z, Direction facing, Direction upwardsFacing, boolean isFlipped) {
        int[] c0 = { x, y, z };
        int[] c1 = new int[3];
        if (facing == Direction.UP || facing == Direction.DOWN) {
            Direction of = facing == Direction.DOWN ? upwardsFacing : upwardsFacing.getOpposite();
            for (int i = 0; i <= 2; i++) {
                Direction actualFacing = structureDir[i].getActualFacing(of);
                if (actualFacing == Direction.UP) c1[1] = c0[i];
                else if (actualFacing == Direction.DOWN) c1[1] = -c0[i];
                else if (actualFacing == Direction.WEST) c1[0] = -c0[i];
                else if (actualFacing == Direction.EAST) c1[0] = c0[i];
                else if (actualFacing == Direction.NORTH) c1[2] = -c0[i];
                else if (actualFacing == Direction.SOUTH) c1[2] = c0[i];
            }

            int xOffset = upwardsFacing.getStepX();
            int zOffset = upwardsFacing.getStepZ();
            int tmp;
            if (xOffset == 0) {
                tmp = c1[2];
                c1[2] = zOffset > 0 ? c1[1] : -c1[1];
                c1[1] = zOffset > 0 ? -tmp : tmp;
            } else {
                tmp = c1[0];
                c1[0] = xOffset > 0 ? c1[1] : -c1[1];
                c1[1] = xOffset > 0 ? -tmp : tmp;
            }
            if (isFlipped) {
                if (upwardsFacing == Direction.NORTH || upwardsFacing == Direction.SOUTH) {
                    c1[0] = -c1[0];
                } else {
                    c1[2] = -c1[2];
                }
            }
        } else {
            for (int i = 0; i <= 2; i++) {
                Direction actualFacing = structureDir[i].getActualFacing(facing);
                if (actualFacing == Direction.UP) c1[1] = c0[i];
                else if (actualFacing == Direction.DOWN) c1[1] = -c0[i];
                else if (actualFacing == Direction.WEST) c1[0] = -c0[i];
                else if (actualFacing == Direction.EAST) c1[0] = c0[i];
                else if (actualFacing == Direction.NORTH) c1[2] = -c0[i];
                else if (actualFacing == Direction.SOUTH) c1[2] = c0[i];
            }

            if (upwardsFacing == Direction.WEST || upwardsFacing == Direction.EAST) {
                int rotXOffset = upwardsFacing == Direction.EAST ? facing.getClockWise().getStepX() : facing.getClockWise().getOpposite().getStepX();
                int rotZOffset = upwardsFacing == Direction.EAST ? facing.getClockWise().getStepZ() : facing.getClockWise().getOpposite().getStepZ();
                int tmp;
                if (rotXOffset == 0) {
                    tmp = c1[2];
                    c1[2] = rotZOffset > 0 ? -c1[1] : c1[1];
                    c1[1] = rotZOffset > 0 ? tmp : -tmp;
                } else {
                    tmp = c1[0];
                    c1[0] = rotXOffset > 0 ? -c1[1] : c1[1];
                    c1[1] = rotXOffset > 0 ? tmp : -tmp;
                }
            } else if (upwardsFacing == Direction.SOUTH) {
                c1[1] = -c1[1];
                if (facing.getStepX() == 0) {
                    c1[0] = -c1[0];
                } else {
                    c1[2] = -c1[2];
                }
            }
            if (isFlipped) {
                if (upwardsFacing == Direction.NORTH || upwardsFacing == Direction.SOUTH) {
                    if (facing == Direction.NORTH || facing == Direction.SOUTH) {
                        c1[0] = -c1[0];
                    } else {
                        c1[2] = -c1[2];
                    }
                } else {
                    c1[1] = -c1[1];
                }
            }
        }
        return new BlockPos(c1[0], c1[1], c1[2]);
    }
}
