package uk.iwaservice.sbwarmory.network;

import uk.iwaservice.sbwarmory.item.LaserDesignatorItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Sent when the client's own hold-to-fire timer completes; the server re-validates and spawns the missile. */
public final class FireCruiseMissilePacket {

    public static void encode(FireCruiseMissilePacket packet, FriendlyByteBuf buf) {}

    public static FireCruiseMissilePacket decode(FriendlyByteBuf buf) {
        return new FireCruiseMissilePacket();
    }

    public static void handle(FireCruiseMissilePacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            var player = ctx.getSender();
            if (player != null) {
                LaserDesignatorItem.tryFireMissile(player);
            }
        });
        ctx.setPacketHandled(true);
    }
}
