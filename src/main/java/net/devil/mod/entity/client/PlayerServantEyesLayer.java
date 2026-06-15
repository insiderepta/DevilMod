package net.devil.mod.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.devil.mod.DevilMod;
import net.devil.mod.entity.PlayerServantEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class PlayerServantEyesLayer extends GeoRenderLayer<PlayerServantEntity> {
    private static final ResourceLocation GLOW_TEXTURE = ResourceLocation.fromNamespaceAndPath(DevilMod.MOD_ID, "textures/entity/servant_glowing.png");

    public PlayerServantEyesLayer(GeoRenderer<PlayerServantEntity> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(PoseStack poseStack, PlayerServantEntity animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        RenderType eyesRenderType = RenderType.eyes(GLOW_TEXTURE);
        getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, eyesRenderType,
                bufferSource.getBuffer(eyesRenderType), partialTick, 15728640, OverlayTexture.NO_OVERLAY,
                1f, 1f, 1f, 1f);
    }
}
