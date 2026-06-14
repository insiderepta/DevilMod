package net.devil.mod;

import net.devil.mod.block.BossSpawnerBlock;
import net.devil.mod.block.BossSpawnerBlockEntity;
import net.devil.mod.entity.BossEntity;
import net.devil.mod.entity.ModEntities;
import net.devil.mod.entity.ServantEntity;
import net.devil.mod.entity.client.BossRenderer;
import net.devil.mod.entity.client.ServantRenderer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(DevilMod.MOD_ID)
public class DevilMod {
    public static final String MOD_ID = "devilmod";

    // 1. Создаем "списки регистрации" (Deferred Registers) для Forge
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);

    // 2. Регистрируем сам блок-спавнер
    public static final RegistryObject<Block> BOSS_SPAWNER = BLOCKS.register("boss_spawner",
            () -> new BossSpawnerBlock(BlockBehaviour.Properties.copy(Blocks.BEDROCK).noLootTable()));

    // 3. Регистрируем блок как предмет
    public static final RegistryObject<Item> BOSS_SPAWNER_ITEM = ITEMS.register("boss_spawner",
            () -> new BlockItem(BOSS_SPAWNER.get(), new Item.Properties()));

    // ==========================================
    // НОВОЕ: Регистрируем предмет Веера Демона!
    // ==========================================
    public static final RegistryObject<Item> DEMON_FAN = ITEMS.register("demon_fan",
            () -> new net.devil.mod.item.DemonFanItem(new Item.Properties()));

    // Добавь список регистрации для BlockEntities
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MOD_ID);

    // Зарегистрируй сам BlockEntity
    public static final RegistryObject<BlockEntityType<BossSpawnerBlockEntity>> BOSS_SPAWNER_BE = BLOCK_ENTITIES.register("boss_spawner_be",
            () -> BlockEntityType.Builder.of(BossSpawnerBlockEntity::new, BOSS_SPAWNER.get()).build(null));

    public static final RegistryObject<Item> DEVIL_CORE = ITEMS.register("devil_core",
            () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC)));
    // Добавил EPIC редкость, чтобы название предмета светилось фиолетовым цветом!
    public DevilMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        // 4. Включаем регистрацию блоков и предметов в игру
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);

        // Регистрируем самих мобов
        ModEntities.register(modEventBus);

        // Регистрируем их характеристики (здоровье, урон)
        modEventBus.addListener(this::registerAttributes);

        // Регистрируем их внешность (Только для клиента!)
        modEventBus.addListener(this::onClientSetup);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.BOSS.get(), BossEntity.createAttributes().build());
        event.put(ModEntities.SERVANT.get(), ServantEntity.createAttributes().build());
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        // Привязываем рендерер к нашему Боссу
        EntityRenderers.register(ModEntities.BOSS.get(), BossRenderer::new);

        // Привязываем рендерер к Слуге
        EntityRenderers.register(ModEntities.SERVANT.get(), ServantRenderer::new);

        // ==========================================
        // НОВОЕ: Рендерер для летящего Веера
        // ==========================================
        // ThrownItemRenderer берет текстуру предмета (demon_fan.png) и рисует ее в полете
        EntityRenderers.register(ModEntities.DEMON_FAN.get(), net.minecraft.client.renderer.entity.ThrownItemRenderer::new);
    }
}