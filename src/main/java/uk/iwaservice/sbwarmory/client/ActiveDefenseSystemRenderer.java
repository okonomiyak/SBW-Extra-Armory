package uk.iwaservice.sbwarmory.client;

import uk.iwaservice.sbwarmory.entity.ActiveDefenseSystemEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/** Stationary device - no facing/rotation logic needed, unlike our thrown projectiles. */
public class ActiveDefenseSystemRenderer extends GeoEntityRenderer<ActiveDefenseSystemEntity> {

    public ActiveDefenseSystemRenderer(EntityRendererProvider.Context context) {
        super(context, new ActiveDefenseSystemModel());
    }
}
