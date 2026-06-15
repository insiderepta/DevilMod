package net.devil.mod.item;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, "devilmod");

    // Регистрируем наш бесконечный предмет
    public static final RegistryObject<Item> DEMON_FAN = ITEMS.register("demon_fan",
            () -> new DemonFanItem(new Item.Properties()));

    public static final RegistryObject<Item> DEVIL_SCYTHE = ITEMS.register("devil_scythe",
            () -> new DevilScytheItem(new Item.Properties()));
}