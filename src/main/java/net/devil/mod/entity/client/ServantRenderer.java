package net.devil.mod.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.devil.mod.entity.ServantEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

import javax.annotation.Nullable;

public class ServantRenderer extends GeoEntityRenderer<ServantEntity> {
    public ServantRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ServantModel()); // Убедись, что ServantModel называется так же, как у тебя
        this.addRenderLayer(new ServantEyesLayer(this));

        // ==========================================
        // МАГИЯ РЕНДЕРА МЕЧА
        // ==========================================
        addRenderLayer(new BlockAndItemGeoLayer<>(this) {
            @Nullable
            @Override
            protected ItemStack getStackForBone(GeoBone bone, ServantEntity animatable) {
                // Ищем ту самую кость, которую ты создал в Blockbench
                if (bone.getName().equals("right_item")) {
                    return animatable.getMainHandItem(); // Возвращаем незеритовый меч!
                }
                return null;
            }

            @Override
            protected ItemDisplayContext getTransformTypeForStack(GeoBone bone, ItemStack stack, ServantEntity animatable) {
                // Рендерим как предмет в руке от третьего лица
                return ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
            }

            @Override
            protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack, ServantEntity animatable, MultiBufferSource bufferSource, float partialTick, int packedLight, int packedOverlay) {
                // Если меч будет торчать криво (например, лезвием в руку),
                // ты сможешь покрутить его здесь с помощью poseStack.mulPose(...)
                // Или просто повернуть саму кость right_item в Blockbench!

                super.renderStackForBone(poseStack, bone, stack, animatable, bufferSource, partialTick, packedLight, packedOverlay);
            }
        });
    }
}