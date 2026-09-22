package uk.iwaservice.sbwarmory.network;

import uk.iwaservice.sbwarmory.SbwArmoryMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetworking {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(SbwArmoryMod.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, FireCruiseMissilePacket.class,
                FireCruiseMissilePacket::encode, FireCruiseMissilePacket::decode, FireCruiseMissilePacket::handle);
        CHANNEL.registerMessage(id++, ToggleNightVisionPacket.class,
                ToggleNightVisionPacket::encode, ToggleNightVisionPacket::decode, ToggleNightVisionPacket::handle);
    }

    private ModNetworking() {}
}
