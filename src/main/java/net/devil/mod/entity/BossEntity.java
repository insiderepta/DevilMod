package net.devil.mod.entity;

import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
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

    // ==========================================
    // НАСТРОЙКА БОСС-БАРА
    // ==========================================
    private final ServerBossEvent bossEvent = (ServerBossEvent) (new ServerBossEvent(
            this.getDisplayName(),
            BossEvent.BossBarColor.RED, // Цвет: PINK, BLUE, RED, GREEN, YELLOW, PURPLE, WHITE
            BossEvent.BossBarOverlay.PROGRESS // Стиль: PROGRESS, NOTCHED_6, NOTCHED_10, NOTCHED_12, NOTCHED_20 (деления)
    )).setDarkenScreen(true); // true — небо будет темнеть при битве с боссом
    public BossEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 500;

    }

    // ==========================================
    // ХАРАКТЕРИСТИКИ БОССА
    // ==========================================
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

    // ==========================================
    // ИСКУССТВЕННЫЙ ИНТЕЛЛЕКТ
    // ==========================================
    // ==========================================
    // ИСКУССТВЕННЫЙ ИНТЕЛЛЕКТ
    // ==========================================
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));

        // ДОБАВЛЕНО: Напрыгивание на цель во время атаки.
        // 0.5F — это сила толчка вперед при прыжке.
        this.goalSelector.addGoal(2, new LeapAtTargetGoal(this, 0.4F));

        // Обычная атака теперь будет идти приоритетом чуть ниже (3 вместо 2)
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2D, false));

        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }


    // ==========================================
    // БОСС-БАР
    // ==========================================
    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player); // Показываем полоску, когда игрок подошел
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player); // Убираем полоску, если игрок убежал
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        // Каждую секунду обновляем уровень здоровья на полоске
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
    }

    // ==========================================
    // НАСТРОЙКА АНИМАЦИЙ (GeckoLib 4)
    // ==========================================
    // Не забудь добавить эту переменную в самый верх класса BossEntity:
    // private boolean isAwakened = true; // Пока оставим true, чтобы он сразу ходил. Потом изменим на false.

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Контроллер для базовых движений (ходьба, стояние, смерть)
        controllers.add(new AnimationController<>(this, "movement", 5, this::movementController));

        // Отдельный контроллер для резких действий (атака)
        controllers.add(new AnimationController<>(this, "attack", 0, this::attackController));
    }

    private PlayState movementController(AnimationState<BossEntity> state) {
        /* ======================================================================
           ЗАГОТОВКА ДЛЯ АНИМАЦИИ ПРОБУЖДЕНИЯ (ТРОН)
           Когда сделаем триггер арены, раскомментируем этот кусок:

           if (!this.isAwakened) {
               // Босс сидит на троне и просыпается
               return state.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.devilboss.пробуждение"));
           }
           ====================================================================== */

        // 1. Проверяем, не умер ли босс
        if (this.isDeadOrDying()) {
            return state.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.devilboss.смерть"));
        }

        // 2. Проверяем, идет ли он
        if (state.isMoving()) {
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.devilboss.ходьба"));
        }

        // 3. Если живой и не идет — просто стоит
        return state.setAndContinue(RawAnimation.begin().thenLoop("animation.devilboss.стояние"));
    }

    private PlayState attackController(AnimationState<BossEntity> state) {
        // Если босс замахивается для удара и при этом жив
        if (this.swinging && !this.isDeadOrDying()) {
            return state.setAndContinue(RawAnimation.begin().thenPlay("animation.devilboss.АТАКА"));
        }
        return PlayState.STOP; // В остальное время этот контроллер спит
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    // ==========================================
    // ТРИГГЕР АТАКИ (Чтобы анимация срабатывала)
    // ==========================================
    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        // Заставляем босса "взмахнуть рукой" в коде, что активирует нашу анимацию АТАКИ
        this.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
        return super.doHurtTarget(target);
    }
    // ==========================================
    // УПРАВЛЕНИЕ ВРЕМЕНЕМ СМЕРТИ
    // ==========================================
    @Override
    protected void tickDeath() {
        this.deathTime++;

        // 40 тиков = 2 секунды. Если твоя анимация длиннее, поставь 60 (3 сек) или больше.
        if (this.deathTime == 60 && !this.level().isClientSide()) {
            this.level().broadcastEntityEvent(this, (byte)60); // Спавн частиц смерти
            this.remove(Entity.RemovalReason.KILLED); // Удаление босса из мира
            this.dropExperience(); // Дроп опыта
        }
    }
    // КОРРЕКТИРОВКА ФИЗИКИ ПРЫЖКА
    // ==========================================
    @Override
    protected void jumpFromGround() {
        super.jumpFromGround();
        net.minecraft.world.phys.Vec3 motion = this.getDeltaMovement();
        // Ванильный прыжок — это 0.42F.
        // Для босса х4.5 отлично подойдет значение от 0.65F до 0.85F (он сможет перепрыгивать стены в 2-3 блока)
        this.setDeltaMovement(motion.x, 0.65F, motion.z);
    }
}
