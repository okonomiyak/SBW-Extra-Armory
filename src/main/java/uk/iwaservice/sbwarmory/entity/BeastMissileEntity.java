package uk.iwaservice.sbwarmory.entity;

import com.atsuishio.superbwarfare.entity.projectile.GunGrenadeEntity;
import com.atsuishio.superbwarfare.init.ModSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/** Same everything as {@link CruiseMissileEntity} - the "siren" is SuperbWarfare's senpai growl instead. */
public class BeastMissileEntity extends CruiseMissileEntity {

    public BeastMissileEntity(EntityType<? extends GunGrenadeEntity> type, Level level) {
        super(type, level);
    }

    public BeastMissileEntity(Entity shooter, Level level, double targetY) {
        super(shooter, level, targetY);
    }

    @Override
    protected double flightTimeScale() {
        return 2.0; // takes twice as long (longer warmup wait, slower fall) to arrive
    }

    @Override
    protected SoundEvent sirenSound() {
        return ModSounds.GROWL.get();
    }

    @Override
    protected int sirenLoopTicks() {
        return 520; // growl.ogg runs ~26s - only re-trigger if the flight somehow outlasts one growl
    }

    @Override
    protected void playImpactExtraSound(ServerLevel serverLevel) {
        serverLevel.playSound(null, getX(), getY(), getZ(), uk.iwaservice.sbwarmory.ModRegistry.YARIMASUNE.get(),
                SoundSource.HOSTILE, 4.0F, 1.0F);
    }

    /** Rock chunks (the missile's own texture) flying outward, instead of a mushroom cloud. */
    @Override
    protected void spawnCloud(ServerLevel serverLevel) {
        var debris = uk.iwaservice.sbwarmory.ModRegistry.ROCK_DEBRIS.get();
        for (int i = 0; i < 60; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double speed = 0.3 + random.nextDouble() * 0.6;
            double dx = Math.cos(angle) * speed;
            double dz = Math.sin(angle) * speed;
            double dy = 0.4 + random.nextDouble() * 0.8;
            serverLevel.sendParticles(debris, getX(), getY() + 1, getZ(), 0, dx, dy, dz, 1.0);
        }
    }
}
