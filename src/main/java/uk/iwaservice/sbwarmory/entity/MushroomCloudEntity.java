package uk.iwaservice.sbwarmory.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/** Purely decorative: a growing/fading billboard mushroom cloud, spawned by {@link CruiseMissileEntity} on impact. */
public class MushroomCloudEntity extends Entity {

    public static final int LIFETIME_TICKS = 80;

    public MushroomCloudEntity(EntityType<? extends MushroomCloudEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.tickCount > LIFETIME_TICKS) {
            discard();
        }
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }

    @Override
    protected void defineSynchedData() {
        // No synced data - position alone is enough.
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        // Not persisted - it's a short-lived visual effect.
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        // Not persisted - it's a short-lived visual effect.
    }
}
