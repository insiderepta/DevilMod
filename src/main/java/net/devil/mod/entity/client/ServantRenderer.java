package net.devil.mod.entity.client;

import net.devil.mod.entity.ServantEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer; // Импортируем слой свечения

public class ServantRenderer extends GeoEntityRenderer<ServantEntity> {
    public ServantRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ServantModel());

        // ВОТ ЭТА МАГИЧЕСКАЯ СТРОЧКА:
        this.addRenderLayer(new ServantEyesLayer(this));
    }
}