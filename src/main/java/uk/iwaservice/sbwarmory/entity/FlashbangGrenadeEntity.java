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
 * Non-lethal stun grenade: bounces like a normal hand grenade, then on fuse-out goes off with a bang
 * instead of an explosion, applying {@link ModRegistry#FLASHBANG} to everyone with line of sight.
 * Duration scales with distance and with whether the target was looking toward the flash.
 */
public class FlashbangGrenadeEntity extends HandGrenadeEntity implements GeoEntity {

    private static final int FUSE_TICKS = 30;
    private static final double RADIUS = 12.0;
    private static final int MAX_DURATION_TICKS = 140; // 7s point-blank, looking straight at it
    private static final int MIN_DURATION_TICKS = 20;
    /** Fraction of the effect still received with your back to the flash. */
    private static final double LOOKING_AWAY_FACTOR = 0.35;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public FlashbangGrenadeEntity(EntityType<? extends HandGrenadeEntity> type, Level level) {
        super(type, level);
    }

    public FlashbangGrenadeEntity(LivingEntity shooter, Level level) {
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
    public void causeExplode(Vec3 pos) {
        if (!(level() instanceof ServerLevel serverLevel)) return;

        AABB area = new AABB(pos, pos).inflate(RADIUS);
        for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, area)) {
            Vec3 eye = target.getEyePosition();
            double distance = eye.distanceTo(pos);
            if (distance > RADIUS) continue;
            if (serverLevel.clip(new ClipContext(eye, pos, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, target))
                    .getType() != HitResult.Type.MISS) continue;

            double facing = target.getViewVector(1f).dot(pos.subtract(eye).normalize()); // 1 = staring at it
            double strength = (1 - distance / RADIUS) * (facing > 0 ? 0.5 + 0.5 * facing : LOOKING_AWAY_FACTOR);
            int duration = Math.max(MIN_DURATION_TICKS, (int) Math.round(MAX_DURATION_TICKS * strength));
            target.addEffect(new MobEffectInstance(ModRegistry.FLASHBANG.get(), duration, 0));
        }

        serverLevel.sendParticles(ParticleTypes.FLASH, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
        serverLevel.sendParticles(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, 40, 0.2, 0.2, 0.2, 0.25);
        serverLevel.sendParticles(ParticleTypes.CLOUD, pos.x, pos.y, pos.z, 10, 0.3, 0.3, 0.3, 0.02);
        serverLevel.playSound(null, pos.x, pos.y, pos.z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 3.0F, 1.8F);
        serverLevel.playSound(null, pos.x, pos.y, pos.z, SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS, 3.0F, 0.6F);
        discard();
    }
}
