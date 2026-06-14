package net.devil.mod.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.devil.mod.DevilMod;
import net.devil.mod.entity.BossEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class BossRenderer extends GeoEntityRenderer<BossEntity> {

    // Храним текущую активную музыку здесь
    private static BossMusicInstance activeMusic = null;

    public BossRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new BossModel());
        this.shadowRadius = 0.7F;
        this.addRenderLayer(new BossEyesLayer(this));
    }

    @Override
    protected float getDeathMaxRotation(BossEntity animatable) {
        return 0.0F;
    }

    @Override
    public void preRender(PoseStack poseStack, BossEntity animatable, BakedGeoModel model,
                          MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender,
                          float partialTick, int packedLight, int packedOverlay,
                          float red, float green, float blue, float alpha) {

        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender,
                partialTick, packedLight, packedOverlay, red, green, blue, alpha);

        if (!isReRender) {
            poseStack.scale(4.5F, 4.5F, 4.5F);
        }

        // ==========================================
        // ЛОГИКА ДИНАМИЧЕСКОЙ МУЗЫКИ БОССА
        // ==========================================
        Minecraft mc = Minecraft.getInstance();

        // Если босс полностью жив и здоров
        if (animatable.isAlive() && !animatable.isDeadOrDying()) {
            // Если музыка еще не создана ИЛИ создана, но уже завершилась/остановилась
            if (activeMusic == null || !mc.getSoundManager().isActive(activeMusic)) {

                // Создаем событие звука напрямую по его ID из sounds.json
                SoundEvent bossSound = SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(DevilMod.MOD_ID, "boss_theme"));

                // Создаем наш тикающий экземпляр музыки и запускаем его
                activeMusic = new BossMusicInstance(bossSound, animatable);
                mc.getSoundManager().play(activeMusic);
            }
        }
    }
}