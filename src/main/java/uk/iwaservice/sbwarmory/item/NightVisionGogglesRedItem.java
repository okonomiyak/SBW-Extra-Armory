package uk.iwaservice.sbwarmory.item;

import uk.iwaservice.sbwarmory.client.NightVisionGogglesRedRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

/** Same everything as {@link NightVisionGogglesItem} - just the red-painted model/texture. */
public class NightVisionGogglesRedItem extends NightVisionGogglesItem {

    public NightVisionGogglesRedItem(Properties properties) {
        super(properties);
    }

    // @OnlyIn required - see the base class's createRenderer() for why.
    @Override
    @OnlyIn(Dist.CLIENT)
    protected GeoArmorRenderer<NightVisionGogglesItem> createRenderer() {
        return new NightVisionGogglesRedRenderer();
    }
}
