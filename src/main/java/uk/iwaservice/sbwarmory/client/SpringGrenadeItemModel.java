package uk.iwaservice.sbwarmory.client;

import uk.iwaservice.sbwarmory.SbwArmoryMod;
import uk.iwaservice.sbwarmory.item.SpringGrenadeItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class SpringGrenadeItemModel extends GeoModel<SpringGrenadeItem> {

    private static final ResourceLocation MODEL = new ResourceLocation(SbwArmoryMod.MODID, "geo/spring_grenade.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(SbwArmoryMod.MODID, "textures/item/spring_grenade.png");
    private static final ResourceLocation ANIMATION = new ResourceLocation(SbwArmoryMod.MODID, "animations/spring_grenade.animation.json");

    @Override
    public ResourceLocation getModelResource(SpringGrenadeItem animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(SpringGrenadeItem animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(SpringGrenadeItem animatable) {
        return ANIMATION;
    }
}
