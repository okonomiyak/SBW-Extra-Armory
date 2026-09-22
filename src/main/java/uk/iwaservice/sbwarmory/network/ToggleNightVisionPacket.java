package uk.iwaservice.sbwarmory.network;

import uk.iwaservice.sbwarmory.item.NightVisionGogglesItem;
import com.atsuishio.superbwarfare.init.ModSounds;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Sent when the player presses the night-vision toggle key while wearing the goggles. */
public final class ToggleNightVisionPacket {

    public static void encode(ToggleNightVisionPacket packet, FriendlyByteBuf buf) {}

    public static ToggleNightVisionPacket decode(FriendlyByteBuf buf) {
        return new ToggleNightVisionPacket();
    }

    public static void handle(ToggleNightVisionPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
            if (!(head.getItem() instanceof NightVisionGogglesItem)) {
                return;
            }
            NightVisionGogglesItem.toggleActive(head);
            boolean active = NightVisionGogglesItem.isActive(head);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.NIGHT_VISION_ACTIVATE.get(), SoundSource.PLAYERS, 0.6F, active ? 1.0F : 0.8F);
        });
        ctx.setPacketHandled(true);
    }
}
