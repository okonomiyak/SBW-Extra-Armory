package uk.iwaservice.sbwarmory.client;

import uk.iwaservice.sbwarmory.entity.SpringGrenadeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/** Same facing fix as {@link ThrowingKnifeEntityRenderer}: GeoEntityRenderer only auto-rotates LivingEntity. */
public class SpringGrenadeEntityRenderer extends GeoEntityRenderer<SpringGrenadeEntity> {

    public SpringGrenadeEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new SpringGrenadeEntityModel());
    }

    @Override
    public void preRender(PoseStack poseStack, SpringGrenadeEntity animatable, BakedGeoModel model,
                           MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender,
                           float partialTick, int packedLight, int packedOverlay,
                           float red, float green, float blue, float alpha) {
        poseStack.mulPose(Axis.YP.rotationDegrees(animatable.getViewYRot(partialTick) - 90f));
        poseStack.mulPose(Axis.ZP.rotationDegrees(animatable.getViewXRot(partialTick)));
        poseStack.scale(0.2f, 0.2f, 0.2f);
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick,
                packedLight, packedOverlay, red, green, blue, alpha);
    }
}
