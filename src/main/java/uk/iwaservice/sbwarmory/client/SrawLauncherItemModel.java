package uk.iwaservice.sbwarmory.client;

import com.atsuishio.superbwarfare.client.animation.AnimationHelper;
import com.atsuishio.superbwarfare.client.model.item.CustomGunModel;
import com.atsuishio.superbwarfare.client.overlay.CrossHairOverlay;
import com.atsuishio.superbwarfare.event.ClientEventHandler;
import uk.iwaservice.sbwarmory.item.SrawLauncherItem;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;

/** Same ADS/recoil/reload-shake wiring as {@link GrenadeCarbineItemModel} - the rigged sraw.geo.json has the same bone set. */
public class SrawLauncherItemModel extends CustomGunModel<SrawLauncherItem> {

    // ADS(覗き込み)時のオフセット。仮値、実機で照準がずれる場合はここを調整してください。
    private static final float ADS_X = 3.3055f;
    private static final float ADS_Y = 1.04f;
    private static final float ADS_Z = 3.0f;
    private static final float ADS_SCALE_Z = 0.2f;

    @Override
    public void setCustomAnimations(SrawLauncherItem animatable, long instanceId, AnimationState<SrawLauncherItem> animationState) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        ItemStack stack = player.getMainHandItem();
        if (shouldCancelRender(stack, animationState)) return;

        CoreGeoBone gun = getAnimationProcessor().getBone("bone");
        CoreGeoBone shen = getAnimationProcessor().getBone("fireRootNormal");
        CoreGeoBone main = getAnimationProcessor().getBone("0");
        CoreGeoBone camera = getAnimationProcessor().getBone("camera");

        double zt = ClientEventHandler.zoomTime;
        double zp = ClientEventHandler.zoomPos;
        double zpz = ClientEventHandler.zoomPosZ;

        gun.setPosX(ADS_X * (float) zp);
        gun.setPosY(ADS_Y * (float) zp - (float) (0.2f * zpz));
        gun.setPosZ(ADS_Z * (float) zp + (float) (0.3f * zpz));
        gun.setRotZ((float) (0.05f * zpz));
        gun.setScaleZ(1f - (ADS_SCALE_Z * (float) zp));

        ClientEventHandler.handleShootAnimation(shen, 0.95f, -0.95f, 0.85f, 0.8f, 0.9f, 1, 0.5f, 0.75f);
        CrossHairOverlay.gunRot = shen.getRotZ();

        ClientEventHandler.gunRootMove(getAnimationProcessor(), 2, 0, 0, false);

        float numR = (float) (1 - 0.985 * zt);
        float numP = (float) (1 - 0.92 * zt);
        AnimationHelper.handleReloadShakeAnimation(stack, main, camera, numR, numP);
        ClientEventHandler.handleReloadShake(Mth.RAD_TO_DEG * camera.getRotX(), Mth.RAD_TO_DEG * camera.getRotY(), Mth.RAD_TO_DEG * camera.getRotZ());
    }
}
