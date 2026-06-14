package net.devil.mod.block;

import net.devil.mod.DevilMod;
import net.devil.mod.entity.BossEntity;
import net.devil.mod.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class BossSpawnerBlockEntity extends BlockEntity {

    public BossSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(DevilMod.BOSS_SPAWNER_BE.get(), pos, state);
    }

    // Вызывается каждый тик
    public static void tick(Level level, BlockPos pos, BlockState state, BossSpawnerBlockEntity entity) {
        // Проверяем игроков только 1 раз в секунду (каждые 20 тиков), чтобы не нагружать сервер
        if (level.getGameTime() % 20 == 0) {
            AABB searchArea = new AABB(pos).inflate(30.0D);
            boolean hasPlayer = !level.getEntitiesOfClass(Player.class, searchArea).isEmpty();

            if (hasPlayer) {
                // ИГРОК НАЙДЕН - Спавним босса
                BossEntity boss = ModEntities.BOSS.get().create(level);
                if (boss != null) {
                    boss.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
                    level.addFreshEntity(boss);
                    level.playSound(null, pos, SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.0F, 1.0F);
                }

                // Удаляем блок
                level.removeBlock(pos, false);
            }
        }
    }
}