package uk.iwaservice.sbwarmory.item;

import com.atsuishio.superbwarfare.client.GunRendererBuilder;
import com.atsuishio.superbwarfare.item.gun.GunGeoItem;
import uk.iwaservice.sbwarmory.client.SrawLauncherItemModel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import software.bernie.geckolib.renderer.GeoItemRenderer;

import java.util.function.Supplier;

/**
 * Shoulder-fired guided missile launcher: stats/ammo come from data/smokelauncher/sbw/guns/sraw_launcher.json,
 * firing {@link uk.iwaservice.sbwarmory.entity.SrawMissileEntity} which steers itself toward wherever
 * the shooter is currently looking.
 */
public class SrawLauncherItem extends GunGeoItem {

    public SrawLauncherItem() {
        super(new Item.Properties().rarity(Rarity.RARE));
    }

    @Override
    public Supplier<? extends GeoItemRenderer<? extends Item>> getRenderer() {
        return GunRendererBuilder.simple(SrawLauncherItemModel::new);
    }
}
