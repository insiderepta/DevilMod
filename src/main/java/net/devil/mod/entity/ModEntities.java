package net.devil.mod.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, "devilmod");

    // Регистрация Прислуги
    public static final RegistryObject<EntityType<ServantEntity>> SERVANT =
            ENTITY_TYPES.register("servant", () -> EntityType.Builder.of(ServantEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .build("servant"));

    public static final RegistryObject<EntityType<BossEntity>> BOSS =
            ENTITY_TYPES.register("boss", () -> EntityType.Builder.of(BossEntity::new, MobCategory.MONSTER)
                    .sized(1.2F, 3.0F) // 1.2 блока в ширину, 3.0 блока в высоту
                    .build("boss"));

    public static final RegistryObject<EntityType<PlayerServantEntity>> PLAYER_SERVANT =
            ENTITY_TYPES.register("player_servant", () -> EntityType.Builder.of(PlayerServantEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .build("player_servant"));

    public static final RegistryObject<EntityType<DemonFanEntity>> DEMON_FAN =
            ENTITY_TYPES.register("demon_fan",
                    () -> EntityType.Builder.<DemonFanEntity>of(DemonFanEntity::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F) // Размер как у эндер-пёрла
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build("demon_fan"));


    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}