package net.devil.mod.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.devil.mod.DevilMod;
import net.devil.mod.entity.BossEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class BossEyesLayer extends GeoRenderLayer<BossEntity> {
    private static final ResourceLocation GLOW_TEXTURE = new ResourceLocation(DevilMod.MOD_ID, "textures/entity/boss_glowing.png");

    public BossEyesLayer(GeoRenderer<BossEntity> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(PoseStack poseStack, BossEntity animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        // Возвращаем RenderType.eyes. Он игнорирует черный фон текстуры и светится в темноте.
        RenderType glowRenderType = RenderType.eyes(GLOW_TEXTURE);

        getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, glowRenderType,
                bufferSource.getBuffer(glowRenderType), partialTick, 15728640, OverlayTexture.NO_OVERLAY,
                1f, 1f, 1f, 1f);
    }
}