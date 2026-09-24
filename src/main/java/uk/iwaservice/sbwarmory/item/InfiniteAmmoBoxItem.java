package uk.iwaservice.sbwarmory.item;

import com.atsuishio.superbwarfare.data.gun.Ammo;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Right-click: refills the wielder's SuperbWarfare ammo pool for one selected {@link Ammo} type to
 * its cap, without ever being consumed. Sneak-right-click cycles which type it's set to.
 */
public class InfiniteAmmoBoxItem extends Item {

    private static final String TAG_TYPE = "AmmoType";
    private static final Ammo[] TYPES = Ammo.values();

    public InfiniteAmmoBoxItem(Properties properties) {
        super(properties);
    }

    private static Ammo typeOf(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(TAG_TYPE)) {
            try {
                return Ammo.valueOf(tag.getString(TAG_TYPE));
            } catch (IllegalArgumentException ignored) {
                // fall through to default
            }
        }
        return TYPES[0];
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                Ammo next = TYPES[(typeOf(stack).ordinal() + 1) % TYPES.length];
                stack.getOrCreateTag().putString(TAG_TYPE, next.name());
                player.displayClientMessage(Component.translatable(next.translationKey).withStyle(next.color), true);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }

        if (!level.isClientSide) {
            Ammo type = typeOf(stack);
            type.set(player, type.getLimit());
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        Ammo type = typeOf(stack);
        tooltip.add(Component.translatable(type.translationKey).withStyle(type.color));
        tooltip.add(Component.translatable("item.sbwarmory.infinite_ammo_box.tooltip").withStyle(ChatFormatting.GRAY));
    }
}
