package uk.iwaservice.sbwarmory.client;

import uk.iwaservice.sbwarmory.entity.ComaGrenadeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * {@code ComaGrenadeEntity} extends SuperbWarfare's {@code HandGrenadeEntity}, which - unlike
 * {@code GunGrenadeEntity} - hardcodes its OWN model/texture resolution instead of deriving it from
 * the entity's registry name. Using SBW's generic {@code BasicProjectileRenderer} for it therefore
 * renders SBW's plain default hand grenade in flight, not our model (same reason
 * {@link SpringGrenadeEntityRenderer} exists) - so this uses GeckoLib's own renderer instead, which
 * resolves the model from the {@code GeoModel} we hand it directly, ignoring HandGrenadeEntity's
 * override entirely. Also needs the same manual facing fix as {@link ThrowingKnifeEntityRenderer}.
 */
public class ComaGrenadeEntityRenderer extends GeoEntityRenderer<ComaGrenadeEntity> {

    public ComaGrenadeEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new ComaGrenadeEntityModel());
    }

    @Override
    public void preRender(PoseStack poseStack, ComaGrenadeEntity animatable, BakedGeoModel model,
                           MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender,
                           float partialTick, int packedLight, int packedOverlay,
                           float red, float green, float blue, float alpha) {
        poseStack.mulPose(Axis.YP.rotationDegrees(animatable.getViewYRot(partialTick) - 90f));
        poseStack.mulPose(Axis.ZP.rotationDegrees(animatable.getViewXRot(partialTick)));
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick,
                packedLight, packedOverlay, red, green, blue, alpha);
    }
}
