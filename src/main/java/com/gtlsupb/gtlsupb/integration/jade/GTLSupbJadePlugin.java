package com.gtlsupb.gtlsupb.integration.jade;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import com.gtlsupb.gtlsupb.common.machine.generator.VoidEnergyAbsorberMachine;
import com.gtlsupb.gtlsupb.common.machine.multiblock.HypercubePhaseStirringCriticalPolymerizationCoreTower;
import com.gtlsupb.gtlsupb.common.machine.multiblock.HyperdimensionalEnergyStabilizationHub;
import com.gtlsupb.gtlsupb.common.machine.multiblock.LingZhuMachine;
import com.gtlsupb.gtlsupb.common.machine.multiblock.MicrocosmFoundry;
import com.gtlsupb.gtlsupb.common.machine.multiblock.MoWanMachine;
import com.gtlsupb.gtlsupb.common.machine.multiblock.SuperStellarWorldForgingHeavenShip;
import com.gtlsupb.gtlsupb.common.machine.multiblock.UniversalFactoryMachine;
import com.gtlsupb.gtlsupb.utils.RainbowText;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

@WailaPlugin
public class GTLSupbJadePlugin implements IWailaPlugin {

    public static final ResourceLocation OVERCLOCK_PROVIDER = new ResourceLocation("gtlsupb", "overclock_level");
    public static final ResourceLocation FOUNDRY_PROVIDER = new ResourceLocation("gtlsupb", "foundry_status");
    public static final ResourceLocation FACTORY_PROVIDER = new ResourceLocation("gtlsupb", "factory_status");
    public static final ResourceLocation VOID_ABSORBER_PROVIDER = new ResourceLocation("gtlsupb", "void_absorber_status");
    public static final ResourceLocation FORGING_SHIP_PROVIDER = new ResourceLocation("gtlsupb", "forging_ship_status");
    public static final ResourceLocation LINGZHU_MOWAN_PROVIDER = new ResourceLocation("gtlsupb", "lingzhu_mowan_status");
    public static final ResourceLocation STEAM_FACTORY_PROVIDER = new ResourceLocation("gtlsupb", "steam_factory_status");
    public static final ResourceLocation HYPERCUBE_PROVIDER = new ResourceLocation("gtlsupb", "hypercube_status");
    public static final ResourceLocation RECIPE_STATUS_PROVIDER = new ResourceLocation("gtlsupb", "recipe_status");

    private static final HatchJadeProvider HATCH_PROVIDER = new HatchJadeProvider();

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(OverclockDataProvider.INSTANCE, MetaMachineBlockEntity.class);
        registration.registerBlockDataProvider(FoundryDataProvider.INSTANCE, MetaMachineBlockEntity.class);
        registration.registerBlockDataProvider(FactoryDataProvider.INSTANCE, MetaMachineBlockEntity.class);
        registration.registerBlockDataProvider(VoidAbsorberDataProvider.INSTANCE, MetaMachineBlockEntity.class);
        registration.registerBlockDataProvider(ForgingShipDataProvider.INSTANCE, MetaMachineBlockEntity.class);
        registration.registerBlockDataProvider(LingZhuMoWanDataProvider.INSTANCE, MetaMachineBlockEntity.class);
        registration.registerBlockDataProvider(HypercubeDataProvider.INSTANCE, MetaMachineBlockEntity.class);
        registration.registerBlockDataProvider(HATCH_PROVIDER, MetaMachineBlockEntity.class);
        registration.registerBlockDataProvider(RecipeStatusDataProvider.INSTANCE, MetaMachineBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(OverclockClientProvider.INSTANCE, Block.class);
        registration.registerBlockComponent(FoundryClientProvider.INSTANCE, Block.class);
        registration.registerBlockComponent(FactoryClientProvider.INSTANCE, Block.class);
        registration.registerBlockComponent(VoidAbsorberClientProvider.INSTANCE, Block.class);
        registration.registerBlockComponent(ForgingShipClientProvider.INSTANCE, Block.class);
        registration.registerBlockComponent(LingZhuMoWanClientProvider.INSTANCE, Block.class);
        registration.registerBlockComponent(HypercubeClientProvider.INSTANCE, Block.class);
        registration.registerBlockComponent(HATCH_PROVIDER, Block.class);
        registration.registerBlockComponent(RecipeStatusClientProvider.INSTANCE, Block.class);
    }

    public static final class OverclockDataProvider implements IServerDataProvider<BlockAccessor> {

        public static final OverclockDataProvider INSTANCE = new OverclockDataProvider();

        private OverclockDataProvider() {}

        @Override
        public ResourceLocation getUid() {
            return OVERCLOCK_PROVIDER;
        }

        @Override
        public void appendServerData(CompoundTag data, BlockAccessor accessor) {
            if (accessor.getBlockEntity() instanceof MetaMachineBlockEntity) {
                var machine = ((MetaMachineBlockEntity) accessor.getBlockEntity()).getMetaMachine();
                if (machine instanceof HyperdimensionalEnergyStabilizationHub) {
                    HyperdimensionalEnergyStabilizationHub hub = (HyperdimensionalEnergyStabilizationHub) machine;
                    data.putLong("ocLevel", hub.getOcLevel());
                    data.putBoolean("ocEnabled", hub.isOcEnabled());
                    data.putLong("dimensionalStability", hub.getDimensionalStability());
                }
            }
        }
    }

    public static final class OverclockClientProvider implements IBlockComponentProvider {

        public static final OverclockClientProvider INSTANCE = new OverclockClientProvider();

        private OverclockClientProvider() {}

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            CompoundTag serverData = accessor.getServerData();
            if (!serverData.contains("ocLevel")) return;

            long ocLevel = serverData.getLong("ocLevel");
            boolean ocEnabled = serverData.getBoolean("ocEnabled");

            if (serverData.contains("dimensionalStability")) {
                long stability = serverData.getLong("dimensionalStability");

                if (ocEnabled && ocLevel > 0) {
                    tooltip.add(
                            Component.translatable(
                                    "gtlsupb.jade.overclock_level",
                                    RainbowText.createRainbowComponent(String.valueOf(ocLevel))));
                }

                tooltip.add(
                        Component.translatable(
                                "gtlsupb.jade.stability",
                                RainbowText.createRainbowComponent(String.format("%,d", stability))));
            }
        }

        @Override
        public ResourceLocation getUid() {
            return OVERCLOCK_PROVIDER;
        }
    }

    public static final class FoundryDataProvider implements IServerDataProvider<BlockAccessor> {

        public static final FoundryDataProvider INSTANCE = new FoundryDataProvider();

        private FoundryDataProvider() {}

        @Override
        public ResourceLocation getUid() {
            return FOUNDRY_PROVIDER;
        }

        @Override
        public void appendServerData(CompoundTag data, BlockAccessor accessor) {
            if (accessor.getBlockEntity() instanceof MetaMachineBlockEntity) {
                var machine = ((MetaMachineBlockEntity) accessor.getBlockEntity()).getMetaMachine();
                if (machine instanceof MicrocosmFoundry && ((MicrocosmFoundry) machine).isFormed()) {
                    MicrocosmFoundry foundry = (MicrocosmFoundry) machine;
                    data.putInt("foundryModuleCount", foundry.getFormedModuleCount());
                    data.putLong("foundryOutputMultiplier", foundry.getOutputMultiplier());
                }
            }
        }
    }

    public static final class FoundryClientProvider implements IBlockComponentProvider {

        public static final FoundryClientProvider INSTANCE = new FoundryClientProvider();

        private FoundryClientProvider() {}

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            CompoundTag serverData = accessor.getServerData();
            if (!serverData.contains("foundryModuleCount")) return;

            int moduleCount = serverData.getInt("foundryModuleCount");
            long outputMultiplier = serverData.getLong("foundryOutputMultiplier");

            tooltip.add(
                    Component.translatable(
                            "gtlsupb.jade.foundry.module_count",
                            RainbowText.createRainbowComponent(String.valueOf(moduleCount))));
            tooltip.add(
                    Component.translatable(
                            "gtlsupb.jade.foundry.output_multiplier",
                            RainbowText.createRainbowComponent("\u00D7" + outputMultiplier)));
        }

        @Override
        public ResourceLocation getUid() {
            return FOUNDRY_PROVIDER;
        }
    }

    public static final class FactoryDataProvider implements IServerDataProvider<BlockAccessor> {

        public static final FactoryDataProvider INSTANCE = new FactoryDataProvider();

        private FactoryDataProvider() {}

        @Override
        public ResourceLocation getUid() {
            return FACTORY_PROVIDER;
        }

        @Override
        public void appendServerData(CompoundTag data, BlockAccessor accessor) {
            if (accessor.getBlockEntity() instanceof MetaMachineBlockEntity) {
                var machine = ((MetaMachineBlockEntity) accessor.getBlockEntity()).getMetaMachine();
                if (machine instanceof UniversalFactoryMachine && ((UniversalFactoryMachine) machine).isFormed()) {
                    UniversalFactoryMachine factory = (UniversalFactoryMachine) machine;
                    data.putInt("maxParallel", factory.getMaxParallel());
                }
            }
        }
    }

    public static final class FactoryClientProvider implements IBlockComponentProvider {

        public static final FactoryClientProvider INSTANCE = new FactoryClientProvider();

        private FactoryClientProvider() {}

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            CompoundTag serverData = accessor.getServerData();
            if (!serverData.contains("maxParallel")) return;

            int maxParallel = serverData.getInt("maxParallel");

            tooltip.add(
                    Component.translatable(
                            "gtlsupb.jade.factory.max_parallel",
                            RainbowText.createRainbowComponent(String.format("%,d", maxParallel))));
        }

        @Override
        public ResourceLocation getUid() {
            return FACTORY_PROVIDER;
        }
    }

    public static final class VoidAbsorberDataProvider implements IServerDataProvider<BlockAccessor> {

        public static final VoidAbsorberDataProvider INSTANCE = new VoidAbsorberDataProvider();

        private VoidAbsorberDataProvider() {}

        @Override
        public ResourceLocation getUid() {
            return VOID_ABSORBER_PROVIDER;
        }

        @Override
        public void appendServerData(CompoundTag data, BlockAccessor accessor) {
            if (accessor.getBlockEntity() instanceof MetaMachineBlockEntity) {
                var machine = ((MetaMachineBlockEntity) accessor.getBlockEntity()).getMetaMachine();
                if (machine instanceof VoidEnergyAbsorberMachine) {
                    VoidEnergyAbsorberMachine absorber = (VoidEnergyAbsorberMachine) machine;
                    data.putLong("accumulatedEU", absorber.getLastOutputEU());
                    data.putString("voltageTier", absorber.getVoltageTierName());
                }
            }
        }
    }

    public static final class VoidAbsorberClientProvider implements IBlockComponentProvider {

        public static final VoidAbsorberClientProvider INSTANCE = new VoidAbsorberClientProvider();

        private VoidAbsorberClientProvider() {}

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            CompoundTag serverData = accessor.getServerData();
            if (!serverData.contains("accumulatedEU")) return;

            long accumulatedEU = serverData.getLong("accumulatedEU");
            String voltageTier = serverData.getString("voltageTier");

            tooltip.add(
                    Component.translatable(
                            "gtlsupb.jade.void_absorber.power",
                            RainbowText.createRainbowComponent(String.format("%,d", accumulatedEU))));

            tooltip.add(
                    Component.translatable(
                            "gtlsupb.jade.void_absorber.voltage",
                            RainbowText.createRainbowComponent(voltageTier)));
        }

        @Override
        public ResourceLocation getUid() {
            return VOID_ABSORBER_PROVIDER;
        }
    }

    public static final class ForgingShipDataProvider implements IServerDataProvider<BlockAccessor> {

        public static final ForgingShipDataProvider INSTANCE = new ForgingShipDataProvider();

        private ForgingShipDataProvider() {}

        @Override
        public ResourceLocation getUid() {
            return FORGING_SHIP_PROVIDER;
        }

        @Override
        public void appendServerData(CompoundTag data, BlockAccessor accessor) {
            if (accessor.getBlockEntity() instanceof MetaMachineBlockEntity) {
                var machine = ((MetaMachineBlockEntity) accessor.getBlockEntity()).getMetaMachine();
                if (machine instanceof SuperStellarWorldForgingHeavenShip && ((SuperStellarWorldForgingHeavenShip) machine).isFormed()) {
                    SuperStellarWorldForgingHeavenShip ship = (SuperStellarWorldForgingHeavenShip) machine;
                    data.putInt("forgingShipModuleCount", ship.getFormedModuleCount());
                    data.putLong("forgingShipOutputMultiplier", ship.getOutputMultiplier());
                }
            }
        }
    }

    public static final class ForgingShipClientProvider implements IBlockComponentProvider {

        public static final ForgingShipClientProvider INSTANCE = new ForgingShipClientProvider();

        private ForgingShipClientProvider() {}

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            CompoundTag serverData = accessor.getServerData();
            if (!serverData.contains("forgingShipModuleCount")) return;

            int moduleCount = serverData.getInt("forgingShipModuleCount");
            long outputMultiplier = serverData.getLong("forgingShipOutputMultiplier");

            tooltip.add(
                    Component.translatable(
                            "gtlsupb.jade.forging_ship.modules",
                            RainbowText.createRainbowComponent(String.valueOf(moduleCount))));
            tooltip.add(
                    Component.translatable(
                            "gtlsupb.jade.forging_ship.output_multiplier",
                            RainbowText.createRainbowComponent("\u00D7" + outputMultiplier)));
        }

        @Override
        public ResourceLocation getUid() {
            return FORGING_SHIP_PROVIDER;
        }
    }

    public static final class LingZhuMoWanDataProvider implements IServerDataProvider<BlockAccessor> {

        public static final LingZhuMoWanDataProvider INSTANCE = new LingZhuMoWanDataProvider();

        private LingZhuMoWanDataProvider() {}

        @Override
        public ResourceLocation getUid() {
            return LINGZHU_MOWAN_PROVIDER;
        }

        @Override
        public void appendServerData(CompoundTag data, BlockAccessor accessor) {
            if (accessor.getBlockEntity() instanceof MetaMachineBlockEntity) {
                var machine = ((MetaMachineBlockEntity) accessor.getBlockEntity()).getMetaMachine();
                if (machine instanceof LingZhuMachine) {
                    LingZhuMachine lingzhu = (LingZhuMachine) machine;
                    if (lingzhu.isFormed()) {
                        data.putString("type", "lingzhu");

                        data.putLong("parallel", lingzhu.getMaxParallelLong());
                        data.putBoolean("linked", lingzhu.isLinked());
                        data.putDouble("linkedBonus", 2.0);
                    }
                } else if (machine instanceof MoWanMachine) {
                    MoWanMachine mowan = (MoWanMachine) machine;
                    if (mowan.isFormed()) {
                        data.putString("type", "mowan");
                        data.putLong("parallel", mowan.getMaxParallelLong());
                        data.putBoolean("linked", mowan.isLinked());
                        data.putDouble("linkedBonus", 2.0);
                    }
                } else if (machine instanceof com.gtlsupb.gtlsupb.common.machine.multiblock.AlchemicalForgeMachine forge) {

                    if (forge.isFormed()) {
                        data.putString("type", "alchemical");
                        data.putLong("parallel", forge.getMaxParallelLong());
                        data.putBoolean("linked", false);
                        data.putDouble("linkedBonus", 1.0);
                    }
                } else if (machine instanceof com.gtlsupb.gtlsupb.common.machine.multiblock.module.SteamUniversalModule steam) {

                    if (steam.isFormed()) {
                        data.putString("type", "steam");
                        data.putLong("parallel", steam.getMaxParallel());
                        data.putBoolean("linked", false);
                        data.putDouble("linkedBonus", 1.0);
                    }
                }
            }
        }
    }

    public static final class LingZhuMoWanClientProvider implements IBlockComponentProvider {

        public static final LingZhuMoWanClientProvider INSTANCE = new LingZhuMoWanClientProvider();

        private LingZhuMoWanClientProvider() {}

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            CompoundTag serverData = accessor.getServerData();
            if (!serverData.contains("type")) return;

            String type = serverData.getString("type");
            long parallel = serverData.getLong("parallel");
            boolean linked = serverData.getBoolean("linked");
            double linkedBonus = serverData.getDouble("linkedBonus");

            String parallelKey = switch (type) {
                case "lingzhu" -> "gtlsupb.jade.lingzhu.parallel";
                case "alchemical" -> "gtlsupb.jade.alchemical_forge.parallel";
                case "steam" -> "gtlsupb.jade.steam_factory.parallel";
                default -> "gtlsupb.jade.mowan.parallel";
            };
            tooltip.add(
                    Component.translatable(
                            parallelKey,
                            RainbowText.createRainbowComponent(String.format("%,d", parallel))));

            if ("alchemical".equals(type) || "steam".equals(type)) return;

            if (linked) {
                String linkedKey = "lingzhu".equals(type) ? "gtlsupb.jade.lingzhu.linked" : "gtlsupb.jade.mowan.linked";
                tooltip.add(
                        Component.translatable(
                                linkedKey,
                                RainbowText.createRainbowComponent(String.format("%.1f", linkedBonus))));
            } else {
                String notLinkedKey = "lingzhu".equals(type) ? "gtlsupb.jade.lingzhu.not_linked" : "gtlsupb.jade.mowan.not_linked";
                tooltip.add(Component.translatable(notLinkedKey));
            }
        }

        @Override
        public ResourceLocation getUid() {
            return LINGZHU_MOWAN_PROVIDER;
        }
    }

    public static final class HypercubeDataProvider implements IServerDataProvider<BlockAccessor> {

        public static final HypercubeDataProvider INSTANCE = new HypercubeDataProvider();

        private HypercubeDataProvider() {}

        @Override
        public ResourceLocation getUid() {
            return HYPERCUBE_PROVIDER;
        }

        @Override
        public void appendServerData(CompoundTag data, BlockAccessor accessor) {
            if (accessor.getBlockEntity() instanceof MetaMachineBlockEntity) {
                var machine = ((MetaMachineBlockEntity) accessor.getBlockEntity()).getMetaMachine();
                if (machine instanceof HypercubePhaseStirringCriticalPolymerizationCoreTower) {
                    data.putLong("outputMultiplier", 100L);
                }
            }
        }
    }

    public static final class HypercubeClientProvider implements IBlockComponentProvider {

        public static final HypercubeClientProvider INSTANCE = new HypercubeClientProvider();

        private HypercubeClientProvider() {}

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            CompoundTag serverData = accessor.getServerData();
            if (!serverData.contains("outputMultiplier")) return;

            long outputMultiplier = serverData.getLong("outputMultiplier");

            tooltip.add(
                    Component.translatable(
                            "gtlsupb.jade.hypercube.output_multiplier",
                            RainbowText.createRainbowComponent(String.valueOf(outputMultiplier))));
        }

        @Override
        public ResourceLocation getUid() {
            return HYPERCUBE_PROVIDER;
        }
    }

    static String gtlsupb$resolvePlayerName(BlockAccessor accessor, java.util.UUID id) {
        try {
            var level = accessor.getLevel();
            var server = level == null ? null : level.getServer();
            if (server != null) {
                var player = server.getPlayerList().getPlayer(id);
                if (player != null) return player.getGameProfile().getName();
            }
        } catch (Throwable ignored) {}
        String raw = id.toString();
        return raw.length() > 8 ? raw.substring(0, 8) : raw;
    }

    public static final class RecipeStatusDataProvider implements IServerDataProvider<BlockAccessor> {

        public static final RecipeStatusDataProvider INSTANCE = new RecipeStatusDataProvider();

        private RecipeStatusDataProvider() {}

        @Override
        public ResourceLocation getUid() {
            return RECIPE_STATUS_PROVIDER;
        }

        @Override
        public void appendServerData(CompoundTag data, BlockAccessor accessor) {
            try {
                if (!(accessor.getBlockEntity() instanceof MetaMachineBlockEntity be)) return;
                var machine = be.getMetaMachine();
                if (!(machine instanceof com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine logicMachine)) return;
                var logic = logicMachine.getRecipeLogic();
                if (!(logic instanceof com.gtlsupb.gtlsupb.api.machine.logic.IMergedRecipeCount counter)) return;
                int merged = counter.gtlsupb$getMergedRecipeCount();
                if (merged <= 0) return;
                data.putInt("gtlsupb_merged", merged);
                var recipe = logic.getLastRecipe();
                if (recipe == null) return;
                var itemTotals = new java.util.LinkedHashMap<net.minecraft.world.item.Item, java.math.BigInteger>();
                var itemIcons = new java.util.LinkedHashMap<net.minecraft.world.item.Item, net.minecraft.world.item.ItemStack>();
                var fluidTotals = new java.util.LinkedHashMap<net.minecraft.world.level.material.Fluid, java.math.BigInteger>();
                for (var content : recipe.getOutputContents(com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability.CAP)) {
                    var ingredient = com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability.CAP.of(content.content);
                    long amount = ingredient instanceof org.gtlcore.gtlcore.api.recipe.ingredient.LongIngredient longIngredient ? longIngredient.getActualAmount() : ingredient instanceof com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient sized ? (long) sized.getAmount() : 1L;
                    if (amount <= 0L) continue;
                    var items = ingredient.getItems();
                    if (items.length == 0 || items[0].isEmpty()) continue;
                    var icon = items[0];
                    itemTotals.merge(icon.getItem(), java.math.BigInteger.valueOf(amount), java.math.BigInteger::add);
                    itemIcons.putIfAbsent(icon.getItem(), icon.copyWithCount(1));
                }
                for (var fluid : com.gregtechceu.gtceu.api.recipe.RecipeHelper.getOutputFluids(recipe)) {
                    if (fluid == null || fluid.isEmpty()) continue;
                    fluidTotals.merge(fluid.getFluid(), java.math.BigInteger.valueOf(fluid.getAmount()), java.math.BigInteger::add);
                }
                if (!itemTotals.isEmpty()) {
                    var outs = new net.minecraft.nbt.ListTag();
                    for (var entry : itemTotals.entrySet()) {
                        var icon = itemIcons.get(entry.getKey());
                        if (icon == null) continue;
                        var tag = new CompoundTag();
                        com.gregtechceu.gtceu.utils.GTUtil.saveItemStack(icon, tag);
                        tag.putString("gtlsupb_count", entry.getValue().toString());
                        outs.add(tag);
                    }
                    data.put("gtlsupb_outs", outs);
                }
                if (!fluidTotals.isEmpty()) {
                    var outFluids = new net.minecraft.nbt.ListTag();
                    for (var entry : fluidTotals.entrySet()) {
                        var tag = new CompoundTag();
                        com.lowdragmc.lowdraglib.side.fluid.FluidStack.create(entry.getKey(), 1L).saveToTag(tag);
                        tag.putString("gtlsupb_amount", entry.getValue().toString());
                        outFluids.add(tag);
                    }
                    data.put("gtlsupb_outf", outFluids);
                }
            } catch (Throwable ignored) {}
        }
    }

    public static final class RecipeStatusClientProvider implements IBlockComponentProvider {

        public static final RecipeStatusClientProvider INSTANCE = new RecipeStatusClientProvider();

        private RecipeStatusClientProvider() {}

        @Override
        public ResourceLocation getUid() {
            return RECIPE_STATUS_PROVIDER;
        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            CompoundTag data = accessor.getServerData();
            if (!data.contains("gtlsupb_merged")) return;
            int merged = data.getInt("gtlsupb_merged");
            if (merged > 0) tooltip.add(Component.translatable("gtlsupb.jade.merged_recipe_count", merged).withStyle(net.minecraft.ChatFormatting.WHITE));
            if (!data.contains("gtlsupb_outs") && !data.contains("gtlsupb_outf")) return;
            tooltip.add(Component.translatable("gtlsupb.jade.merged_outputs").withStyle(net.minecraft.ChatFormatting.WHITE));
            appendOutputs(tooltip, data);
        }

        private static void appendOutputs(ITooltip tooltip, CompoundTag data) {
            var helper = tooltip.getElementHelper();
            if (data.contains("gtlsupb_outs")) {
                var list = data.getList("gtlsupb_outs", 10);
                for (int i = 0; i < list.size(); i++) {
                    var tag = list.getCompound(i);
                    var stack = com.gregtechceu.gtceu.utils.GTUtil.loadItemStack(tag);
                    if (stack == null || stack.isEmpty()) continue;
                    var amount = parseAmount(tag.getString("gtlsupb_count"));
                    tooltip.add(helper.smallItem(stack.copyWithCount(1)));
                    tooltip.append(Component.literal(" §f" + plainCount(amount) + " × ").append(stack.getItem().getName(stack)).withStyle(net.minecraft.ChatFormatting.WHITE));
                }
            }
            if (data.contains("gtlsupb_outf")) {
                var list = data.getList("gtlsupb_outf", 10);
                for (int i = 0; i < list.size(); i++) {
                    var tag = list.getCompound(i);
                    var fluid = com.lowdragmc.lowdraglib.side.fluid.FluidStack.loadFromTag(tag);
                    if (fluid == null || fluid.isEmpty()) continue;
                    var amount = parseAmount(tag.getString("gtlsupb_amount"));
                    long display = amount.min(java.math.BigInteger.valueOf(Long.MAX_VALUE)).longValue();
                    tooltip.append(Component.literal(" §f" + plainCount(amount) + " mB ").append(fluid.getDisplayName()).withStyle(net.minecraft.ChatFormatting.WHITE));
                }
            }
        }

        private static java.math.BigInteger parseAmount(String value) {
            try {
                return new java.math.BigInteger(value);
            } catch (Throwable ignored) {
                return java.math.BigInteger.ZERO;
            }
        }

        private static final String[] UNITS = { "K", "M", "B", "T", "Qa", "Qi", "Sx", "Sp" };

        private static String plainCount(java.math.BigInteger amount) {
            if (amount == null || amount.signum() <= 0) return "0";
            var value = new java.math.BigDecimal(amount);
            var thousand = java.math.BigDecimal.valueOf(1000L);
            int unit = 0;
            while (value.compareTo(thousand) >= 0 && unit < UNITS.length) {
                value = value.movePointLeft(3);
                unit++;
            }
            if (unit == 0) return amount.toString();
            int scale = value.compareTo(java.math.BigDecimal.valueOf(100L)) >= 0 ? 0 : value.compareTo(java.math.BigDecimal.valueOf(10L)) >= 0 ? 1 : 2;
            return value.setScale(scale, java.math.RoundingMode.DOWN).toPlainString() + UNITS[unit - 1];
        }
    }
}
