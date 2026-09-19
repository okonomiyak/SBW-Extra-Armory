package uk.iwaservice.sbwarmory.client;

import uk.iwaservice.sbwarmory.SbwArmoryMod;
import uk.iwaservice.sbwarmory.item.SrawMissileItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class SrawMissileItemModel extends GeoModel<SrawMissileItem> {

    private static final ResourceLocation MODEL = new ResourceLocation(SbwArmoryMod.MODID, "geo/sraw_missile.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(SbwArmoryMod.MODID, "textures/item/sraw_missile.png");
    private static final ResourceLocation ANIMATION = new ResourceLocation(SbwArmoryMod.MODID, "animations/sraw_missile.animation.json");

    @Override
    public ResourceLocation getModelResource(SrawMissileItem animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(SrawMissileItem animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(SrawMissileItem animatable) {
        return ANIMATION;
    }
}
