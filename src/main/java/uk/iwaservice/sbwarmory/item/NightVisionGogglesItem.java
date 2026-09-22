package uk.iwaservice.sbwarmory.item;

import uk.iwaservice.sbwarmory.client.NightVisionGogglesRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

/** A real helmet-slot item now, not a Curios accessory - equip it in the armor slot like any other helmet. */
public class NightVisionGogglesItem extends ArmorItem implements GeoItem {

    private static final int EFFECT_REFRESH_TICKS = 220; // reapplied every tick while worn and active
    private static final String TAG_ACTIVE = "Active";

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public NightVisionGogglesItem(Properties properties) {
        super(ArmorMaterials.LEATHER, Type.HELMET, properties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    /** Defaults to on (no tag yet) so a freshly worn pair works immediately - the key just lets you kill it. */
    public static boolean isActive(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null || !tag.contains(TAG_ACTIVE) || tag.getBoolean(TAG_ACTIVE);
    }

    public static void toggleActive(ItemStack stack) {
        stack.getOrCreateTag().putBoolean(TAG_ACTIVE, !isActive(stack));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide || !(entity instanceof Player player)) {
            return;
        }
        if (player.getItemBySlot(EquipmentSlot.HEAD) != stack || !isActive(stack)) {
            return;
        }
        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, EFFECT_REFRESH_TICKS, 0, true, false));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Static model, no animations.
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    /** Overridden by color variants to swap in their own renderer without repeating the plumbing below. */
    protected GeoArmorRenderer<NightVisionGogglesItem> createRenderer() {
        return new NightVisionGogglesRenderer();
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        super.initializeClient(consumer);
        consumer.accept(new IClientItemExtensions() {
            private GeoArmorRenderer<NightVisionGogglesItem> renderer;

            @Override
            @OnlyIn(Dist.CLIENT)
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack,
                    EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                if (renderer == null) {
                    renderer = createRenderer();
                }
                renderer.prepForRender(livingEntity, itemStack, equipmentSlot, original);
                return renderer;
            }
        });
    }
}
