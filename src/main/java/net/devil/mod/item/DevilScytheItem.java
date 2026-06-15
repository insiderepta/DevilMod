package net.devil.mod.item;

import net.devil.mod.item.client.DevilScytheRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

public class DevilScytheItem extends SwordItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public DevilScytheItem(Properties properties) {
        super(Tiers.NETHERITE, 8, -2.4F, properties);
    }

    @Override
    public net.minecraft.world.InteractionResultHolder<ItemStack> use(net.minecraft.world.level.Level level, net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                java.util.List<net.devil.mod.entity.PlayerServantEntity> existing = level.getEntitiesOfClass(
                        net.devil.mod.entity.PlayerServantEntity.class, 
                        player.getBoundingBox().inflate(128.0D), 
                        e -> e.isOwnedBy(player)
                );

                if (existing.isEmpty()) {
                    net.devil.mod.entity.PlayerServantEntity servant = net.devil.mod.entity.ModEntities.PLAYER_SERVANT.get().create(level);
                    if (servant != null) {
                        servant.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), 0.0F);
                        servant.tame(player); // Устанавливаем игрока как владельца
                        level.addFreshEntity(servant);
                        level.playSound(null, player.getX(), player.getY(), player.getZ(), net.minecraft.sounds.SoundEvents.TOTEM_USE, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
                    }
                } else {
                    player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cУ вас уже есть прислуга!"), true);
                }
            }
            return net.minecraft.world.InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
        }

        return super.use(level, player, hand);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, event -> PlayState.CONTINUE));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private DevilScytheRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new DevilScytheRenderer();
                }
                return this.renderer;
            }
        });
    }
}
