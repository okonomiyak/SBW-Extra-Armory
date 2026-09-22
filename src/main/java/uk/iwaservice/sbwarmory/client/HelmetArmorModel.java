package uk.iwaservice.sbwarmory.client;

import uk.iwaservice.sbwarmory.SbwArmoryMod;
import uk.iwaservice.sbwarmory.item.HelmetItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/** Shared by every plain (no-effect) helmet color - just points at that color's model/texture. */
public class HelmetArmorModel extends GeoModel<HelmetItem> {

    private static final ResourceLocation ANIMATION =
            new ResourceLocation(SbwArmoryMod.MODID, "animations/night_vision_goggles.animation.json"); // empty, shared

    private final ResourceLocation model;
    private final ResourceLocation texture;

    public HelmetArmorModel(ResourceLocation model, ResourceLocation texture) {
        this.model = model;
        this.texture = texture;
    }

    @Override
    public ResourceLocation getModelResource(HelmetItem animatable) {
        return model;
    }

    @Override
    public ResourceLocation getTextureResource(HelmetItem animatable) {
        return texture;
    }

    @Override
    public ResourceLocation getAnimationResource(HelmetItem animatable) {
        return ANIMATION;
    }
}
