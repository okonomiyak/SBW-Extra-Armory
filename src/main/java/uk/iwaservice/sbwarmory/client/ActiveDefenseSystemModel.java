package uk.iwaservice.sbwarmory.client;

import uk.iwaservice.sbwarmory.SbwArmoryMod;
import uk.iwaservice.sbwarmory.entity.ActiveDefenseSystemEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ActiveDefenseSystemModel extends GeoModel<ActiveDefenseSystemEntity> {

    private static final ResourceLocation MODEL = new ResourceLocation(SbwArmoryMod.MODID, "geo/active_defense_system.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(SbwArmoryMod.MODID, "textures/entity/active_defense_system.png");
    private static final ResourceLocation ANIMATION = new ResourceLocation(SbwArmoryMod.MODID, "animations/active_defense_system.animation.json");

    @Override
    public ResourceLocation getModelResource(ActiveDefenseSystemEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(ActiveDefenseSystemEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(ActiveDefenseSystemEntity animatable) {
        return ANIMATION;
    }
}
