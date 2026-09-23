package uk.iwaservice.sbwarmory.entity;

import uk.iwaservice.sbwarmory.ModRegistry;
import com.atsuishio.superbwarfare.entity.projectile.HandGrenadeEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * A non-lethal utility grenade (zero damage) that bursts into a cloud of {@link ModRegistry#COMA}
 * on impact, briefly slowing and blinding everyone caught nearby. Renders as the (recolored) smoke
 * cluster grenade model via {@link uk.iwaservice.sbwarmory.client.ComaGrenadeEntityRenderer}.
 */
public class ComaGrenadeEntity extends HandGrenadeEntity implements GeoEntity {

    private static final double EFFECT_RADIUS = 5.0;
    private static final int EFFECT_DURATION_TICKS = 20; // 1s

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private boolean detonated = false;

    public ComaGrenadeEntity(EntityType<? extends HandGrenadeEntity> type, Level level) {
        super(type, level);
    }

    public ComaGrenadeEntity(LivingEntity shooter, Level level) {
        super(shooter, level);
        setDamageValue(0f);
        setExplosionDamageValue(0f);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Static model, no animations.
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    /**
     * Same FastThrowableProjectile-family hook used by {@link CruiseMissileEntity} - fires on the
     * real impact. Deliberately does NOT call {@code super.onHit(...)}: with damage zeroed out,
     * SuperbWarfare's own hand grenade explosion logic treats this as a dud and just leaves the
     * grenade sitting on the ground instead of discarding it - so this handles cleanup itself instead.
     */
    @Override
    public void onHit(HitResult hitResult) {
        if (!level().isClientSide && !detonated) {
            detonated = true;
            burst();
        }
    }

    private void burst() {
        if (level() instanceof ServerLevel serverLevel) {
            AABB area = new AABB(getX() - EFFECT_RADIUS, getY() - EFFECT_RADIUS, getZ() - EFFECT_RADIUS,
                    getX() + EFFECT_RADIUS, getY() + EFFECT_RADIUS, getZ() + EFFECT_RADIUS);
            for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, area)) {
                target.addEffect(new MobEffectInstance(ModRegistry.COMA.get(), EFFECT_DURATION_TICKS, 0));
            }
            serverLevel.sendParticles(ParticleTypes.SNEEZE, getX(), getY(), getZ(), 40, EFFECT_RADIUS * 0.4, 0.5, EFFECT_RADIUS * 0.4, 0.02);
            serverLevel.playSound(null, getX(), getY(), getZ(), SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.NEUTRAL, 1.0F, 0.8F);
        }
        discard();
    }
}
