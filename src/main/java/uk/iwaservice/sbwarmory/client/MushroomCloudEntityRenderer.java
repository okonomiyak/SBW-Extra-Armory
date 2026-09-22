package uk.iwaservice.sbwarmory.client;

import uk.iwaservice.sbwarmory.SbwArmoryMod;
import uk.iwaservice.sbwarmory.entity.MushroomCloudEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

/**
 * Not GeckoLib - a plain camera-facing (yaw-only, stays upright) billboard quad, cycling through the
 * 6 vertically-stacked frames of mushroom_cloud.png as the entity ages, then fading out.
 */
public class MushroomCloudEntityRenderer extends EntityRenderer<MushroomCloudEntity> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(SbwArmoryMod.MODID, "textures/entity/mushroom_cloud.png");
    private static final int FRAME_COUNT = 6;
    private static final float SIZE = 32f; // blocks, per the asset's own notes
    private static final int FADE_TICKS = 20;

    public MushroomCloudEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(MushroomCloudEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(MushroomCloudEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                        MultiBufferSource buffer, int packedLight) {
        float age = entity.tickCount + partialTick;
        int frame = Math.min(FRAME_COUNT - 1, (int) (age * FRAME_COUNT / MushroomCloudEntity.LIFETIME_TICKS));
        float v0 = frame / (float) FRAME_COUNT;
        float v1 = (frame + 1) / (float) FRAME_COUNT;

        float fadeStart = MushroomCloudEntity.LIFETIME_TICKS - FADE_TICKS;
        float alpha = age > fadeStart ? Math.max(0f, 1f - (age - fadeStart) / FADE_TICKS) : 1f;

        poseStack.pushPose();
        // Yaw-only billboard: always faces the camera left/right, but stays upright like a real cloud.
        poseStack.mulPose(Axis.YP.rotationDegrees(-this.entityRenderDispatcher.camera.getYRot()));

        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
        Matrix4f matrix = poseStack.last().pose();
        float half = SIZE / 2f;

        vertex(consumer, matrix, -half, 0, 0f, v1, packedLight, alpha);
        vertex(consumer, matrix, half, 0, 1f, v1, packedLight, alpha);
        vertex(consumer, matrix, half, SIZE, 1f, v0, packedLight, alpha);
        vertex(consumer, matrix, -half, SIZE, 0f, v0, packedLight, alpha);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    private void vertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float u, float v, int light, float alpha) {
        consumer.vertex(matrix, x, y, 0)
                .color(1f, 1f, 1f, alpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(0, 1, 0)
                .endVertex();
    }
}
