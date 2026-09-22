package uk.iwaservice.sbwarmory.client;

import uk.iwaservice.sbwarmory.item.NightVisionGogglesItem;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class NightVisionGogglesRedRenderer extends GeoArmorRenderer<NightVisionGogglesItem> {

    private static final float SHELL_SCALE = 1.05F;

    public NightVisionGogglesRedRenderer() {
        super(new NightVisionGogglesRedArmorModel());
        withScale(SHELL_SCALE);
    }
}
