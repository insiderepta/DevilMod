
        package net.devil.mod;

import net.devil.mod.entity.BossEntity;
import net.devil.mod.entity.ModEntities;
import net.devil.mod.entity.ServantEntity;
import net.devil.mod.entity.client.BossRenderer;
import net.devil.mod.entity.client.ServantRenderer; // Оставь, если есть
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent; // ВАЖНЫЙ ИМПОРТ
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

        @Mod(DevilMod.MOD_ID)
        public class DevilMod {
            public static final String MOD_ID = "devilmod";

            public DevilMod() {
                IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

                // 1. Регистрируем самих мобов
                ModEntities.register(modEventBus);

                // 2. Жестко регистрируем их характеристики (здоровье, урон)
                modEventBus.addListener(this::registerAttributes);

                // 3. Жестко регистрируем их внешность (Только для клиента!)
                modEventBus.addListener(this::onClientSetup);

                MinecraftForge.EVENT_BUS.register(this);
            }

            private void registerAttributes(EntityAttributeCreationEvent event) {
                event.put(ModEntities.BOSS.get(), BossEntity.createAttributes().build());
                event.put(ModEntities.SERVANT.get(), ServantEntity.createAttributes().build()); // Раскомментируй для Слуги
            }

            // НОВЫЙ МЕТОД: Говорит игре, какими классами рисовать мобов
            private void onClientSetup(final FMLClientSetupEvent event) {
                // Привязываем рендерер к нашему Боссу
                EntityRenderers.register(ModEntities.BOSS.get(), BossRenderer::new);

                // Привязываем рендерер к Слуге (если он есть)
                EntityRenderers.register(ModEntities.SERVANT.get(), ServantRenderer::new);
            }
        }

