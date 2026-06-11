package net.devil.mod;

import net.devil.mod.entity.BossEntity;
import net.devil.mod.entity.ModEntities;
import net.devil.mod.entity.ServantEntity; // Оставь это, если у тебя есть Слуга
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.devil.mod.entity.client.BossRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.client.event.EntityRenderersEvent;

// ВАЖНО: Используем DevilMod.MOD_ID, чтобы точно попасть в мод!
@Mod.EventBusSubscriber(modid = DevilMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventBusEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Внутри только такие вызовы:
        event.registerEntityRenderer(ModEntities.BOSS.get(), BossRenderer::new);
    }
}