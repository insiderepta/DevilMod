package net.devil.mod.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.EyeOfEnder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.item.ItemEntity;

public class DemonFanEntity extends EyeOfEnder {

    // Конструктор для Майнкрафта (при регистрации)
    public DemonFanEntity(EntityType<? extends net.minecraft.world.entity.projectile.EyeOfEnder> type, net.minecraft.world.level.Level level) {
        super(type, level);
    }

    // Конструктор для нас (когда игрок кидает предмет)
    public DemonFanEntity(net.minecraft.world.level.Level level, double x, double y, double z) {
        // ИСПРАВЛЕНО: Указываем наш зарегистрированный тип!
        super(net.devil.mod.entity.ModEntities.DEMON_FAN.get(), level);
        this.setPos(x, y, z);
    }

    // ==========================================
    // АНТИ-ДЮП БЕСКОНЕЧНОГО ПРЕДМЕТА
    // ==========================================
    @Override
    public ItemEntity spawnAtLocation(ItemStack stack, float offset) {
        // Око Края имеет шанс выпасть как предмет после броска.
        // Так как наш веер бесконечный и остается в руке, летящий снаряд
        // просто исчезнет, ничего не выбрасывая.
        return null;
    }
}