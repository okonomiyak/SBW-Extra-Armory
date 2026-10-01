package uk.iwaservice.sbwarmory.client;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.core.animatable.GeoAnimatable;

/**
 * Vehicle renderer for SuperbWarfare 0.8.9.0, which draws vehicles with GeckoLib. SBW's generic
 * VehicleModel resolves model/texture from the "Model" block of assets/sbwarmory/sbw/vehicles/<id>.json
 * (the GeckoLib copy of each geo lives under geo/vehicle/, copied there at build time).
 * Only used when {@link uk.iwaservice.sbwarmory.SbwCompat#LEGACY_VEHICLE_RENDERING}.
 */
public class LegacyVehicleRenderer<T extends VehicleEntity & GeoAnimatable> extends VehicleRenderer<T> {

    public LegacyVehicleRenderer(EntityRendererProvider.Context context) {
        super(context, new VehicleModel<>());
    }
}
