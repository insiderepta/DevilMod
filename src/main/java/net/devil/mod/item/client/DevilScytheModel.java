package net.devil.mod.item.client;

import net.devil.mod.DevilMod;
import net.devil.mod.item.DevilScytheItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class DevilScytheModel extends GeoModel<DevilScytheItem> {
    @Override
    public ResourceLocation getModelResource(DevilScytheItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(DevilMod.MOD_ID, "geo/devil_scythe.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DevilScytheItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(DevilMod.MOD_ID, "textures/item/devil_scythe.png");
    }

    @Override
    public ResourceLocation getAnimationResource(DevilScytheItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(DevilMod.MOD_ID, "animations/devil_scythe.animation.json");
    }
}
