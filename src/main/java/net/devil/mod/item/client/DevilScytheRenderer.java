package net.devil.mod.item.client;

import net.devil.mod.item.DevilScytheItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class DevilScytheRenderer extends GeoItemRenderer<DevilScytheItem> {
    public DevilScytheRenderer() {
        super(new DevilScytheModel());
    }
}
