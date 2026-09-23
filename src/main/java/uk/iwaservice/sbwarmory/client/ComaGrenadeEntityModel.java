package uk.iwaservice.sbwarmory.client;

import uk.iwaservice.sbwarmory.SbwArmoryMod;
import uk.iwaservice.sbwarmory.entity.ComaGrenadeEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/** Reuses the smoke cluster grenade's shape (same geo file) - only the texture (blue) differs. */
public class ComaGrenadeEntityModel extends GeoModel<ComaGrenadeEntity> {

    private static final ResourceLocation MODEL = new ResourceLocation(SbwArmoryMod.MODID, "geo/smoke_cluster_grenade.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(SbwArmoryMod.MODID, "textures/item/coma_grenade.png");
    private static final ResourceLocation ANIMATION = new ResourceLocation(SbwArmoryMod.MODID, "animations/smoke_cluster_grenade.animation.json");

    @Override
    public ResourceLocation getModelResource(ComaGrenadeEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(ComaGrenadeEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(ComaGrenadeEntity animatable) {
        return ANIMATION;
    }
}
