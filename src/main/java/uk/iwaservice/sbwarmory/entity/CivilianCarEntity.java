package uk.iwaservice.sbwarmory.entity;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * Plain civilian car (probox_wagon, hiace_van, luxury_sedan, probox_mg; hovercraft via HovercraftEntity). All stats/seats/OBBs come from data/sbwarmory/sbw/vehicles/<id>.json,
 * model/texture from assets/sbwarmory/sbw/vehicles/<id>.json - same as SBW's SodayoPickUpEntity.
 */
public class CivilianCarEntity extends VehicleEntity {

    public CivilianCarEntity(EntityType<? extends CivilianCarEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public void baseTick() {
        super.baseTick();
        // Same as SBW's civilian cars: the decoy key sounds the horn.
        if (decoyInputDown()) horn();
    }
}
