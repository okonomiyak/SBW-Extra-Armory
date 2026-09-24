package uk.iwaservice.sbwarmory.item;

import com.atsuishio.superbwarfare.data.gun.Ammo;
import com.atsuishio.superbwarfare.data.gun.GunProp;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.item.ammo.AmmoSupplierItem;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Just having this anywhere in the inventory (not even in hand) keeps the owner's SuperbWarfare
 * ammo pool for one selected {@link Ammo} type topped up to its cap every tick - same "just carry
 * it" behavior as SBW's own Creative Ammo Box, but scoped to a single chosen ammo type instead of
 * all of them. Pick which type by picking this stack up (cursor) and clicking any SuperbWarfare
 * ammo/ammo-box item in an inventory slot - same UX as TACZ's own single-type ammo box.
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

    /** Pick this stack up (cursor) and click an ammo/ammo-box slot to set the type it distributes. */
    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
        if (action != ClickAction.SECONDARY || !(slot.getItem().getItem() instanceof AmmoSupplierItem supplier)) {
            return false;
        }
        Ammo type = supplier.getType();
        stack.getOrCreateTag().putString(TAG_TYPE, type.name());
        player.displayClientMessage(Component.translatable(type.translationKey).withStyle(type.color), true);
        return true;
    }

    /**
     * Fires for every stack in the inventory each tick, not just the one held - same as SBW's own
     * check. Guarded by get() so set() (which syncs the whole ammo capability) only actually runs
     * on the tick something drops below the cap, not every tick regardless.
     */
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide || !(entity instanceof Player player)) {
            return;
        }
        Ammo type = typeOf(stack);
        if (type.get(player) < type.getLimit()) {
            type.set(player, type.getLimit());
        }
        topUpVehicleWeapons(player);
    }

    /**
     * Vehicle-mounted weapons track their loaded magazine separately from the backpack ammo pool
     * above (SuperbWarfare's own GunData per weapon slot, not the player's Ammo capability), so this
     * tops each of the current vehicle's weapons back up to its magazine capacity too.
     */
    private static void topUpVehicleWeapons(Player player) {
        if (!(player.getVehicle() instanceof VehicleEntity vehicle)) {
            return;
        }
        for (String weapon : vehicle.getGunDataMap().keySet()) {
            vehicle.modifyGunData(weapon, gunData -> {
                int magazine = gunData.get(GunProp.MAGAZINE);
                if (gunData.ammo.get() < magazine) {
                    gunData.ammo.set(magazine);
                }
            });
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        Ammo type = typeOf(stack);
        tooltip.add(Component.translatable(type.translationKey).withStyle(type.color));
        tooltip.add(Component.translatable("item.sbwarmory.infinite_ammo_box.tooltip").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.sbwarmory.infinite_ammo_box.tooltip_switch").withStyle(ChatFormatting.DARK_GRAY));
    }
}
