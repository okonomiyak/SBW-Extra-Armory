package uk.iwaservice.sbwarmory.item;

import com.atsuishio.superbwarfare.entity.projectile.ProjectileEntity;
import uk.iwaservice.sbwarmory.ModRegistry;
import uk.iwaservice.sbwarmory.client.ThrowingKnifeItemModel;
import uk.iwaservice.sbwarmory.entity.ThrowingKnifeEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

/** Thrown like a snowball; reuses SuperbWarfare's generic bullet entity so headshots use its native multiplier. */
public class ThrowingKnifeItem extends Item implements GeoItem {

    private static final float DAMAGE = 100f;
    private static final float HEADSHOT_MULTIPLIER = 2f;
    private static final float VELOCITY = 1.2f;
    private static final float INACCURACY = 2f;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public ThrowingKnifeItem(Properties properties) {
        super(properties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOWBALL_THROW,
                SoundSource.NEUTRAL, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));

        if (!level.isClientSide) {
            ProjectileEntity knife = new ThrowingKnifeEntity(ModRegistry.THROWING_KNIFE.get(), level)
                    .shooter(player)
                    .damage(DAMAGE)
                    .headShot(HEADSHOT_MULTIPLIER)
                    .legShot(1f)
                    .velocity(VELOCITY);
            knife.setGravity(0f); // superbwarfare:setGravity - renamed to setCustomGravity in 0.8.9.1+
            knife.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
            knife.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, VELOCITY, INACCURACY);
            level.addFreshEntity(knife);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Static model, no animations.
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        super.initializeClient(consumer);
        consumer.accept(new IClientItemExtensions() {
            private final GeoItemRenderer<ThrowingKnifeItem> renderer = new GeoItemRenderer<>(new ThrowingKnifeItemModel());

            @Override
            @OnlyIn(Dist.CLIENT)
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }
        });
    }
}
