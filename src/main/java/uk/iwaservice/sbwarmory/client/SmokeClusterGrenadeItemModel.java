package uk.iwaservice.sbwarmory.client;

import uk.iwaservice.sbwarmory.SbwArmoryMod;
import uk.iwaservice.sbwarmory.item.SmokeClusterGrenadeItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class SmokeClusterGrenadeItemModel extends GeoModel<SmokeClusterGrenadeItem> {

    private static final ResourceLocation MODEL = new ResourceLocation(SbwArmoryMod.MODID, "geo/smoke_cluster_grenade.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(SbwArmoryMod.MODID, "textures/item/smoke_cluster_grenade.png");
    private static final ResourceLocation ANIMATION = new ResourceLocation(SbwArmoryMod.MODID, "animations/smoke_cluster_grenade.animation.json");

    @Override
    public ResourceLocation getModelResource(SmokeClusterGrenadeItem animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(SmokeClusterGrenadeItem animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(SmokeClusterGrenadeItem animatable) {
        return ANIMATION;
    }
}
