package uk.iwaservice.sbwarmory.client;

import uk.iwaservice.sbwarmory.SbwArmoryMod;
import uk.iwaservice.sbwarmory.item.NightVisionGogglesItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class NightVisionGogglesRedArmorModel extends GeoModel<NightVisionGogglesItem> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(SbwArmoryMod.MODID, "geo/armor/night_vision_goggles_red.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(SbwArmoryMod.MODID, "textures/armor/night_vision_goggles_red.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(SbwArmoryMod.MODID, "animations/night_vision_goggles.animation.json");

    @Override
    public ResourceLocation getModelResource(NightVisionGogglesItem animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(NightVisionGogglesItem animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(NightVisionGogglesItem animatable) {
        return ANIMATION;
    }
}
