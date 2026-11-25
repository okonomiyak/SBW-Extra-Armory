package uk.iwaservice.sbwarmory.entity;

import com.atsuishio.superbwarfare.entity.vehicle.base.GeoVehicleEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * Plain civilian car (probox_wagon, hiace_van, luxury_sedan, probox_mg, assault_boat; hovercraft/motorcycle via subclasses). All stats/seats/OBBs come from data/sbwarmory/sbw/vehicles/<id>.json,
 * model/texture from assets/sbwarmory/sbw/vehicles/<id>.json - same as SBW's SodayoPickUpEntity.
 * Extends GeoVehicleEntity (not plain VehicleEntity) because SBW 0.8.9.0 renders vehicles through
 * GeckoLib and needs a GeoAnimatable; 0.8.9.1+ ignores it (see SbwCompat).
 */
public class CivilianCarEntity extends GeoVehicleEntity {

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
