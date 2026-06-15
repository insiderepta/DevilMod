package net.devil.mod.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.devil.mod.entity.PlayerServantEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

import javax.annotation.Nullable;

public class PlayerServantRenderer extends GeoEntityRenderer<PlayerServantEntity> {
    public PlayerServantRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new PlayerServantModel());
        this.addRenderLayer(new PlayerServantEyesLayer(this));

        addRenderLayer(new BlockAndItemGeoLayer<>(this) {
            @Nullable
            @Override
            protected ItemStack getStackForBone(GeoBone bone, PlayerServantEntity animatable) {
                if (bone.getName().equals("right_item")) {
                    return animatable.getMainHandItem();
                }
                return null;
            }

            @Override
            protected ItemDisplayContext getTransformTypeForStack(GeoBone bone, ItemStack stack, PlayerServantEntity animatable) {
                return ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
            }

            @Override
            protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack, PlayerServantEntity animatable, MultiBufferSource bufferSource, float partialTick, int packedLight, int packedOverlay) {
                super.renderStackForBone(poseStack, bone, stack, animatable, bufferSource, partialTick, packedLight, packedOverlay);
            }
         });
    }
}
