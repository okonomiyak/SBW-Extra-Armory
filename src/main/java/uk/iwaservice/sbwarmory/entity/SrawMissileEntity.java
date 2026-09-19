package uk.iwaservice.sbwarmory.entity;

import com.atsuishio.superbwarfare.entity.projectile.GunGrenadeEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Wire-guided: every tick it curves its flight path toward wherever the shooter is currently looking.
 * Renders with SuperbWarfare's own default {@code BasicProjectileRenderer} appearance in flight (no
 * custom mesh/orientation tuning) - the custom sraw_missile model is only used for the held ammo item.
 */
public class SrawMissileEntity extends GunGrenadeEntity {

    private static final int GUIDANCE_START_DELAY_TICKS = 5;
    private static final double GUIDANCE_AIM_DISTANCE = 64.0;
    private static final double TURN_FACTOR = 0.08;

    public SrawMissileEntity(EntityType<? extends GunGrenadeEntity> type, Level level) {
        super(type, level);
    }

    public SrawMissileEntity(Entity shooter, Level level, float damage, float explosionDamage, float explosionRadius) {
        super(shooter, level, damage, explosionDamage, explosionRadius);
        setGravity(0f);
    }

    @Override
    public void tick() {
        if (!level().isClientSide && this.tickCount > GUIDANCE_START_DELAY_TICKS) {
            Entity shooter = getOwner();
            if (shooter != null && shooter.isAlive()) {
                Vec3 aimPoint = shooter.getEyePosition().add(shooter.getViewVector(1.0f).scale(GUIDANCE_AIM_DISTANCE));
                Vec3 desired = aimPoint.subtract(position());
                Vec3 current = getDeltaMovement();
                double speed = current.length();
                if (desired.lengthSqr() > 1.0E-6 && speed > 1.0E-6) {
                    Vec3 newDirection = current.normalize().scale(1 - TURN_FACTOR)
                            .add(desired.normalize().scale(TURN_FACTOR))
                            .normalize();
                    setDeltaMovement(newDirection.scale(speed));
                }
            }
        }
        super.tick();
        syncRotationToVelocity();

        if (level().isClientSide) {
            level().addParticle(ParticleTypes.END_ROD, getX(), getY(), getZ(), 0, 0, 0);
        }
    }

    private void syncRotationToVelocity() {
        Vec3 motion = getDeltaMovement();
        if (motion.lengthSqr() < 1.0E-6) return;
        double horizontalDistance = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        float newYRot = (float) (Mth.atan2(motion.x, motion.z) * (180.0 / Math.PI));
        float newXRot = (float) (Mth.atan2(-motion.y, horizontalDistance) * (180.0 / Math.PI));
        this.yRotO = getYRot();
        this.xRotO = getXRot();
        setYRot(newYRot);
        setXRot(newXRot);
    }
}
