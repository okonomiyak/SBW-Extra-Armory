package uk.iwaservice.sbwarmory.entity;

import uk.iwaservice.sbwarmory.ModConfig;
import com.atsuishio.superbwarfare.entity.projectile.GunGrenadeEntity;
import com.atsuishio.superbwarfare.entity.projectile.HandGrenadeEntity;
import com.atsuishio.superbwarfare.entity.projectile.M18SmokeGrenadeEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * A stationary defensive post: every tick, destroys any grenade, primed TNT, or actively-swelling
 * creeper that comes within range before it can detonate. Deliberately narrow-scoped - RPG/SRAW/
 * Javelin and our own cruise/beast missiles are all explicitly left alone (see {@link #isThreat}).
 * Fragile by design (low health, configurable) and rate-limited so it can't shrug off a spam attack
 * for free.
 */
public class ActiveDefenseSystemEntity extends Entity implements GeoEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // Read once at spawn (not per-tick) so a live config edit doesn't affect a device already placed.
    private final double radius;
    private final int cooldownTicks;
    private int health;
    private int cooldownRemaining = 0;

    public ActiveDefenseSystemEntity(EntityType<? extends ActiveDefenseSystemEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.radius = ModConfig.ACTIVE_DEFENSE_RADIUS.get();
        this.cooldownTicks = ModConfig.ACTIVE_DEFENSE_COOLDOWN_SECONDS.get() * 20;
        this.health = ModConfig.ACTIVE_DEFENSE_HEALTH.get();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Static model, no animations yet.
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            return;
        }

        if (cooldownRemaining > 0) {
            cooldownRemaining--;
            return;
        }

        AABB area = getBoundingBox().inflate(radius);
        for (Entity target : level().getEntitiesOfClass(Entity.class, area, ActiveDefenseSystemEntity::isThreat)) {
            intercept(target);
            cooldownRemaining = cooldownTicks;
            break; // one interception per cooldown window
        }
    }

    /**
     * Grenades, primed TNT, and swelling creepers - explicitly NOT RPG/SRAW/Javelin. Our own cluster/
     * smoke-cluster grenades and vanilla 40mm rounds all extend {@link GunGrenadeEntity}, hand
     * grenades/spring grenade extend {@link HandGrenadeEntity}, and smoke grenades are
     * {@link M18SmokeGrenadeEntity}. Some of our own guided missiles (SRAW, cruise/beast missile) also
     * happen to extend GunGrenadeEntity for convenience - excluding those by class name is fragile
     * (silently breaks again the next time a new missile picks the same base class, as SRAW just
     * did), so exclude by the shared {@link GuidedMissile} marker interface instead. RPG/Javelin/
     * wire-guided missiles use entirely different SuperbWarfare base classes and were never going to
     * match here regardless. A creeper only counts while actively swelling toward detonation
     * ({@code getSwellDir() > 0}) - a calm one just standing nearby isn't a threat yet.
     */
    private static boolean isThreat(Entity entity) {
        if (entity instanceof GuidedMissile) {
            return false;
        }
        if (entity instanceof GunGrenadeEntity || entity instanceof HandGrenadeEntity
                || entity instanceof M18SmokeGrenadeEntity || entity instanceof PrimedTnt) {
            return true;
        }
        return entity instanceof Creeper creeper && creeper.getSwellDir() > 0;
    }

    private void intercept(Entity target) {
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SMOKE, target.getX(), target.getY(), target.getZ(),
                    10, 0.2, 0.2, 0.2, 0.03);
            serverLevel.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.FIREWORK_ROCKET_BLAST_FAR, SoundSource.HOSTILE, 1.0F, 1.6F);
        }
        target.discard();
    }

    // Plain Entity isn't damageable by default (isPickable() is false) - override the usual trio so
    // players/explosions can actually destroy this fragile little thing.
    @Override
    public boolean isPickable() {
        return !isRemoved();
    }

    @Override
    public boolean isAttackable() {
        return true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (level().isClientSide || isRemoved()) {
            return false;
        }
        health -= Math.max(1, (int) amount);
        if (health <= 0) {
            if (level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SMOKE, getX(), getY() + 0.5, getZ(), 15, 0.3, 0.3, 0.3, 0.05);
                serverLevel.playSound(null, getX(), getY(), getZ(), SoundEvents.GENERIC_EXPLODE,
                        SoundSource.BLOCKS, 0.6F, 1.4F);
            }
            discard();
        }
        return true;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }

    @Override
    protected void defineSynchedData() {
        // No synced data - position alone is enough for now.
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        // No extra fields yet.
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        // No extra fields yet.
    }
}
