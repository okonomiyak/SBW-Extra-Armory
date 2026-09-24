package uk.iwaservice.sbwarmory.item;

import com.atsuishio.superbwarfare.data.gun.GunProp;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Just having this anywhere in the inventory keeps every weapon (and the smoke/chaff decoy
 * launcher, if any) on whatever SuperbWarfare vehicle the owner is currently riding topped up -
 * same "just carry it" idea as {@link InfiniteAmmoBoxItem}, but for vehicle-tracked ammo instead
 * of the player's own backpack ammo pool.
 */
public class VehicleAmmoBoxItem extends Item {

    // Weapons with no magazine (MAGAZINE <= 0 - GunData#useBackpackAmmo()) fire straight off
    // backupAmmoCount instead, which has no capacity prop to read - just force it high.
    private static final int VEHICLE_BACKUP_AMMO = 999999;

    public VehicleAmmoBoxItem(Properties properties) {
        super(properties);
    }

    /**
     * Fires for every stack in the inventory each tick, not just the one held. Tops up: weapon
     * magazine capacity (or backup ammo count, for weapons that fire straight from reserve instead)
     * per weapon slot, and decoy count up to its configured magazine size.
     */
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide || !(entity instanceof Player player)) {
            return;
        }
        if (!(player.getVehicle() instanceof VehicleEntity vehicle)) {
            return;
        }

        for (String weapon : vehicle.getGunDataMap().keySet()) {
            vehicle.modifyGunData(weapon, gunData -> {
                int magazine = gunData.get(GunProp.MAGAZINE);
                if (gunData.ammo.get() < magazine) {
                    gunData.ammo.set(magazine);
                }
                if (gunData.backupAmmoCount.get() < VEHICLE_BACKUP_AMMO) {
                    gunData.backupAmmoCount.set(VEHICLE_BACKUP_AMMO);
                }
            });
        }

        // Smoke/chaff decoy launchers are tracked entirely separately from GunData - a plain
        // synced int on the vehicle itself, capped by DefaultVehicleData's configured magazine size
        // (0 for vehicles with no decoy hardware, so this is a no-op for those).
        int decoyMax = vehicle.computed().getDecoyMagazineSize();
        if (vehicle.getDecoyCount() < decoyMax) {
            vehicle.setDecoyCount(decoyMax);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.sbwarmory.vehicle_ammo_box.tooltip"));
    }
}
