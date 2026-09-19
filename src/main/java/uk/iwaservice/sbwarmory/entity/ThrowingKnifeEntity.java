package uk.iwaservice.sbwarmory.entity;

import com.atsuishio.superbwarfare.entity.projectile.ProjectileEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

/** Same damage/headshot/legshot/velocity-falloff behaviour as {@link ProjectileEntity}, just rendered with our knife model in flight. */
public class ThrowingKnifeEntity extends ProjectileEntity implements GeoEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public ThrowingKnifeEntity(EntityType<? extends ProjectileEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public void tick() {
        super.tick();
        // Keep the model's facing locked to the current travel direction every tick,
        // rather than only at the moment it was thrown.
        updateHeading();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Static model, no animations.
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
