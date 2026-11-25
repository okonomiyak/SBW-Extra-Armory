package uk.iwaservice.sbwarmory.entity;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * SBW's Wheel engine assumes four wheels and never banks, so this leans the bike into turns itself:
 * lean = atan(speed * yawRate / gravity), the angle a real bike needs to balance the turn. Applied as the
 * vehicle's roll, so riders (positioned by the vehicle transform) lean with it.
 * <p>
 * SBW also writes roll every tick (terrain following in baseTick lerps it toward the ground slope), so
 * our lean is kept as a separate offset: stripped before SBW's tick, re-added after it.
 */
public class MotorcycleEntity extends CivilianCarEntity {

    /** Minecraft's entity gravity, blocks/tick^2. */
    private static final double GRAVITY = 0.08;
    private static final float MAX_LEAN_DEGREES = 40f;
    /** Fraction of the remaining lean change applied per tick (higher = snappier). */
    private static final float LEAN_SMOOTHING = 0.3f;

    private float lean;

    public MotorcycleEntity(EntityType<? extends MotorcycleEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public void baseTick() {
        float shownLean = lean;
        setZRot(getRoll() - shownLean);
        super.baseTick(); // saves prevRoll (now lean-free), drives, terrain-follows

        double speed = getDeltaMovement().horizontalDistance();
        double yawRate = Math.toRadians(Mth.wrapDegrees(getYRot() - yRotO));
        // SBW rolls around the forward axis with Axis.ZP: positive roll banks right, and turning right raises yRot.
        float target = Mth.clamp((float) Math.toDegrees(Math.atan(speed * yawRate / GRAVITY)),
                -MAX_LEAN_DEGREES, MAX_LEAN_DEGREES);
        lean += (target - lean) * LEAN_SMOOTHING;

        setPrevRoll(getPrevRoll() + shownLean); // keep render interpolation continuous across the strip/re-add
        setZRot(getRoll() + lean);
    }
}
