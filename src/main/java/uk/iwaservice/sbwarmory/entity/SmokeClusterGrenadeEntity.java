package uk.iwaservice.sbwarmory.entity;

import com.atsuishio.superbwarfare.entity.projectile.GunGrenadeEntity;
import com.atsuishio.superbwarfare.entity.projectile.M18SmokeGrenadeEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

import java.util.Random;

public class SmokeClusterGrenadeEntity extends GunGrenadeEntity {

    private static final int SPREAD_AMOUNT = 3;      // 分離する数
    private static final float SPREAD_ANGLE = 25f;   // 散開角度
    private static final int SMOKE_FUSE = 30;        // 分離後、発煙までのtick数
    private static final Random RANDOM = new Random();

    private boolean released = false;

    public SmokeClusterGrenadeEntity(EntityType<? extends GunGrenadeEntity> type, Level level) {
        super(type, level);
    }

    public SmokeClusterGrenadeEntity(Entity shooter, Level level, float damage, float explosionDamage, float explosionRadius) {
        super(shooter, level, damage, explosionDamage, explosionRadius);
    }

    @Override
    public void tick() {
        super.tick();

        // discard済みなら以降の判定は不要
        if (released || this.tickCount <= 3) return;

        // 8tick先までレイキャストして着弾直前を検知（CannonShellEntityと同じ手法）
        int spreadTime = 8;
        HitResult hitResult = level().clip(new ClipContext(
                position(),
                position().add(getDeltaMovement().scale(spreadTime)),
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.ANY,
                this
        ));

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            releaseSmokeCluster();
        }
    }

    private void releaseSmokeCluster() {
        released = true;
        Level level = level();
        if (!(level instanceof ServerLevel serverLevel)) return;

        Entity ownerEntity = getOwner();
        LivingEntity shooter = (ownerEntity instanceof LivingEntity le) ? le : null;

        serverLevel.sendParticles(ParticleTypes.POOF, getX(), getY(), getZ(), 10, 0.2, 0.2, 0.2, 0.02);

        for (int i = 0; i < SPREAD_AMOUNT; i++) {
            M18SmokeGrenadeEntity smoke = new M18SmokeGrenadeEntity(shooter, level, SMOKE_FUSE);
            smoke.setPos(getX(), getY(), getZ());
            smoke.shoot(
                    getDeltaMovement().x,
                    getDeltaMovement().y,
                    getDeltaMovement().z,
                    RANDOM.nextFloat() * 0.2f + 0.4f * (float) getDeltaMovement().length(),
                    SPREAD_ANGLE
            );
            level.addFreshEntity(smoke);
        }

        this.discard();
    }
}
