package net.devil.mod.entity.client;

import net.devil.mod.DevilMod;
import net.devil.mod.entity.PlayerServantEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class PlayerServantModel extends GeoModel<PlayerServantEntity> {
    @Override
    public ResourceLocation getModelResource(PlayerServantEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(DevilMod.MOD_ID, "geo/servant.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PlayerServantEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(DevilMod.MOD_ID, "textures/entity/servant.png");
    }

    @Override
    public ResourceLocation getAnimationResource(PlayerServantEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(DevilMod.MOD_ID, "animations/servant.animation.json");
    }
}
