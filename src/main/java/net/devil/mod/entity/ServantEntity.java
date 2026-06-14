package net.devil.mod.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal; // НОВЫЙ ИМПОРТ
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal; // НОВЫЙ ИМПОРТ
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
 import software.bernie.geckolib.core.animation.RawAnimation;
 import software.bernie.geckolib.core.object.PlayState;

public class ServantEntity extends Monster implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private boolean isBossMinion = false;
    private int minionTimer = 100;

    public ServantEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D) // Базовая скорость (быстрый шаг)
                .add(Attributes.ATTACK_DAMAGE, 3.0D);
    }

    // ==========================================
    // ПРЕВРАЩЕНИЕ В БОЕВОГО МИНЬОНА
    // ==========================================
    public void setAsBossMinion(LivingEntity target) {
        this.isBossMinion = true;
        this.minionTimer = 200;

        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_SWORD));
        this.setInvulnerable(true);

        // ИСПРАВЛЕНО: Добавляем нормальные боевые инстинкты!
        // Priority 2 - Атака
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.3D, false));
        // Priority 1 - Поиск цели (он сам будет искать игрока, нам не нужно заставлять его кодом)
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));

        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(12.0D);

        this.setPathfindingMalus(net.minecraft.world.level.pathfinder.BlockPathTypes.LAVA, 0.0F);
        this.setPathfindingMalus(net.minecraft.world.level.pathfinder.BlockPathTypes.DAMAGE_FIRE, 0.0F);

        if (target != null) {
            this.setTarget(target);
        }
    }

    @Override
    public boolean fireImmune() {
        return this.isBossMinion ? true : super.fireImmune();
    }

    @Override
    public boolean canStandOnFluid(net.minecraft.world.level.material.FluidState fluidState) {
        if (this.isBossMinion && fluidState.is(net.minecraft.tags.FluidTags.LAVA)) {
            return true;
        }
        return super.canStandOnFluid(fluidState);
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt && this.isBossMinion && this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0D, target.getZ(), 20, 0.4D, 0.4D, 0.4D, 0.2D);
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.HOSTILE, 1.0F, 1.0F);
        }
        return hurt;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new software.bernie.geckolib.core.animation.AnimationController<>(this, "movement", 5, state -> {
            // Если моб движется (бежит или идет)
            if (state.isMoving()) {
                // ВАЖНО: Замени "animation.servant.run" на точное название твоей анимации из Blockbench!
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.servant.walk"));
            }
            // Если стоит на месте, анимация останавливается (или можешь добавить сюда idle-анимацию)
            return PlayState.STOP;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return 1;
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {

            if (this.isBossMinion) {
                this.minionTimer--;

                // ИСПРАВЛЕНО: Убрали сломанный setTarget каждый тик. ИИ теперь рулит сам.

                if (this.minionTimer <= 0) {
                    if (this.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.POOF, this.getX(), this.getY() + 1.0D, this.getZ(), 20, 0.3D, 0.5D, 0.3D, 0.02D);
                    }
                    this.discard();
                }
                return;
            }

            // Старая логика наблюдателя
            Player closestPlayer = this.level().getNearestPlayer(this, 64.0D);
            if (closestPlayer != null) {
                this.getLookControl().setLookAt(closestPlayer, 30.0F, 30.0F);

                if (this.tickCount == 1) {
                    if (closestPlayer instanceof ServerPlayer serverPlayer) {
                        Advancement advancement = serverPlayer.server.getAdvancements()
                                .getAdvancement(new net.minecraft.resources.ResourceLocation("devilmod", "devil_sees_you"));
                        if (advancement != null) {
                            AdvancementProgress progress = serverPlayer.getAdvancements().getOrStartProgress(advancement);
                            if (!progress.isDone()) {
                                for (String criterion : progress.getRemainingCriteria()) {
                                    serverPlayer.getAdvancements().award(advancement, criterion);
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

            if (this.tickCount > 60) {
                Player playerTooClose = this.level().getNearestPlayer(this, 12.0D);
                if (playerTooClose != null || this.tickCount > 300) {
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0F, 1.0F);
                    if (this.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.POOF, this.getX(), this.getY() + 1.0D, this.getZ(), 20, 0.3D, 0.5D, 0.3D, 0.02D);
                    }
                    this.discard();
                }
            }
        }
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        // ИСПРАВЛЕНО: Добавлен FloatGoal, чтобы он не тонул.
        this.goalSelector.addGoal(0, new FloatGoal(this));

        // ИСПРАВЛЕНО: Понижен приоритет взгляда с 1 до 8! Теперь атака важнее разглядывания игрока.
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 64.0F, 1.0F));
    }

    public static boolean checkServantSpawnRules(EntityType<ServantEntity> entityType, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        if (!Monster.checkMonsterSpawnRules(entityType, level, spawnType, pos, random)) {
            return false;
        }
        AABB searchBox = new AABB(pos).inflate(200.0D);
        List<ServantEntity> nearbyServants = level.getEntitiesOfClass(ServantEntity.class, searchBox);
        return nearbyServants.isEmpty();
    }
}