package uk.iwaservice.sbwarmory.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Uses SBW's "Ship" engine (data/sbwarmory/sbw/vehicles/hovercraft.json), which only steers and pushes
 * while the hull is in fluid. On land we apply the same steering/thrust formulas as SBW's shipEngine
 * (VehicleEngineUtils) ourselves, so the craft also drives over ground and beaches.
 */
public class HovercraftEntity extends CivilianCarEntity {

    /**
     * SBW damps grounded Ship-engine vehicles by 0.75/tick vs ~0.85/tick in water, so land thrust is
     * scaled up to land at roughly 90% of water top speed. Raise for faster land travel.
     */
    private static final double LAND_THRUST_SCALE = 1.5;

    public HovercraftEntity(EntityType<? extends HovercraftEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public void travel() {
        super.travel();
        if (isInFluidType() || isUnderWater() || isWreck()) return;

        float power = getPower();
        Vec3 motion = getDeltaMovement();
        // Same as shipEngine: yaw -= 20 * horizontalSpeed * deltaRot * sign(power)
        setYRot((float) (getYRot() - 20 * motion.horizontalDistance() * getDeltaRot() * (power > 0 ? 1 : -1)));
        // Same as shipEngine: motion += view * 0.11 * targetSpeed * power * min(|power|, 1)
        double thrust = LAND_THRUST_SCALE * 0.11 * getTargetSpeed() * power * Math.min(Math.abs(power), 1f);
        setDeltaMovement(motion.add(getViewVector(1f).scale(thrust)));
    }
}
