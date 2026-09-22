package uk.iwaservice.sbwarmory.item;

import uk.iwaservice.sbwarmory.client.LaserDesignatorItemModel;
import uk.iwaservice.sbwarmory.entity.CruiseMissileEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
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

/**
 * Same interaction pattern as SuperbWarfare's own long-range artillery indicator (right click to
 * look down the sight, scroll to zoom) - the only new part is what happens on a completed call-in:
 * a cruise missile instead of an artillery shell. Right click toggles aim; while aiming, holding
 * left click for {@link #LOCK_TICKS} calls in the missile at wherever the crosshair is pointing.
 */
public class LaserDesignatorItem extends Item implements GeoItem {

    public static final double MAX_RANGE = 128.0;
    protected static final double SPAWN_HEIGHT_ABOVE_TARGET = 150.0;
    public static final int LOCK_TICKS = 30; // ~1.5s sustained left-click hold to call in
    private static final String TAG_AIMING = "Aiming";

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public LaserDesignatorItem(Properties properties) {
        super(properties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    public static boolean isAiming(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(TAG_AIMING);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        boolean nowAiming = !isAiming(stack);
        stack.getOrCreateTag().putBoolean(TAG_AIMING, nowAiming);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                nowAiming ? SoundEvents.SPYGLASS_USE : SoundEvents.SPYGLASS_STOP_USING,
                SoundSource.PLAYERS, 0.6F, 1.0F);
        // CONSUME (not SUCCESS) - InteractionResult.shouldSwing() is only true for SUCCESS, and the
        // arm-swing animation looks like an attack, which is confusing on a toggle-aim action.
        return InteractionResultHolder.consume(stack);
    }

    /** Runs every tick the stack exists in an inventory - paints the laser dot while aiming and selected. */
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide || !selected || !isAiming(stack) || !(entity instanceof Player player)) {
            return;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        HitResult hit = player.pick(MAX_RANGE, 0f, false);
        if (hit.getType() == HitResult.Type.BLOCK) {
            Vec3 target = hit.getLocation();
            serverLevel.sendParticles(ParticleTypes.END_ROD, target.x, target.y + 0.1, target.z, 1, 0, 0, 0, 0);
        }
    }

    /**
     * Called server-side once the client's own hold-to-fire timer completes. Re-validated here rather
     * than trusted from the packet: the player must actually be holding an aiming designator.
     */
    public static void tryFireMissile(ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof LaserDesignatorItem) || !isAiming(stack)) {
            stack = player.getOffhandItem();
            if (!(stack.getItem() instanceof LaserDesignatorItem) || !isAiming(stack)) {
                return;
            }
        }
        LaserDesignatorItem item = (LaserDesignatorItem) stack.getItem();

        HitResult hit = player.pick(MAX_RANGE, 0f, false);
        if (hit.getType() != HitResult.Type.BLOCK) {
            return;
        }

        Level level = player.level();
        Vec3 target = hit.getLocation();
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE,
                SoundSource.PLAYERS, 0.6F, 1.6F);

        item.spawnMissile(player, level, target);

        var warning = net.minecraft.network.chat.Component.translatable(item.inboundMessageKey());
        for (var recipient : level.players()) {
            recipient.sendSystemMessage(warning);
        }

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
    }

    /** Overridden by variants that call in a different missile entity. */
    protected void spawnMissile(ServerPlayer player, Level level, Vec3 target) {
        CruiseMissileEntity missile = new CruiseMissileEntity(player, level, target.y);
        missile.setPos(target.x, target.y + SPAWN_HEIGHT_ABOVE_TARGET, target.z);
        level.addFreshEntity(missile);
    }

    /** Overridden by variants that want a different chat warning. */
    protected String inboundMessageKey() {
        return "chat.sbwarmory.cruise_missile_inbound";
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
            private final GeoItemRenderer<LaserDesignatorItem> renderer = new GeoItemRenderer<>(new LaserDesignatorItemModel());

            @Override
            @OnlyIn(Dist.CLIENT)
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }
        });
    }
}
