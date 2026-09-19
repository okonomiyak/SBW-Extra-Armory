package uk.iwaservice.sbwarmory.client;

import com.atsuishio.superbwarfare.client.animation.AnimationHelper;
import com.atsuishio.superbwarfare.client.model.item.CustomGunModel;
import com.atsuishio.superbwarfare.client.overlay.CrossHairOverlay;
import com.atsuishio.superbwarfare.event.ClientEventHandler;
import uk.iwaservice.sbwarmory.item.GrenadeCarbineItem;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;

public class GrenadeCarbineItemModel extends CustomGunModel<GrenadeCarbineItem> {

    // ADS(覗き込み)時のオフセット。HK-416のアイアンサイト値をベースにしており、
    // 実機で照準がずれる場合はここを調整してください。
    private static final float ADS_X = 3.3055f;
    private static final float ADS_Y = 1.04f;
    private static final float ADS_Z = 3.0f;
    private static final float ADS_SCALE_Z = 0.2f;

    @Override
    public void setCustomAnimations(GrenadeCarbineItem animatable, long instanceId, AnimationState<GrenadeCarbineItem> animationState) {
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

        // 発砲時の反動 (HK-416と同じ係数)
        ClientEventHandler.handleShootAnimation(shen, 0.95f, -0.95f, 0.85f, 0.8f, 0.9f, 1, 0.5f, 0.75f);
        CrossHairOverlay.gunRot = shen.getRotZ();

        // 歩行・ダッシュ・ドロー時の揺れ
        ClientEventHandler.gunRootMove(getAnimationProcessor(), 2, 0, 0, false);

        // リロード時のカメラ/銃の揺れ
        float numR = (float) (1 - 0.985 * zt);
        float numP = (float) (1 - 0.92 * zt);
        AnimationHelper.handleReloadShakeAnimation(stack, main, camera, numR, numP);
        ClientEventHandler.handleReloadShake(Mth.RAD_TO_DEG * camera.getRotX(), Mth.RAD_TO_DEG * camera.getRotY(), Mth.RAD_TO_DEG * camera.getRotZ());
    }
}
