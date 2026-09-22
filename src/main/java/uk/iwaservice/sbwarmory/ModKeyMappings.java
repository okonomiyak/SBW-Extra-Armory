package uk.iwaservice.sbwarmory;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = SbwArmoryMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModKeyMappings {

    private static final String CATEGORY = "key.categories.sbwarmory";

    public static final KeyMapping TOGGLE_NIGHT_VISION = new KeyMapping(
            "key.sbwarmory.toggle_night_vision", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_N, CATEGORY);

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_NIGHT_VISION);
    }

    private ModKeyMappings() {}
}
