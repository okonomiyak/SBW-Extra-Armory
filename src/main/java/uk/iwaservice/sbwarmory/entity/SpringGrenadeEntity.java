package uk.iwaservice.sbwarmory.entity;

import com.atsuishio.superbwarfare.entity.projectile.HandGrenadeEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

/** Like a real S-mine: sits/bounces like a normal hand grenade, then springs 1.5 blocks up right before it detonates. */
public class SpringGrenadeEntity extends HandGrenadeEntity implements GeoEntity {

    private static final int FUSE_TICKS = 30;
    private static final int POP_TICKS_BEFORE_EXPLOSION = 4;
    private static final double POP_HEIGHT = 1.5;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private boolean popped = false;

    public SpringGrenadeEntity(EntityType<? extends HandGrenadeEntity> type, Level level) {
        super(type, level);
    }

    public SpringGrenadeEntity(LivingEntity shooter, Level level) {
        super(shooter, level);
        setLife(FUSE_TICKS);
        setDamageValue(getDamageValue() * 2f / 3f);
        setExplosionDamageValue(getExplosionDamageValue() * 2f / 3f);
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
    public void tick() {
        // Check before super.tick() runs: that's where the fuse countdown reaches 0 and explodes.
        if (!popped && getLife() == POP_TICKS_BEFORE_EXPLOSION) {
            popped = true;
            setDeltaMovement(0, 0, 0);
            setPos(getX(), getY() + POP_HEIGHT, getZ());
        }
        super.tick();
    }
}
