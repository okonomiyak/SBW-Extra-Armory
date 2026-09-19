package uk.iwaservice.sbwarmory.client;

import uk.iwaservice.sbwarmory.entity.ThrowingKnifeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * {@code GeoEntityRenderer}'s automatic facing logic only fires for {@code LivingEntity} (it reads
 * yBodyRot, which non-living entities don't have) - for a plain {@code ProjectileEntity} it never
 * rotates the model at all, so we apply the entity's own (interpolated) yaw/pitch ourselves here,
 * the same way vanilla's ArrowRenderer does for thrown projectiles.
 */
public class ThrowingKnifeEntityRenderer extends GeoEntityRenderer<ThrowingKnifeEntity> {

    // Extra local-axis correction for the blade's orientation within the model, on top of facing
    // the flight direction. Was previously guessed at 90 while direction-tracking was broken (so
    // that guess is meaningless) - reset to 0, tune once direction-tracking itself looks right.
    private static final float LOCAL_YAW_CORRECTION = 90f;

    public ThrowingKnifeEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new ThrowingKnifeEntityModel());
    }

    @Override
    public void preRender(PoseStack poseStack, ThrowingKnifeEntity animatable, BakedGeoModel model,
                           MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender,
                           float partialTick, int packedLight, int packedOverlay,
                           float red, float green, float blue, float alpha) {
        poseStack.mulPose(Axis.YP.rotationDegrees(animatable.getViewYRot(partialTick) - 90f + LOCAL_YAW_CORRECTION));
        poseStack.mulPose(Axis.ZP.rotationDegrees(animatable.getViewXRot(partialTick)));
        poseStack.scale(0.25f, 0.25f, 0.25f);
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick,
                packedLight, packedOverlay, red, green, blue, alpha);
    }
}
