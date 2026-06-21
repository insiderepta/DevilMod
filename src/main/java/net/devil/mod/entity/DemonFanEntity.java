package net.devil.mod.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.EyeOfEnder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.item.ItemEntity;

public class DemonFanEntity extends EyeOfEnder {

    public DemonFanEntity(EntityType<? extends EyeOfEnder> type, Level level) {
        super(type, level);
    }

    public DemonFanEntity(Level level, double x, double y, double z) {
        super(net.devil.mod.entity.ModEntities.DEMON_FAN.get(), level);
        this.setPos(x, y, z);
    }

    // ==========================================
    // БЛОКИРОВКА СТАНДАРТНОГО ДРОПА
    // ==========================================
    @Override
    public ItemEntity spawnAtLocation(ItemStack stack, float offset) {
        return null; // Как ты и просил, возвращаем null, чтобы избежать лишних дюпов
    }
}