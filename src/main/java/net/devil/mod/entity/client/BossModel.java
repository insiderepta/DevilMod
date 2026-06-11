package net.devil.mod.entity.client;

import net.devil.mod.DevilMod;
import net.devil.mod.entity.BossEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BossModel extends GeoModel<BossEntity> {
    @Override
    public ResourceLocation getModelResource(BossEntity animatable) {
        // Путь к файлу геометрии .geo.json
        return new ResourceLocation(DevilMod.MOD_ID, "geo/boss.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BossEntity animatable) {
        // Путь к текстуре .png
        return new ResourceLocation(DevilMod.MOD_ID, "textures/entity/boss.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BossEntity animatable) {
        // Путь к файлу анимаций .animation.json
        return new ResourceLocation(DevilMod.MOD_ID, "animations/boss.animation.json");
    }
}
