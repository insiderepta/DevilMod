package net.devil.mod.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.particles.ParticleTypes;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import java.util.List;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.server.level.ServerLevel;

public class ServantEntity extends Monster implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // Конструктор
    public ServantEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    // Атрибуты
    public static AttributeSupplier.Builder createAttributes() {
        // Заменили PathfinderMob на Monster
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.15D);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
    @Override
    public int getMaxSpawnClusterSize() {
        return 1; // Строго 1 моб за одну попытку спавна!
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            // 1. Логика взгляда на игрока (Работает идеально)
            Player closestPlayer = this.level().getNearestPlayer(this, 64.0D);
            if (closestPlayer != null) {
                this.getLookControl().setLookAt(closestPlayer, 30.0F, 30.0F);

                if (this.tickCount == 1) {
                    System.out.println("SERVANT ADVANCEMENT TRIGGERED");
                    System.out.println(
                            "SERVANT SPAWNED X=" + this.getX()
                                    + " Y=" + this.getY()
                                    + " Z=" + this.getZ()
                    );
                    if (closestPlayer instanceof ServerPlayer serverPlayer) {

                        Advancement advancement =
                                serverPlayer.server.getAdvancements()
                                        .getAdvancement(new net.minecraft.resources.ResourceLocation(
                                                "devilmod",
                                                "devil_sees_you"));

                        if (advancement != null) {
                            AdvancementProgress progress =
                                    serverPlayer.getAdvancements().getOrStartProgress(advancement);

                            if (!progress.isDone()) {
                                for (String criterion : progress.getRemainingCriteria()) {
                                    serverPlayer.getAdvancements()
                                            .award(advancement, criterion);
                                }
                            }
                        }
                    }
                    double d0 = closestPlayer.getX() - this.getX();
                    double d1 = closestPlayer.getZ() - this.getZ();
                    float yaw = (float)(Math.atan2(d1, d0) * (180.0D / Math.PI)) - 90.0F;
                    this.setYRot(yaw);
                    this.setYHeadRot(yaw);
                    this.setYBodyRot(yaw);
                }
            }

            // 2. Таймер жизни и исчезновения вблизи (Тоже работает отлично)
            if (this.tickCount > 60) {
                Player playerTooClose = this.level().getNearestPlayer(this, 12.0D);
                if (playerTooClose != null || this.tickCount > 300) {

                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0F, 1.0F);

                    if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.POOF,
                                this.getX(), this.getY() + 1.0D, this.getZ(),
                                20, 0.3D, 0.5D, 0.3D, 0.02D);
                    }

                    this.discard(); // Спокойно исчезаем сами, никого не трогая
                }
            }
        }
    }
    @Override
    protected void registerGoals() {
        super.registerGoals();
        // 1 - это приоритет.
        // Player.class - на кого смотреть.
        // 64.0F - радиус (в блоках), с которого он начнет на тебя пялиться.
        // 1.0F - вероятность (100% времени будет смотреть).
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 64.0F, 1.0F));
    }
    public static boolean checkServantSpawnRules(EntityType<ServantEntity> entityType, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        // 1. Проверяем стандартные условия монстров (свет, твердый блок под ногами)
        if (!Monster.checkMonsterSpawnRules(entityType, level, spawnType, pos, random)) {
            return false;
        }

        // 2. Ищем других Слуг в гигантском радиусе 200 блоков
        AABB searchBox = new AABB(pos).inflate(200.0D);
        List<ServantEntity> nearbyServants = level.getEntitiesOfClass(ServantEntity.class, searchBox);

        // Если в этом районе УЖЕ КТО-ТО ЕСТЬ, метод возвращает false,
        // и Майнкрафт просто отменяет спавн, не ломая моб-кап!
        return nearbyServants.isEmpty();
    }
}