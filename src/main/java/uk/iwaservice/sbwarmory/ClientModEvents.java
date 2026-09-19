package uk.iwaservice.sbwarmory;

import com.atsuishio.superbwarfare.client.renderer.projectile.BasicProjectileRenderer;
import uk.iwaservice.sbwarmory.client.SpringGrenadeEntityRenderer;
import uk.iwaservice.sbwarmory.client.ThrowingKnifeEntityRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Mod-bus client events: entity renderer registration (needs the mod bus, not the Forge bus). */
@Mod.EventBusSubscriber(modid = SbwArmoryMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ClientModEvents {

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModRegistry.SMOKE_CLUSTER_GRENADE.get(), BasicProjectileRenderer::new);
        event.registerEntityRenderer(ModRegistry.CLUSTER_GRENADE.get(), BasicProjectileRenderer::new);
        event.registerEntityRenderer(ModRegistry.SPRING_GRENADE.get(), SpringGrenadeEntityRenderer::new);
        event.registerEntityRenderer(ModRegistry.THROWING_KNIFE.get(), ThrowingKnifeEntityRenderer::new);
        event.registerEntityRenderer(ModRegistry.SRAW_MISSILE.get(), BasicProjectileRenderer::new);
    }

    private ClientModEvents() {}
}
