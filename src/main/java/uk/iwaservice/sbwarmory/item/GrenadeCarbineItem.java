package uk.iwaservice.sbwarmory.item;

import com.atsuishio.superbwarfare.client.GunRendererBuilder;
import com.atsuishio.superbwarfare.item.gun.GunGeoItem;
import uk.iwaservice.sbwarmory.client.GrenadeCarbineItemModel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import software.bernie.geckolib.renderer.GeoItemRenderer;

import java.util.function.Supplier;

/**
 * Standalone grenade launcher: stats come from data/smokelauncher/sbw/guns/grenade_carbine.json,
 * single-shot, firing {@link uk.iwaservice.sbwarmory.entity.ClusterGrenadeEntity}.
 * Model/animation come from assets/smokelauncher/sbw/guns/grenade_carbine.json.
 */
public class GrenadeCarbineItem extends GunGeoItem {

    public GrenadeCarbineItem() {
        super(new Item.Properties().rarity(Rarity.RARE));
    }

    @Override
    public Supplier<? extends GeoItemRenderer<? extends Item>> getRenderer() {
        return GunRendererBuilder.simple(GrenadeCarbineItemModel::new);
    }
}
