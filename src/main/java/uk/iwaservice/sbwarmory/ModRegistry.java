package uk.iwaservice.sbwarmory;

import uk.iwaservice.sbwarmory.entity.ClusterGrenadeEntity;
import uk.iwaservice.sbwarmory.entity.SmokeClusterGrenadeEntity;
import uk.iwaservice.sbwarmory.entity.SpringGrenadeEntity;
import uk.iwaservice.sbwarmory.entity.SrawMissileEntity;
import uk.iwaservice.sbwarmory.entity.ThrowingKnifeEntity;
import uk.iwaservice.sbwarmory.item.ClusterGrenadeItem;
import uk.iwaservice.sbwarmory.item.GrenadeCarbineItem;
import uk.iwaservice.sbwarmory.item.SmokeClusterGrenadeItem;
import uk.iwaservice.sbwarmory.item.SpringGrenadeItem;
import uk.iwaservice.sbwarmory.item.SrawLauncherItem;
import uk.iwaservice.sbwarmory.item.SrawMissileItem;
import uk.iwaservice.sbwarmory.item.ThrowingKnifeItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModRegistry {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, SbwArmoryMod.MODID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, SbwArmoryMod.MODID);
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SbwArmoryMod.MODID);

    /** Not shown in the tab's item list - only used as the tab's own icon texture. */
    public static final RegistryObject<Item> TAB_ICON_ITEM = ITEMS.register("tab_icon", () -> new Item(new Item.Properties()));

    public static final RegistryObject<EntityType<SmokeClusterGrenadeEntity>> SMOKE_CLUSTER_GRENADE = ENTITY_TYPES.register(
            "smoke_cluster_grenade",
            () -> EntityType.Builder.<SmokeClusterGrenadeEntity>of(SmokeClusterGrenadeEntity::new, MobCategory.MISC)
                    .sized(0.2f, 0.2f)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("smoke_cluster_grenade"));

    public static final RegistryObject<Item> SMOKE_CLUSTER_GRENADE_ITEM = ITEMS.register("smoke_cluster_grenade",
            () -> new SmokeClusterGrenadeItem(new Item.Properties().stacksTo(16)));

    public static final RegistryObject<EntityType<ClusterGrenadeEntity>> CLUSTER_GRENADE = ENTITY_TYPES.register(
            "cluster_grenade",
            () -> EntityType.Builder.<ClusterGrenadeEntity>of(ClusterGrenadeEntity::new, MobCategory.MISC)
                    .sized(0.2f, 0.2f)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("cluster_grenade"));

    public static final RegistryObject<Item> CLUSTER_GRENADE_ITEM = ITEMS.register("cluster_grenade",
            () -> new ClusterGrenadeItem(new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> GRENADE_CARBINE_ITEM = ITEMS.register("grenade_carbine",
            GrenadeCarbineItem::new);

    public static final RegistryObject<Item> THROWING_KNIFE_ITEM = ITEMS.register("throwing_knife",
            () -> new ThrowingKnifeItem(new Item.Properties().stacksTo(16)));

    public static final RegistryObject<EntityType<ThrowingKnifeEntity>> THROWING_KNIFE = ENTITY_TYPES.register(
            "throwing_knife",
            () -> EntityType.Builder.<ThrowingKnifeEntity>of(ThrowingKnifeEntity::new, MobCategory.MISC)
                    .sized(0.2f, 0.2f)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("throwing_knife"));

    public static final RegistryObject<EntityType<SpringGrenadeEntity>> SPRING_GRENADE = ENTITY_TYPES.register(
            "spring_grenade",
            () -> EntityType.Builder.<SpringGrenadeEntity>of(SpringGrenadeEntity::new, MobCategory.MISC)
                    .sized(0.2f, 0.2f)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("spring_grenade"));

    public static final RegistryObject<Item> SPRING_GRENADE_ITEM = ITEMS.register("spring_grenade",
            () -> new SpringGrenadeItem(new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> SRAW_LAUNCHER_ITEM = ITEMS.register("sraw_launcher",
            SrawLauncherItem::new);

    public static final RegistryObject<Item> SRAW_MISSILE_ITEM = ITEMS.register("sraw_missile",
            () -> new SrawMissileItem(new Item.Properties().stacksTo(16)));

    public static final RegistryObject<EntityType<SrawMissileEntity>> SRAW_MISSILE = ENTITY_TYPES.register(
            "sraw_missile",
            () -> EntityType.Builder.<SrawMissileEntity>of(SrawMissileEntity::new, MobCategory.MISC)
                    .sized(0.6f, 0.6f)
                    .clientTrackingRange(8)
                    .updateInterval(5)
                    .build("sraw_missile"));

    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.smokelauncher"))
            .icon(() -> new ItemStack(TAB_ICON_ITEM.get()))
            .displayItems((params, output) -> {
                output.accept(SMOKE_CLUSTER_GRENADE_ITEM.get());
                output.accept(CLUSTER_GRENADE_ITEM.get());
                output.accept(SPRING_GRENADE_ITEM.get());
                output.accept(THROWING_KNIFE_ITEM.get());
                output.accept(GRENADE_CARBINE_ITEM.get());
                output.accept(SRAW_LAUNCHER_ITEM.get());
                output.accept(SRAW_MISSILE_ITEM.get());
            })
            .build());

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
        ENTITY_TYPES.register(modBus);
        TABS.register(modBus);
    }

    private ModRegistry() {}
}
