package uk.iwaservice.sbwarmory;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
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
    }
}
