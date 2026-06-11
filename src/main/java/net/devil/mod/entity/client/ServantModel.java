package net.devil.mod.entity.client;

import net.devil.mod.DevilMod;
import net.devil.mod.entity.ServantEntity; // Добавили импорт моба
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ServantModel extends GeoModel<ServantEntity> {
    @Override
    public ResourceLocation getModelResource(ServantEntity animatable) {
        return new ResourceLocation(DevilMod.MOD_ID, "geo/servant.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ServantEntity animatable) {
        return new ResourceLocation(DevilMod.MOD_ID, "textures/entity/servant.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ServantEntity animatable) {
        return new ResourceLocation(DevilMod.MOD_ID, "animations/servant.animation.json");
    }
}