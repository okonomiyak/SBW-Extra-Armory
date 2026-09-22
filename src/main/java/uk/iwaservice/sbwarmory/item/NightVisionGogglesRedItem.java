package uk.iwaservice.sbwarmory.item;

import uk.iwaservice.sbwarmory.client.NightVisionGogglesRedRenderer;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

/** Same everything as {@link NightVisionGogglesItem} - just the red-painted model/texture. */
public class NightVisionGogglesRedItem extends NightVisionGogglesItem {

    public NightVisionGogglesRedItem(Properties properties) {
        super(properties);
    }

    @Override
    protected GeoArmorRenderer<NightVisionGogglesItem> createRenderer() {
        return new NightVisionGogglesRedRenderer();
    }
}
