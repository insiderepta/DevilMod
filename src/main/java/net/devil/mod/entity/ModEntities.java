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

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}