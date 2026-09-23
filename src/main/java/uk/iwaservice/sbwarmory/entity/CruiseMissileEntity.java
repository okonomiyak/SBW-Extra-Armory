package uk.iwaservice.sbwarmory.entity;

import uk.iwaservice.sbwarmory.SbwArmoryMod;
import com.atsuishio.superbwarfare.entity.projectile.GunGrenadeEntity;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;

/**
 * Called in by {@link uk.iwaservice.sbwarmory.item.LaserDesignatorItem}: spawns high above the marked
 * point, hovers out of sight for a few seconds, then falls straight down and hits a wide area with
 * damage (falling off with distance) rather than actually destroying blocks of terrain.
 */
public class CruiseMissileEntity extends GunGrenadeEntity implements GuidedMissile {

    private static final int SIREN_DELAY_TICKS = 30; // starts 1.5s after launch

    // Read once at spawn (not per-tick/per-detonation) so a live config edit can't change the
    // behavior of a missile already in flight.
    private int warmupTicks;
    private double fallSpeed;
    private double damageHalfWidth;
    private double damageHalfHeight;
    private double targetY;
    private boolean detonated = false;
    // Not reusing the inherited tickCount: FastThrowableProjectile's own heavily-overridden tick()
    // may not reliably run the vanilla Entity bookkeeping that increments it.
    private int aliveTicks = 0;

    public CruiseMissileEntity(EntityType<? extends GunGrenadeEntity> type, Level level) {
        super(type, level);
    }

    public CruiseMissileEntity(Entity shooter, Level level, double targetY) {
        super(shooter, level, 0f, 0f, 0f);
        this.targetY = targetY;
        this.warmupTicks = (int) Math.round(uk.iwaservice.sbwarmory.ModConfig.CRUISE_MISSILE_WARMUP_SECONDS.get() * 20 * flightTimeScale());
        this.fallSpeed = uk.iwaservice.sbwarmory.ModConfig.CRUISE_MISSILE_FALL_SPEED.get() / flightTimeScale();
        this.damageHalfWidth = uk.iwaservice.sbwarmory.ModConfig.CRUISE_MISSILE_FOOTPRINT.get() / 2.0;
        this.damageHalfHeight = uk.iwaservice.sbwarmory.ModConfig.CRUISE_MISSILE_HEIGHT.get() / 2.0;
        setGravity(0f); // superbwarfare:setGravity - renamed to setCustomGravity in 0.8.9.1+
        setDeltaMovement(0, 0, 0);
        // Bypass the base class's own collision handling entirely (it would otherwise trigger its
        // own zero-power explosion the moment we physically touch a block during the fall).
        this.noPhysics = true;
        // FastThrowableProjectile has its own internal fuse (life) that silently self-destructs
        // (with zero power) once it runs out - our warmup + fall time can easily outlast the
        // default fuse, killing the missile a few ticks (blocks) short of the target. Disable it.
        setLife(Integer.MAX_VALUE);
    }

    @Override
    public void tick() {
        if (level().isClientSide || detonated) {
            super.tick();
            return;
        }

        aliveTicks++;
        if (aliveTicks >= SIREN_DELAY_TICKS && (aliveTicks - SIREN_DELAY_TICKS) % sirenLoopTicks() == 0) {
            float sirenVolume = (float) (16.0 * uk.iwaservice.sbwarmory.ModConfig.SIREN_VOLUME.get());
            if (sirenVolume > 0F) {
                // Source it at the target on the ground, not the missile's current position - during
                // warmup the missile sits 150 blocks straight up, which is outside any reasonable
                // audible range even before accounting for horizontal distance to the player.
                level().playSound(null, getX(), targetY, getZ(), sirenSound(),
                        SoundSource.HOSTILE, sirenVolume, 1.0F);
            }
        }

        if (warmupTicks > 0) {
            warmupTicks--;
            setDeltaMovement(0, 0, 0);
            if (warmupTicks % 10 == 0 && level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.FLAME, getX(), targetY, getZ(), 6, 0.3, 0.1, 0.3, 0.01);
            }
            super.tick();
            return;
        }

        // Check BEFORE moving/colliding this tick: super.tick() resolves real block collision,
        // and GunGrenadeEntity's own collision-explosion (with our deliberately zeroed damage/
        // radius) would silently consume the entity before detonate() ever got a chance to run.
        // Also detonate the moment it touches water - the target-designating raycast ignores
        // fluids, so a strike called in over/near water could otherwise sail past its target Y.
        if (getY() - fallSpeed <= targetY || isTouchingWater() || isTouchingImpactTarget()) {
            detonate();
            return;
        }
        setDeltaMovement(0, -fallSpeed, 0);
        super.tick();
    }

    /** Multiplies both warmup time and fall time - overridden by variants that arrive slower/faster. */
    protected double flightTimeScale() {
        return 1.0;
    }

    /** Reused every {@link #sirenLoopTicks()} ticks - overridden by variants with a different warning sound. */
    protected net.minecraft.sounds.SoundEvent sirenSound() {
        return uk.iwaservice.sbwarmory.ModRegistry.AIR_RAID_SIREN.get();
    }

    /** How often (in ticks) {@link #sirenSound()} re-triggers - match it to that sound's own length. */
    protected int sirenLoopTicks() {
        return 60; // 3s, matching the air raid siren's own loop length
    }

    /**
     * FastThrowableProjectile runs its own per-tick raycast (block AND entity) independent of vanilla
     * collision/{@code noPhysics}, and calls onHit() the moment it finds anything - which would
     * otherwise trigger its own silent, zero-power explosion. Catch that here instead: whatever it
     * hit, at whatever point in the flight, this is a real impact - detonate for real.
     */
    @Override
    public void onHit(HitResult hitResult) {
        if (!level().isClientSide && !detonated) {
            detonate();
            return;
        }
        super.onHit(hitResult);
    }

    /**
     * Checked directly against the fluid state rather than {@code isInWater()}: with {@code noPhysics}
     * set, the vanilla in-water flag isn't guaranteed to be kept up to date for this entity.
     */
    private boolean isTouchingWater() {
        return level().getFluidState(blockPosition()).is(net.minecraft.tags.FluidTags.WATER);
    }

    /**
     * With {@code noPhysics} set, entity collision never fires either - a vehicle or mob directly in
     * the flight path would otherwise just be phased through with no impact at all.
     */
    private boolean isTouchingImpactTarget() {
        AABB probe = getBoundingBox().inflate(1.0);
        if (!level().getEntitiesOfClass(VehicleEntity.class, probe).isEmpty()) {
            return true;
        }
        for (LivingEntity target : level().getEntitiesOfClass(LivingEntity.class, probe)) {
            if (target != getOwner()) {
                return true;
            }
        }
        return false;
    }

    private void detonate() {
        detonated = true;
        SbwArmoryMod.LOGGER.info("CruiseMissileEntity detonating at {} {} {}", getX(), getY(), getZ());
        Level level = level();
        if (level instanceof ServerLevel serverLevel) {
            float explosionRadius = uk.iwaservice.sbwarmory.ModConfig.CRUISE_MISSILE_EXPLOSION_RADIUS.get().floatValue();
            float damage = uk.iwaservice.sbwarmory.ModConfig.CRUISE_MISSILE_DAMAGE.get().floatValue();
            float vehicleDamage = uk.iwaservice.sbwarmory.ModConfig.CRUISE_MISSILE_VEHICLE_DAMAGE.get().floatValue();

            level.explode(this, getX(), getY(), getZ(), explosionRadius, Level.ExplosionInteraction.MOB);
            spawnSpectacle(serverLevel);

            AABB area = new AABB(
                    getX() - damageHalfWidth, getY() - damageHalfHeight, getZ() - damageHalfWidth,
                    getX() + damageHalfWidth, getY() + damageHalfHeight, getZ() + damageHalfWidth);
            for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, area)) {
                target.hurt(level.damageSources().explosion(this, getOwner()), damage);
            }
            // VehicleEntity is a plain Entity (not LivingEntity), so it needs its own pass here -
            // it was previously untouched by this area damage entirely.
            for (VehicleEntity vehicle : serverLevel.getEntitiesOfClass(VehicleEntity.class, area)) {
                vehicle.hurt(level.damageSources().explosion(this, getOwner()), vehicleDamage);
            }
        }
        discard();
    }

    private void spawnSpectacle(ServerLevel serverLevel) {
        spawnCloud(serverLevel);

        serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY(), getZ(), 1, 0, 0, 0, 0);
        serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY(), getZ(), 40, 6, 4, 6, 0.05);
        serverLevel.sendParticles(ParticleTypes.FLAME, getX(), getY(), getZ(), 60, 8, 3, 8, 0.08);

        // Scatter secondary blasts across the whole footprint for a carpet-bombing look.
        for (int i = 0; i < 24; i++) {
            double x = getX() + (random.nextDouble() * 2 - 1) * damageHalfWidth;
            double z = getZ() + (random.nextDouble() * 2 - 1) * damageHalfWidth;
            serverLevel.sendParticles(ParticleTypes.EXPLOSION, x, getY(), z, 1, 0, 0, 0, 0);
        }

        serverLevel.playSound(null, getX(), getY(), getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 8.0F, 0.8F);
        playImpactExtraSound(serverLevel);
    }

    /** Mushroom-cloud puffs: a narrow rising stem, then a wide cap on top. Overridden by other themes. */
    protected void spawnCloud(ServerLevel serverLevel) {
        MushroomCloudEntity cloud = new MushroomCloudEntity(uk.iwaservice.sbwarmory.ModRegistry.MUSHROOM_CLOUD.get(), serverLevel);
        cloud.setPos(getX(), getY(), getZ());
        serverLevel.addFreshEntity(cloud);

        var puff = uk.iwaservice.sbwarmory.ModRegistry.MUSHROOM_PUFF.get();
        serverLevel.sendParticles(puff, getX(), getY() + 8, getZ(), 40, 2.0, 8.0, 2.0, 0.02);
        serverLevel.sendParticles(puff, getX(), getY() + 20, getZ(), 50, 10.0, 3.0, 10.0, 0.05);
    }

    /** No-op here - overridden by variants that want an extra sound layered on top of the explosion. */
    protected void playImpactExtraSound(ServerLevel serverLevel) {
    }
}
