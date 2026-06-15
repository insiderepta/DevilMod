package net.devil.mod;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, DevilMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> DEVIL_TAB = CREATIVE_MODE_TABS.register("devil_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(DevilMod.DEVIL_CORE.get()))
                    .title(Component.translatable("creativetab.devil_tab"))
                    .displayItems((pParameters, pOutput) -> {
                        pOutput.accept(DevilMod.DEVIL_CORE.get());
                        pOutput.accept(DevilMod.DEMON_FAN.get());
                        pOutput.accept(DevilMod.DEVIL_SCYTHE.get());
                        pOutput.accept(DevilMod.BOSS_SPAWNER_ITEM.get());
                        pOutput.accept(DevilMod.BOSS_SPAWN_EGG.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
