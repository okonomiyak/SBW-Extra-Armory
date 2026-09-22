package uk.iwaservice.sbwarmory.client;

import uk.iwaservice.sbwarmory.item.NightVisionGogglesItem;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class NightVisionGogglesRenderer extends GeoArmorRenderer<NightVisionGogglesItem> {

    // Slightly larger than the bare head so its shell doesn't sit flush against the vanilla head
    // model - coincident/overlapping surfaces z-fight (visibly flicker/cull) as the camera turns.
    private static final float SHELL_SCALE = 1.05F;

    public NightVisionGogglesRenderer() {
        super(new NightVisionGogglesArmorModel());
        withScale(SHELL_SCALE);
    }
}
