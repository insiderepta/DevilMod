package net.devil.mod.entity.client;

import net.devil.mod.entity.BossEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public class BossMusicInstance extends AbstractTickableSoundInstance {
    private final BossEntity boss;

    public BossMusicInstance(SoundEvent soundEvent, BossEntity boss) {
        super(soundEvent, SoundSource.MUSIC, RandomSource.create());
        this.boss = boss;
        this.looping = true; // Музыка будет автоматически зациклена во время боя
        this.delay = 0;
        this.volume = 1.0F;
        this.pitch = 1.0F;
        this.relative = true; // true делает звук глобальным (UI), он не затихает при повороте головы игрока
    }

    @Override
    public void tick() {
        // Условия остановки: босс удален, босс мертв ИЛИ игрок убежал дальше 50 блоков (50 * 50 = 2500)
        if (this.boss.isRemoved() || this.boss.isDeadOrDying() || this.boss.distanceToSqr(Minecraft.getInstance().player) > 2500) {
            this.stop(); // Полностью выключаем этот трек
        }
    }
}