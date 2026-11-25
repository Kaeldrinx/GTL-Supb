package com.gtlsupb.gtlsupb.client.renderer.machine;

import org.gtlcore.gtlcore.GTLCore;
import org.gtlcore.gtlcore.client.ClientUtil;
import org.gtlcore.gtlcore.utils.RenderUtil;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.client.renderer.machine.WorkableCasingMachineRenderer;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;

import com.gtlsupb.gtlsupb.common.data.RotationParams;
import com.gtlsupb.gtlsupb.common.machine.multiblock.MicrocosmFoundry;
import com.gtlsupb.gtlsupb.common.machine.multiblock.module.MicrocosmFoundryModuleBase;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.function.Consumer;

public class MicrocosmFoundryRenderer extends WorkableCasingMachineRenderer {

    public MicrocosmFoundryRenderer() {
        super(GTLCore.id("block/molecular_casing"), GTCEu.id("block/multiblock/cosmos_simulation"));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void render(
                       BlockEntity blockEntity, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer,
                       int combinedLight, int combinedOverlay) {
        if (blockEntity instanceof IMachineBlockEntity) {
            var machine = ((IMachineBlockEntity) blockEntity).getMetaMachine();
            if (!(machine instanceof MicrocosmFoundry)) return;
            MicrocosmFoundry foundry = (MicrocosmFoundry) machine;
            if (foundry.isFormed()) {
                float tick = RenderUtil.getSmoothTick(foundry, partialTicks);
                long seed = blockEntity.getBlockPos().asLong();
                int moduleCount = foundry.getFormedModuleCount();
                boolean hasEternity = foundry.getModuleSet().stream().anyMatch(
                        it -> it instanceof MicrocosmFoundryModuleBase && ((MicrocosmFoundryModuleBase) it).isEternityModule());

                StageData stageData = getStageParams(moduleCount, hasEternity);

                Vec3 pos = getRotatedRenderPosition(Direction.SOUTH, foundry.getFrontFacing(), 0.0, 0.0, 20.0);

                renderStar(tick, poseStack, buffer, seed, pos.x, pos.y, pos.z,
                        stageData.r, stageData.g, stageData.b, stageData.scale);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void onAdditionalModel(Consumer<ResourceLocation> registry) {
        super.onAdditionalModel(registry);
        registry.accept(SPACE_MODEL);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean hasTESR(BlockEntity blockEntity) {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean isGlobalRenderer(BlockEntity blockEntity) {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public int getViewDistance() {
        return 512;
    }

    private static final ResourceLocation SPACE_MODEL = GTLCore.id("obj/space");
    private static final float HEARTBEAT_FREQUENCY = 0.14f;
    private static final float HEARTBEAT_AMPLITUDE = 0.22f;

    @OnlyIn(Dist.CLIENT)
    private static float calculateHeartbeatScale(float tick) {
        float phase = (float) Math.sin(tick * HEARTBEAT_FREQUENCY);
        return 1.0f - phase * phase * HEARTBEAT_AMPLITUDE;
    }

    @OnlyIn(Dist.CLIENT)
    private static RotationParams createRandomRotation(RandomSource random, float minSpeed, float maxSpeed) {
        float theta = random.nextFloat() * 2.0f * (float) Math.PI;
        float phi = (float) Math.acos(2.0f * random.nextFloat() - 1.0f);
        Vector3f axis = new Vector3f(
                (float) (Math.sin(phi) * Math.cos(theta)),
                (float) (Math.sin(phi) * Math.sin(theta)),
                (float) Math.cos(phi));
        float speed = minSpeed + (float) Math.sqrt(random.nextFloat()) * (maxSpeed - minSpeed);
        float offset = random.nextFloat() * 360f;
        return new RotationParams(axis, speed, offset);
    }

    @OnlyIn(Dist.CLIENT)
    private static Vec3 getRotatedRenderPosition(
                                                 Direction baseFacing, Direction targetFacing,
                                                 double offsetX, double offsetY, double offsetZ) {
        double y = 0.5 + offsetY;
        if (baseFacing == targetFacing) return new Vec3(0.5 + offsetX, y, 0.5 + offsetZ);

        int baseIdx = getHorizontalIndex(baseFacing);
        int targetIdx = getHorizontalIndex(targetFacing);
        switch ((targetIdx - baseIdx + 4) % 4) {
            case 0:
                return new Vec3(0.5 + offsetX, y, 0.5 + offsetZ);
            case 1:
                return new Vec3(0.5 - offsetZ, y, 0.5 + offsetX);
            case 2:
                return new Vec3(0.5 - offsetX, y, 0.5 - offsetZ);
            case 3:
                return new Vec3(0.5 + offsetZ, y, 0.5 - offsetX);
            default:
                return new Vec3(0.5 + offsetX, y, 0.5 + offsetZ);
        }
    }

    private static int getHorizontalIndex(Direction facing) {
        switch (facing) {
            case EAST:
                return 0;
            case SOUTH:
                return 1;
            case WEST:
                return 2;
            case NORTH:
                return 3;
            default:
                return 0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static StageData getStageParams(int moduleCount, boolean hasEternity) {
        if (moduleCount >= 4 && hasEternity) return new StageData(255, 220, 255, 0.80f);
        if (moduleCount >= 3) return new StageData(100, 180, 255, 0.55f);
        if (moduleCount >= 2) return new StageData(80, 60, 220, 0.40f);
        return new StageData(120, 20, 200, 0.28f);
    }

    public static final class StageData {

        public final int r;
        public final int g;
        public final int b;
        public final float scale;

        public StageData(int r, int g, int b, float scale) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.scale = scale;
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void renderStar(
                                   float tick, PoseStack poseStack, MultiBufferSource buffer,
                                   long randomSeed, double x, double y, double z,
                                   int r, int g, int b, float stageScale) {
        poseStack.pushPose();
        poseStack.translate(x, y, z);

        float heartbeat = calculateHeartbeatScale(tick);
        float finalScale = heartbeat * stageScale;
        poseStack.scale(finalScale, finalScale, finalScale);

        RotationParams rotation = createRandomRotation(RandomSource.create(randomSeed), 0.5f, 2.0f);
        poseStack.scale(0.25f, 0.25f, 0.25f);
        poseStack.mulPose(
                new Quaternionf().fromAxisAngleDeg(
                        rotation.getAxis().x(), rotation.getAxis().y(), rotation.getAxis().z(),
                        rotation.getAngle(tick)));

        var model = ClientUtil.getBakedModel(SPACE_MODEL);
        ClientUtil.modelRenderer().renderModel(
                poseStack.last(),
                buffer.getBuffer(RenderType.solid()),
                null, model,
                r / 255f,
                g / 255f,
                b / 255f,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                ModelData.EMPTY,
                RenderType.solid());

        poseStack.popPose();
    }
}
