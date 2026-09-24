package uk.iwaservice.sbwarmory.item;

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
 * Just having this anywhere in the inventory keeps whatever SuperbWarfare vehicle the owner is
 * currently riding fully charged - same "just carry it" idea as {@link VehicleAmmoBoxItem}, but for
 * the vehicle's energy storage instead of its weapons/decoys.
 */
public class VehicleBatteryPackItem extends Item {

    public VehicleBatteryPackItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide || !(entity instanceof Player player)) {
            return;
        }
        if (!(player.getVehicle() instanceof VehicleEntity vehicle) || !vehicle.hasEnergyStorage()) {
            return;
        }
        if (vehicle.getEnergy() < vehicle.getMaxEnergy()) {
            vehicle.setEnergy(vehicle.getMaxEnergy());
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.sbwarmory.vehicle_battery_pack.tooltip"));
    }
}
