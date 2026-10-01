package uk.iwaservice.sbwarmory;

import uk.iwaservice.sbwarmory.network.ModNetworking;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/** SuperbWarfare addon: adds extra weapons (grenades, a grenade-launching carbine, throwing knife, guided SRAW launcher). */
@Mod(SbwArmoryMod.MODID)
public class SbwArmoryMod {
    public static final String MODID = "sbwarmory";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SbwArmoryMod() {
        var modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModRegistry.register(modBus);
        // Lists our vehicles in SBW's own container tab, like SBW does for its built-in cars.
        modBus.addListener((com.atsuishio.superbwarfare.api.event.RegisterContainersEvent e) -> {
            e.add(ModRegistry.PROBOX_WAGON);
            e.add(ModRegistry.HIACE_VAN);
            e.add(ModRegistry.LUXURY_SEDAN);
            e.add(ModRegistry.PROBOX_MG);
            e.add(ModRegistry.HOVERCRAFT);
            e.add(ModRegistry.ASSAULT_BOAT);
        });
        ModNetworking.register();
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(SbwCompat::onLoadingJson);
        ModLoadingContext.get().registerConfig(Type.SERVER, uk.iwaservice.sbwarmory.ModConfig.SPEC);
    }
}
