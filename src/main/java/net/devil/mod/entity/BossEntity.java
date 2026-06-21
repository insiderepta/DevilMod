package net.devil.mod.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class BossEntity extends Monster implements GeoEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // Синхронизация спавна
    private static final EntityDataAccessor<Boolean> IS_AWAKENED = SynchedEntityData.defineId(BossEntity.class, EntityDataSerializers.BOOLEAN);
    private int awakenTimer = 40;

    // Способность: ЛАВА
    private static final EntityDataAccessor<Boolean> IS_CASTING_LAVA = SynchedEntityData.defineId(BossEntity.class, EntityDataSerializers.BOOLEAN);
    private int lavaCooldown = 300; // Для тестов 15 сек.
    private int lavaCastTimer = 0;
    private BlockPos activeLavaPos = null;
    private int activeLavaTimer = 0;

    // ==========================================
    // НОВОЕ: ПРИЗЫВ СЛУГИ (CONSCRIPT)
    // ==========================================
    private static final EntityDataAccessor<Boolean> IS_CASTING_CONSCRIPT = SynchedEntityData.defineId(BossEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_CONSCRIPT_DOWN = SynchedEntityData.defineId(BossEntity.class, EntityDataSerializers.BOOLEAN);
    private int conscriptCooldown = 1200; // Кулдаун 60 секунд
    private int conscriptCastTimer = 0;
    private final java.util.List<ServantEntity> activeServants = new java.util.ArrayList<>();

    // ==========================================
    // НОВОЕ: РЫВОК
    // ==========================================
    private static final EntityDataAccessor<Boolean> IS_DASHING = SynchedEntityData.defineId(BossEntity.class, EntityDataSerializers.BOOLEAN);
    private int dashCooldown = 400; // Кулдаун рывка (5 секунд)

    // ВАЖНО: Настрой эту цифру под длину твоей анимации animation.devilboss.jerk!
    // Сейчас стоит 15 тиков (0.75 секунды).
    private int dashTimer = 32;

    private static final net.minecraft.network.syncher.EntityDataAccessor<Boolean> IS_CASTING_SPLASH =
            net.minecraft.network.syncher.SynchedEntityData.defineId(BossEntity.class, net.minecraft.network.syncher.EntityDataSerializers.BOOLEAN);
    private int splashCooldown = 60; // Кулдаун 25 секунд
    private int splashCastTimer = 0;

    // ==========================================
    // НАСТРОЙКА БОСС-БАРА
    // ==========================================
    private final ServerBossEvent bossEvent = (ServerBossEvent) (new ServerBossEvent(
            this.getDisplayName(),
            BossEvent.BossBarColor.RED,
            BossEvent.BossBarOverlay.PROGRESS
    )).setDarkenScreen(true);

    public BossEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 500;

        this.setPathfindingMalus(net.minecraft.world.level.pathfinder.BlockPathTypes.LAVA, 0.0F);
        this.setPathfindingMalus(net.minecraft.world.level.pathfinder.BlockPathTypes.DAMAGE_FIRE, 0.0F);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_AWAKENED, false);
        this.entityData.define(IS_CASTING_LAVA, false);
        this.entityData.define(IS_DASHING, false); // По умолчанию рывок выключен
        this.entityData.define(IS_CASTING_CONSCRIPT, false);
        this.entityData.define(IS_CONSCRIPT_DOWN, false);
        this.entityData.define(IS_CASTING_SPLASH, false);
    }

    public boolean isAwakened() { return this.entityData.get(IS_AWAKENED); }
    public void setAwakened(boolean awakened) { this.entityData.set(IS_AWAKENED, awakened); }

    public boolean isCastingLava() { return this.entityData.get(IS_CASTING_LAVA); }
    public void setCastingLava(boolean casting) { this.entityData.set(IS_CASTING_LAVA, casting); }

    // Геттеры/Сеттеры для рывка
    public boolean isDashing() { return this.entityData.get(IS_DASHING); }
    public void setDashing(boolean dashing) { this.entityData.set(IS_DASHING, dashing); }

    public boolean isCastingConscript() { return this.entityData.get(IS_CASTING_CONSCRIPT); }
    public void setCastingConscript(boolean casting) { this.entityData.set(IS_CASTING_CONSCRIPT, casting); }

    public boolean isConscriptDown() { return this.entityData.get(IS_CONSCRIPT_DOWN); }
    public void setConscriptDown(boolean down) { this.entityData.set(IS_CONSCRIPT_DOWN, down); }

    public boolean isCastingSplash() { return this.entityData.get(IS_CASTING_SPLASH); }
    public void setCastingSplash(boolean casting) { this.entityData.set(IS_CASTING_SPLASH, casting); }

    @Override
    public boolean fireImmune() {
        return true;
    }

    // ХОЖДЕНИЕ ПО ПОВЕРХНОСТИ ЛАВЫ
    @Override
    public boolean canStandOnFluid(net.minecraft.world.level.material.FluidState fluidState) {
        return fluidState.is(net.minecraft.tags.FluidTags.LAVA);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 350.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.ATTACK_DAMAGE, 20.0D)
                .add(Attributes.ARMOR, 50.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 4.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 40.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        // Прыжок (LeapAtTargetGoal) удален
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2D, false));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

        // 1. АУРА СТРАХА
        for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(40.0D))) {
            if (entity != this && !(entity instanceof ServantEntity) && entity instanceof net.minecraft.world.entity.monster.Enemy) {
                entity.discard();
            }
        }

        // 2. ПРОБУЖДЕНИЕ
        if (!this.isAwakened()) {
            this.awakenTimer--;
            this.getNavigation().stop();
            this.setTarget(null);
            if (this.awakenTimer <= 0) {
                this.setAwakened(true);
            }
        }
        else if (!this.isDeadOrDying()) {

            // ==========================================
            // ВЫПОЛНЕНИЕ АКТИВНЫХ СПОСОБНОСТЕЙ
            // ==========================================

            // 3. ВСПЛЕСК ТЬМЫ (SPLASH) - Выполняется первым
            if (this.isCastingSplash()) {
                this.splashCastTimer--;
                this.getNavigation().stop();

                // Накладываем эффекты на 50-м тике анимации
                if (this.splashCastTimer == 50) {
                    java.util.List<Player> players = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(30.0D));

                    for (Player p : players) {
                        p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DARKNESS, 300, 0));
                        p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 300, 0));
                        p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WEAKNESS, 300, 0));
                    }

                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                            net.minecraft.sounds.SoundEvents.TOTEM_USE, net.minecraft.sounds.SoundSource.HOSTILE, 1.0F, 0.5F);
                }

                if (this.splashCastTimer <= 0) {
                    this.setCastingSplash(false);
                }
            }
// 4. ПРИЗЫВ (CONSCRIPT) - Анализатор игроков
            else if (this.isCastingConscript()) {
                this.getNavigation().stop();
                if (!this.isConscriptDown()) {
                    this.conscriptCastTimer++;
                    if (this.conscriptCastTimer == 34) {
                        // Очищаем старый список на всякий случай
                        this.activeServants.clear();

                        // Собираем всех игроков в радиусе 50 блоков
                        java.util.List<Player> fightingPlayers = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(50.0D));

                        // Спавним по 1 прислуге для КАЖДОГО игрока
                        for (Player targetPlayer : fightingPlayers) {
                            ServantEntity servant = net.devil.mod.entity.ModEntities.SERVANT.get().create(this.level());
                            if (servant != null) {
                                // Добавляем небольшой случайный разброс координат, чтобы они не спавнились в одной точке
                                double spawnX = this.getX() + (this.random.nextDouble() - 0.5D) * 4.0D;
                                double spawnZ = this.getZ() + (this.random.nextDouble() - 0.5D) * 4.0D;

                                servant.moveTo(spawnX, this.getY(), spawnZ, this.getYRot(), 0.0F);
                                servant.setAsBossMinion(targetPlayer); // Натравливаем конкретно на этого игрока
                                this.level().addFreshEntity(servant);

                                this.activeServants.add(servant); // Запоминаем созданного слугу
                            }
                        }

                        // Если игроков не было вообще, сразу сворачиваем способность
                        if (this.activeServants.isEmpty()) {
                            this.setConscriptDown(true);
                            this.conscriptCastTimer = 0;
                        }

                    } else if (this.conscriptCastTimer > 34) {
                        if (this.tickCount % 10 == 0) {
                            this.heal(1.0F); // Хил босса пока живы слуги
                        }

                        // Удаляем из списка мертвых или пропавших слуг
                        this.activeServants.removeIf(s -> s == null || !s.isAlive() || s.isRemoved());

                        // Проверяем, убиты ли ВСЕ слуги
                        if (this.activeServants.isEmpty()) {
                            this.setConscriptDown(true);
                            this.conscriptCastTimer = 0;
                        }
                    }
                } else {
                    this.conscriptCastTimer++;
                    if (this.conscriptCastTimer >= 30) {
                        this.setCastingConscript(false);
                    }
                }
            }
            // 5. ЛАВА
            else if (this.isCastingLava()) {
                this.lavaCastTimer--;
                this.getNavigation().stop();

                if (this.lavaCastTimer == 30) {
                    Player targetPlayer = this.level().getNearestPlayer(this, 50.0D);
                    if (targetPlayer != null) {
                        BlockPos targetPos = targetPlayer.blockPosition();
                        if (this.level().getBlockState(targetPos).canBeReplaced()) {
                            this.level().setBlock(targetPos, Blocks.LAVA.defaultBlockState(), 3);
                            this.activeLavaPos = targetPos;
                            this.activeLavaTimer = 100;
                        }
                    }
                }

                if (this.lavaCastTimer <= 0) {
                    this.setCastingLava(false);
                }
            }
            // 6. РЫВОК
            else if (this.isDashing()) {
                this.dashTimer--;
                this.getNavigation().stop();

                Player targetPlayer = this.level().getNearestPlayer(this, 50.0D);
                if (targetPlayer != null) {
                    this.lookAt(targetPlayer, 30.0F, 30.0F);

                    if (this.dashTimer < 12) {
                        if (this.distanceToSqr(targetPlayer) > 3.0D) {
                            net.minecraft.world.phys.Vec3 moveDir = targetPlayer.position().subtract(this.position()).normalize().scale(1.5D);
                            this.setDeltaMovement(moveDir.x, this.getDeltaMovement().y, moveDir.z);
                        } else {
                            this.setDeltaMovement(0, this.getDeltaMovement().y, 0);
                        }
                        
                        for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(1.5D))) {
                            if (entity != this && !(entity instanceof ServantEntity) && entity instanceof Player) {
                                entity.hurt(this.damageSources().mobAttack(this), 8.0F);
                            }
                        }
                    } else {
                        this.setDeltaMovement(0, this.getDeltaMovement().y, 0);
                    }
                }

                if (this.dashTimer <= 0) {
                    this.setDashing(false);
                }
            }

            // ==========================================
            // 7. ВЫБОР СЛЕДУЮЩЕЙ АТАКИ (ПРИОРИТЕТЫ)
            // ==========================================
            else {
                // Отнимаем время кулдаунов
                if (this.splashCooldown > 0) this.splashCooldown--;
                if (this.conscriptCooldown > 0) this.conscriptCooldown--;
                if (this.lavaCooldown > 0) this.lavaCooldown--;
                if (this.dashCooldown > 0) this.dashCooldown--;

                Player targetPlayer = this.level().getNearestPlayer(this, 50.0D);
                if (targetPlayer != null) {
                    double distance = this.distanceToSqr(targetPlayer);

                    // ПРИОРИТЕТ 1: Сплеш тьмы (Самый редкий)
                    if (this.splashCooldown <= 0) {
                        this.setCastingSplash(true);
                        this.splashCastTimer = 104;
                        this.splashCooldown = 500; // Сразу сбрасываем кулдаун на 25 секунд
                    }
                    // ПРИОРИТЕТ 2: Призыв Слуги (Только если сплеш на кулдауне)
                    else if (this.conscriptCooldown <= 0) {
                        if (this.random.nextInt(100) < 15) {
                            this.setCastingConscript(true);
                            this.setConscriptDown(false);
                            this.conscriptCastTimer = 0;
                            this.conscriptCooldown = 1200; // 60 секунд
                            this.activeServants.clear();
                        }
                    }
                    // ПРИОРИТЕТ 3: Лава
                    else if (this.lavaCooldown <= 0) {
                        this.setCastingLava(true);
                        this.lavaCastTimer = 60;
                        this.lavaCooldown = 150; // Сразу сбрасываем кулдаун на 7.5 секунд
                    }
                    // ПРИОРИТЕТ 4: Рывок
                    else if (this.dashCooldown <= 0 && distance > 36.0D && distance < 225.0D) {
                        if (this.random.nextInt(100) < 5) {
                            this.setDashing(true);
                            this.dashTimer = 31;
                            this.dashCooldown = 100; // Сразу сбрасываем кулдаун на 5 секунд
                        }
                    }
                }   
            }
        }

        // 8. ТАЙМЕР ИСЧЕЗНОВЕНИЯ ЛАВЫ
        if (this.activeLavaTimer > 0) {
            this.activeLavaTimer--;
            if (this.activeLavaTimer <= 0 && this.activeLavaPos != null) {
                if (this.level().getBlockState(this.activeLavaPos).is(Blocks.LAVA)) {
                    this.level().setBlock(this.activeLavaPos, Blocks.AIR.defaultBlockState(), 3);
                }
                this.activeLavaPos = null;
            }
        }
    }

    @Override
    public void travel(net.minecraft.world.phys.Vec3 travelVector) {
        // ИСПРАВЛЕНО: Добавили this.isCastingSplash()
        if (!this.isAwakened() || this.isCastingLava() || this.isCastingConscript() || this.isCastingSplash()) {
            super.travel(net.minecraft.world.phys.Vec3.ZERO);
            return;
        }
        super.travel(travelVector);
    }

    // ==========================================
    // ИСПРАВЛЕННАЯ НАСТРОЙКА АНИМАЦИЙ (GeckoLib 4)
    // ==========================================
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Контроллер №1: Только для обычного перемещения (ходьба, idle, спавн, смерть)
        controllers.add(new AnimationController<>(this, "movement", 5, this::movementController));

        // Контроллер №2: Боевой (высший приоритет). Он будет перекрывать ходьбу во время спецприемов!
        controllers.add(new AnimationController<>(this, "action", 2, this::actionController));
    }

    // Этот контроллер теперь отвечает ТОЛЬКО за базовые состояния тела
    private PlayState movementController(AnimationState<BossEntity> state) {
        if (!this.isAwakened()) {
            return state.setAndContinue(RawAnimation.begin().thenPlay("animation.devilboss.spawn"));
        }

        if (this.isDeadOrDying()) {
            return state.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.devilboss.death"));
        }

        // Если босс кастует или делает рывок, базовый контроллер просто "засыпает", отдавая контроль боевому
        if (this.isCastingLava() || this.isDashing() || this.isCastingConscript()) {
            return PlayState.STOP;
        }

        if (state.isMoving()) {
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.devilboss.walk"));
        }

        return state.setAndContinue(RawAnimation.begin().thenLoop("animation.devilboss.idle"));
    }

    // ИСПРАВЛЕНО: Этот контроллер управляет лавой, рывком и обычными ударами
    private PlayState actionController(AnimationState<BossEntity> state) {
        // 1. Приоритет рывка (вызовется сразу, как только dashTimer станет активным, независимо от скорости движения)
        if (this.isDashing()) {
            return state.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.devilboss.jerk"));
        }

        // 2. Приоритет лавы
        if (this.isCastingLava()) {
            return state.setAndContinue(RawAnimation.begin().thenPlay("animation.devilboss.lava"));
        }

        // 3. Обычный удар рукой (в ближнем бою)
        if (this.swinging && !this.isDeadOrDying() && this.isAwakened()) {
            return state.setAndContinue(RawAnimation.begin().thenPlay("animation.devilboss.attack"));
        }
        // Приоритет призыва
        if (this.isCastingConscript()) {
            if (this.isConscriptDown()) {
                return state.setAndContinue(RawAnimation.begin().thenPlay("animation.devilboss.conscriptdown"));
            } else {
                return state.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.devilboss.conscriptup"));
            }
        }
        // Приоритет Сплеша
        if (this.isCastingSplash()) {
            return state.setAndContinue(software.bernie.geckolib.core.animation.RawAnimation.begin().thenPlay("animation.devilboss.splash"));
        }

        return PlayState.STOP;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (this.isCastingLava()) return false;

        this.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
        return super.doHurtTarget(target);
    }

    @Override
    protected void tickDeath() {
        this.deathTime++;
        if (this.deathTime == 60 && !this.level().isClientSide()) {
            this.level().broadcastEntityEvent(this, (byte)60);
            this.remove(Entity.RemovalReason.KILLED);
            this.dropExperience();
        }
    }

    @Override
    public void die(net.minecraft.world.damagesource.DamageSource cause) {
        if (this.activeLavaPos != null && this.level().getBlockState(this.activeLavaPos).is(Blocks.LAVA)) {
            this.level().setBlock(this.activeLavaPos, Blocks.AIR.defaultBlockState(), 3);
        }
        super.die(cause);
    }
    @Override
    protected void dropCustomDeathLoot(net.minecraft.world.damagesource.DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);

        // Спавним 1 Ядро дьявола на координатах смерти босса
        this.spawnAtLocation(net.devil.mod.DevilMod.DEVIL_CORE.get());
    }
}