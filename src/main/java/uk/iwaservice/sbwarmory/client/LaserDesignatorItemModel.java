package uk.iwaservice.sbwarmory.client;

import uk.iwaservice.sbwarmory.SbwArmoryMod;
import uk.iwaservice.sbwarmory.item.LaserDesignatorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class LaserDesignatorItemModel extends GeoModel<LaserDesignatorItem> {

    private static final ResourceLocation MODEL = new ResourceLocation(SbwArmoryMod.MODID, "geo/soflam.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(SbwArmoryMod.MODID, "textures/item/soflam.png");
    private static final ResourceLocation ANIMATION = new ResourceLocation(SbwArmoryMod.MODID, "animations/soflam.animation.json");

    @Override
    public ResourceLocation getModelResource(LaserDesignatorItem animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(LaserDesignatorItem animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(LaserDesignatorItem animatable) {
        return ANIMATION;
    }
}
