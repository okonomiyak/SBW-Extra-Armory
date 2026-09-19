package uk.iwaservice.sbwarmory.client;

import uk.iwaservice.sbwarmory.SbwArmoryMod;
import uk.iwaservice.sbwarmory.item.ThrowingKnifeItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ThrowingKnifeItemModel extends GeoModel<ThrowingKnifeItem> {

    private static final ResourceLocation MODEL = new ResourceLocation(SbwArmoryMod.MODID, "geo/throwing_knife.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(SbwArmoryMod.MODID, "textures/item/throwing_knife.png");
    private static final ResourceLocation ANIMATION = new ResourceLocation(SbwArmoryMod.MODID, "animations/throwing_knife.animation.json");

    @Override
    public ResourceLocation getModelResource(ThrowingKnifeItem animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(ThrowingKnifeItem animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(ThrowingKnifeItem animatable) {
        return ANIMATION;
    }
}
