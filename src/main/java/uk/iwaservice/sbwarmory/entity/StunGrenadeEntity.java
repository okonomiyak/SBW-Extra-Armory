package uk.iwaservice.sbwarmory.entity;

import com.atsuishio.superbwarfare.entity.projectile.HandGrenadeEntity;
import com.atsuishio.superbwarfare.init.ModMobEffects;
import com.atsuishio.superbwarfare.init.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Bounces like a normal hand grenade, then on fuse-out discharges SuperbWarfare's own Shock effect
 * (the taser one: rooted in place + small electric damage) on everyone in range with line of sight.
 * The thrower is passed as the effect source, so SBW credits the shock damage to them.
 */
public class StunGrenadeEntity extends HandGrenadeEntity implements GeoEntity {

    private static final int FUSE_TICKS = 30;
    private static final double CLEARANCE = 0.25;
    private static final double RADIUS = 6.0;
    private static final int MAX_DURATION_TICKS = 80; // 4s at the centre
    private static final int MIN_DURATION_TICKS = 20;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public StunGrenadeEntity(EntityType<? extends HandGrenadeEntity> type, Level level) {
        super(type, level);
    }

    public StunGrenadeEntity(LivingEntity shooter, Level level) {
        super(shooter, level);
        setLife(FUSE_TICKS);
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

    /** Called by SBW's FastThrowableProjectile when the fuse runs out (it discards us right after). */
    @Override
    public void causeExplode(Vec3 grenadePos) {
        if (!(level() instanceof ServerLevel serverLevel)) return;
        // A grenade resting on the ground sits on (or a hair inside) the floor block, and a ray that starts
        // or ends inside a block counts as blocked - lift the burst point so line-of-sight checks work.
        Vec3 pos = grenadePos.add(0, CLEARANCE, 0);

        AABB area = new AABB(pos, pos).inflate(RADIUS);
        for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, area)) {
            Vec3 center = target.getBoundingBox().getCenter();
            double distance = center.distanceTo(pos);
            if (distance > RADIUS) continue;
            if (serverLevel.clip(new ClipContext(pos, center, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this))
                    .getType() != HitResult.Type.MISS) continue;

            int duration = Math.max(MIN_DURATION_TICKS, (int) Math.round(MAX_DURATION_TICKS * (1 - distance / RADIUS)));
            target.addEffect(new MobEffectInstance(ModMobEffects.SHOCK.get(), duration, 0), getOwner());
        }

        serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, pos.x, pos.y, pos.z, 80, RADIUS * 0.3, 0.6, RADIUS * 0.3, 0.6);
        serverLevel.sendParticles(ParticleTypes.FLASH, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
        serverLevel.playSound(null, pos.x, pos.y, pos.z, ModSounds.ELECTRIC.get(), SoundSource.PLAYERS, 2.0F, 1.0F);
        serverLevel.playSound(null, pos.x, pos.y, pos.z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.5F, 1.6F);
        discard();
    }
}
