package net.devil.mod.item;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.core.Registry;
import net.devil.mod.entity.DemonFanEntity;

public class DemonFanItem extends Item {

    public DemonFanItem(Item.Properties properties) {
        super(properties.fireResistant().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {

            // 1. Получаем базу данных всех структур в мире
            Registry<Structure> structureRegistry = serverLevel.registryAccess().registryOrThrow(Registries.STRUCTURE);

            // ==========================================
            // ВАЖНО: ТВОЯ СТРУКТУРА
            // ==========================================
            // Замени "devilmod" на ID твоего мода, а "boss_arena" на техническое название твоей структуры!
            Structure arenaStructure = structureRegistry.get(ResourceLocation.fromNamespaceAndPath("devilmod", "arena_boss"));

            if (arenaStructure != null) {
                // 2. Готовим структуру для поиска (оборачиваем в технический формат Майнкрафта)
                Holder<Structure> structureHolder = structureRegistry.wrapAsHolder(arenaStructure);
                HolderSet<Structure> targetStructure = HolderSet.direct(structureHolder);

                // 3. Майнкрафт, найди мне ближайшую арену! (Радиус 100 чанков)
                Pair<BlockPos, Holder<Structure>> result = serverLevel.getChunkSource().getGenerator().findNearestMapStructure(
                        serverLevel, targetStructure, player.blockPosition(), 100, false
                );
                if (result != null) {
                    BlockPos arenaPos = result.getFirst();

                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.ENDER_EYE_LAUNCH, SoundSource.NEUTRAL,
                            0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));

                    DemonFanEntity fanEntity = new DemonFanEntity(level, player.getX(), player.getY(0.5D), player.getZ());
                    fanEntity.setItem(itemStack);
                    fanEntity.signalTo(arenaPos);

                    level.addFreshEntity(fanEntity);
                    level.gameEvent(GameEvent.PROJECTILE_SHOOT, player.position(), GameEvent.Context.of(player));

                    // ==========================================
                    // НОВОЕ: АНТИ-ДЮП (Удаляем предмет из рук)
                    // ==========================================
                    if (!player.getAbilities().instabuild) {
                        itemStack.shrink(1);
                    }

                } else {
                    player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cАрена не найдена поблизости..."), true);
                    level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.VILLAGER_NO, SoundSource.NEUTRAL, 1.0F, 1.0F);
                }
            } else {
                System.out.println("ОШИБКА DEVILMOD: Структура с таким именем не зарегистрирована!");
            }
        }

        // ИСПРАВЛЕНО: Возвращаем обновленный стак (он будет пустым, если мы его забрали)
        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }
}
