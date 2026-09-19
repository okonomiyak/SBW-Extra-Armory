package uk.iwaservice.sbwarmory.entity;

import com.atsuishio.superbwarfare.entity.projectile.GunGrenadeEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

import java.util.Random;

/** Same split-before-impact behaviour as {@link SmokeClusterGrenadeEntity}, but the submunitions are live explosive grenades instead of smoke. */
public class ClusterGrenadeEntity extends GunGrenadeEntity {

    private static final int SPREAD_AMOUNT = 3;
    private static final float SPREAD_ANGLE = 25f;
    private static final Random RANDOM = new Random();

    private boolean released = false;

    public ClusterGrenadeEntity(EntityType<? extends GunGrenadeEntity> type, Level level) {
        super(type, level);
    }

    public ClusterGrenadeEntity(Entity shooter, Level level, float damage, float explosionDamage, float explosionRadius) {
        super(shooter, level, damage, explosionDamage, explosionRadius);
    }

    @Override
    public void tick() {
        super.tick();

        if (released || this.tickCount <= 3) return;

        int spreadTime = 8;
        HitResult hitResult = level().clip(new ClipContext(
                position(),
                position().add(getDeltaMovement().scale(spreadTime)),
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.ANY,
                this
        ));

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            releaseCluster();
        }
    }

    private void releaseCluster() {
        released = true;
        Level level = level();
        if (!(level instanceof ServerLevel serverLevel)) return;

        Entity shooter = getOwner();

        serverLevel.sendParticles(ParticleTypes.POOF, getX(), getY(), getZ(), 10, 0.2, 0.2, 0.2, 0.02);

        for (int i = 0; i < SPREAD_AMOUNT; i++) {
            GunGrenadeEntity bomblet = new GunGrenadeEntity(shooter, level, getDamageValue(), getExplosionDamageValue(), getExplosionRadiusValue());
            bomblet.setPos(getX(), getY(), getZ());
            bomblet.shoot(
                    getDeltaMovement().x,
                    getDeltaMovement().y,
                    getDeltaMovement().z,
                    RANDOM.nextFloat() * 0.2f + 0.4f * (float) getDeltaMovement().length(),
                    SPREAD_ANGLE
            );
            level.addFreshEntity(bomblet);
        }

        this.discard();
    }
}
