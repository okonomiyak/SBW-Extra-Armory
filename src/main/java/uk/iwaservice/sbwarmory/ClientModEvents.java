package uk.iwaservice.sbwarmory;

import com.atsuishio.superbwarfare.client.renderer.entity.BasicVehicleRenderer;
import com.atsuishio.superbwarfare.client.renderer.projectile.BasicProjectileRenderer;
import uk.iwaservice.sbwarmory.client.ActiveDefenseSystemRenderer;
import uk.iwaservice.sbwarmory.client.ComaGrenadeEntityRenderer;
import uk.iwaservice.sbwarmory.client.MushroomCloudEntityRenderer;
import uk.iwaservice.sbwarmory.client.MushroomPuffParticle;
import uk.iwaservice.sbwarmory.client.RockDebrisParticle;
import uk.iwaservice.sbwarmory.client.SpringGrenadeEntityRenderer;
import uk.iwaservice.sbwarmory.client.ThrowingKnifeEntityRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
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
        event.registerEntityRenderer(ModRegistry.COMA_GRENADE.get(), ComaGrenadeEntityRenderer::new);
        event.registerEntityRenderer(ModRegistry.FLASHBANG_GRENADE.get(), uk.iwaservice.sbwarmory.client.FlashbangGrenadeEntityRenderer::new);
        event.registerEntityRenderer(ModRegistry.STUN_GRENADE.get(), uk.iwaservice.sbwarmory.client.StunGrenadeEntityRenderer::new);
        event.registerEntityRenderer(ModRegistry.THROWING_KNIFE.get(), ThrowingKnifeEntityRenderer::new);
        event.registerEntityRenderer(ModRegistry.SRAW_MISSILE.get(), BasicProjectileRenderer::new);
        event.registerEntityRenderer(ModRegistry.CRUISE_MISSILE.get(), BasicProjectileRenderer::new);
        event.registerEntityRenderer(ModRegistry.BEAST_MISSILE.get(), BasicProjectileRenderer::new);
        event.registerEntityRenderer(ModRegistry.MUSHROOM_CLOUD.get(), MushroomCloudEntityRenderer::new);
        event.registerEntityRenderer(ModRegistry.ACTIVE_DEFENSE_SYSTEM.get(), ActiveDefenseSystemRenderer::new);
        event.registerEntityRenderer(ModRegistry.PROBOX_WAGON.get(), BasicVehicleRenderer::new);
        event.registerEntityRenderer(ModRegistry.HIACE_VAN.get(), BasicVehicleRenderer::new);
        event.registerEntityRenderer(ModRegistry.LUXURY_SEDAN.get(), BasicVehicleRenderer::new);
        event.registerEntityRenderer(ModRegistry.PROBOX_MG.get(), BasicVehicleRenderer::new);
        // SBW's speedboat renderer spins move_propeller / move_rudder by bone name; no speedboat-specific casts.
        event.registerEntityRenderer(ModRegistry.HOVERCRAFT.get(), com.atsuishio.superbwarfare.client.renderer.entity.SpeedBoatRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModRegistry.MUSHROOM_PUFF.get(), MushroomPuffParticle.Provider::new);
        event.registerSpriteSet(ModRegistry.ROCK_DEBRIS.get(), RockDebrisParticle.Provider::new);
    }

    private ClientModEvents() {}
}
