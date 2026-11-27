package uk.iwaservice.sbwarmory.client;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModelInstance;
import com.atsuishio.superbwarfare.client.renderer.entity.BasicVehicleRenderer;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.event.ClientEventHandler;
import com.github.mcmodderanchor.simplebedrockmodel.v2.common.model.runtime.BoneState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

/**
 * SBW 0.8.9.1+ renderer for the assault boat. SBW only drives one turret per vehicle (here the
 * front RWS), so the aft MG bones are turned here every frame from the aft gunner's own view, the
 * same way SBW turns turret/barrel from turretYRot/turretXRot. Also does what SBW's SpeedBoatRenderer
 * does (propellers, rudder, hiding the RWS while its gunner zooms) - that class is final in Kotlin.
 */
public class AssaultBoatRenderer extends BasicVehicleRenderer {

    private static final int AFT_GUNNER_SEAT = 2; // Seats[2] in data/sbwarmory/sbw/vehicles/assault_boat.json
    // Seat 2's MinPitch/MaxPitch (-20 / 50) as MC xRot (negative = up).
    private static final float AFT_MAX_UP = 50F;
    private static final float AFT_MAX_DOWN = 20F;

    public AssaultBoatRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return true;
    }

    @Override
    public void transformCustomModelPart(VehicleEntity entity, VehicleModelInstance instance, PoseStack poseStack,
                                         float entityYaw, float partialTicks) {
        super.transformCustomModelPart(entity, instance, poseStack, entityYaw, partialTicks);
        if (entity.isWreck()) {
            return;
        }

        float propellerRot = Mth.lerp(partialTicks, entity.getPropellerRotO(), entity.getPropellerRot());
        float rudderRot = Mth.lerp(partialTicks, entity.getRudderRotO(), entity.getRudderRot());
        BoneState propeller = instance.getBone("move_propeller");
        if (propeller != null) {
            propeller.rotation.rotationZ(propellerRot);
        }
        BoneState propeller2 = instance.getBone("move_propeller2");
        if (propeller2 != null) {
            propeller2.rotation.rotationZ(-propellerRot);
        }
        BoneState control = instance.getBone("move_control");
        if (control != null) {
            control.rotation.rotationZ(-4 * rudderRot);
        }
        BoneState rudder = instance.getBone("move_rudder");
        if (rudder != null) {
            rudder.rotation.rotationY(rudderRot);
        }
        BoneState turret = instance.getBone("turret");
        if (turret != null && ClientEventHandler.zoomVehicle
                && entity.getNthEntity(entity.getTurretControllerIndex()) == Minecraft.getInstance().player) {
            turret.visible = false;
        }

        // Aft MG: the gun is modeled facing forward under the fixed 180-degree aftFlip bone, so at
        // rest (no gunner) it points astern. Yaw/pitch use the same conventions as SBW's turret and
        // barrel bones: yaw relative to the boat in MC degrees, pitch as -xRot.
        Entity gunner = entity.getNthEntity(AFT_GUNNER_SEAT);
        if (gunner == null) {
            return;
        }
        float boatYaw = Mth.rotLerp(partialTicks, entity.yRotO, entity.getYRot());
        float yaw = Mth.wrapDegrees(gunner.getViewYRot(partialTicks) - boatYaw - 180F);
        float pitch = Mth.clamp(gunner.getViewXRot(partialTicks), -AFT_MAX_UP, AFT_MAX_DOWN);
        BoneState aftYaw = instance.getBone("aftYaw");
        if (aftYaw != null) {
            aftYaw.rotation.rotationY(yaw * Mth.DEG_TO_RAD);
        }
        BoneState aftPitch = instance.getBone("aftPitch");
        if (aftPitch != null) {
            aftPitch.rotation.rotationX(-pitch * Mth.DEG_TO_RAD);
        }
    }
}
