package com.gtlsupb.gtlsupb.common;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialRegistryEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.event.PostMaterialEvent;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.gtlsupb.gtlsupb.GTLSupb;
import com.gtlsupb.gtlsupb.common.data.GTLSupbMaterials;
import com.gtlsupb.gtlsupb.common.machine.GTLSupbMachines;
import com.gtlsupb.gtlsupb.common.modify.GTLSupbCreativeModeTabs;
import com.gtlsupb.gtlsupb.common.recipe.GTLSupbRecipeTypes;
import com.gtlsupb.gtlsupb.config.ConfigHolder;
import com.gtlsupb.gtlsupb.network.GTLSupbNetworkHandler;

public class CommonProxy {

    public CommonProxy() {
        initInternal();
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        com.gtlsupb.gtlsupb.api.registry.GTLSupbRegistration.REGISTRATE.registerEventListeners(eventBus);
        eventBus.addListener(this::commonSetup);
        eventBus.addListener(this::clientSetup);
        eventBus.addListener(this::addMaterialRegistries);
        eventBus.addListener(this::addMaterials);
        eventBus.addListener(this::modifyMaterials);
        eventBus.addGenericListener(GTRecipeType.class, this::registerRecipeTypes);
        eventBus.addGenericListener(MachineDefinition.class, this::registerMachines);

        MinecraftForge.EVENT_BUS.register(
                com.gtlsupb.gtlsupb.integration.ae2.OverlimitQuantumEntanglementWirelessManager.class);
    }

    public static void initInternal() {
        GTLSupbCreativeModeTabs.init();
        ConfigHolder.init();
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(this::postRegistrationInitialization);
    }

    public void postRegistrationInitialization() {
        com.gtlsupb.gtlsupb.common.machine.GTCoreStructureOverrides.init();
        GTLSupbNetworkHandler.init();

        com.gtlsupb.gtlsupb.integration.ftbquests.AEQuestIntegration.init();

        appeng.api.networking.GridServices.register(
                com.gtlsupb.gtlsupb.integration.ae2.AEGridTrackerService.class,
                com.gtlsupb.gtlsupb.integration.ae2.AEGridTrackerService.class);
    }

    protected void clientSetup(FMLClientSetupEvent event) {}

    private void addMaterialRegistries(MaterialRegistryEvent event) {
        GTCEuAPI.materialManager.createRegistry(GTLSupb.MOD_ID);
    }

    private void addMaterials(MaterialEvent event) {
        GTLSupbMaterials.init();
    }

    private void modifyMaterials(PostMaterialEvent event) {}

    private void registerRecipeTypes(GTCEuAPI.RegisterEvent<ResourceLocation, GTRecipeType> event) {
        GTLSupbRecipeTypes.init();

        com.gtlsupb.gtlsupb.common.modify.RecipesModify.init();
    }

    private void registerMachines(GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition> event) {
        GTLSupbMachines.init();
    }
}
