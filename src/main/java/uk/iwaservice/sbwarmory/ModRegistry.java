package uk.iwaservice.sbwarmory;

import uk.iwaservice.sbwarmory.block.ConcreteBarrierBlock;
import uk.iwaservice.sbwarmory.entity.ClusterGrenadeEntity;
import uk.iwaservice.sbwarmory.entity.CruiseMissileEntity;
import uk.iwaservice.sbwarmory.entity.MushroomCloudEntity;
import uk.iwaservice.sbwarmory.entity.SmokeClusterGrenadeEntity;
import uk.iwaservice.sbwarmory.entity.SpringGrenadeEntity;
import uk.iwaservice.sbwarmory.entity.SrawMissileEntity;
import uk.iwaservice.sbwarmory.entity.ThrowingKnifeEntity;
import uk.iwaservice.sbwarmory.item.ClusterGrenadeItem;
import uk.iwaservice.sbwarmory.item.GrenadeCarbineItem;
import uk.iwaservice.sbwarmory.item.LaserDesignatorItem;
import uk.iwaservice.sbwarmory.item.SmokeClusterGrenadeItem;
import uk.iwaservice.sbwarmory.item.SpringGrenadeItem;
import uk.iwaservice.sbwarmory.item.SrawLauncherItem;
import uk.iwaservice.sbwarmory.item.SrawMissileItem;
import uk.iwaservice.sbwarmory.item.ThrowingKnifeItem;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModRegistry {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, SbwArmoryMod.MODID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, SbwArmoryMod.MODID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, SbwArmoryMod.MODID);
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SbwArmoryMod.MODID);
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, SbwArmoryMod.MODID);
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, SbwArmoryMod.MODID);

    public static final RegistryObject<SimpleParticleType> MUSHROOM_PUFF = PARTICLE_TYPES.register("mushroom_puff",
            () -> new SimpleParticleType(false));

    public static final RegistryObject<SimpleParticleType> ROCK_DEBRIS = PARTICLE_TYPES.register("rock_debris",
            () -> new SimpleParticleType(false));

    public static final RegistryObject<SoundEvent> AIR_RAID_SIREN = SOUND_EVENTS.register("air_raid_siren",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(SbwArmoryMod.MODID, "air_raid_siren")));

    // Points at SuperbWarfare's own "senpai" mob voice line (superbwarfare:yarimasune) via our own
    // sounds.json entry - SBW never registered it as a standalone SoundEvent (only as one of three
    // random picks inside their own "idle" sound), so we register our own event for it here.
    public static final RegistryObject<SoundEvent> YARIMASUNE = SOUND_EVENTS.register("yarimasune",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(SbwArmoryMod.MODID, "yarimasune")));

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

    public static final RegistryObject<Item> GRENADE_CARBINE_BLUEPRINT_ITEM = ITEMS.register("grenade_carbine_blueprint",
            () -> new Item(new Item.Properties().stacksTo(1)));

    /** Launcher ammo - not throwable, plain flat icon (distinct from the hand-thrown item above). */
    public static final RegistryObject<Item> CLUSTER_GRENADE_AMMO_ITEM = ITEMS.register("cluster_grenade_ammo",
            () -> new Item(new Item.Properties().stacksTo(16)));

    /** Launcher ammo - not throwable, plain flat icon (distinct from the hand-thrown item above). */
    public static final RegistryObject<Item> CLUSTER_SMOKE_AMMO_ITEM = ITEMS.register("cluster_smoke_ammo",
            () -> new Item(new Item.Properties().stacksTo(16)));

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

    public static final RegistryObject<Item> SRAW_MISSILE_BLUEPRINT_ITEM = ITEMS.register("sraw_missile_blueprint",
            () -> new Item(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<EntityType<SrawMissileEntity>> SRAW_MISSILE = ENTITY_TYPES.register(
            "sraw_missile",
            () -> EntityType.Builder.<SrawMissileEntity>of(SrawMissileEntity::new, MobCategory.MISC)
                    .sized(0.6f, 0.6f)
                    .clientTrackingRange(8)
                    .updateInterval(5)
                    .build("sraw_missile"));

    public static final RegistryObject<Item> LASER_DESIGNATOR_ITEM = ITEMS.register("laser_designator",
            () -> new LaserDesignatorItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> BEAST_MISSILE_DESIGNATOR_ITEM = ITEMS.register("beast_missile_designator",
            () -> new uk.iwaservice.sbwarmory.item.BeastMissileDesignatorItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> NIGHT_VISION_GOGGLES_ITEM = ITEMS.register("night_vision_goggles",
            () -> new uk.iwaservice.sbwarmory.item.NightVisionGogglesItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> NIGHT_VISION_GOGGLES_RED_ITEM = ITEMS.register("night_vision_goggles_red",
            () -> new uk.iwaservice.sbwarmory.item.NightVisionGogglesRedItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> HELMET_BLUE_ITEM = ITEMS.register("helmet_blue",
            () -> new uk.iwaservice.sbwarmory.item.HelmetItem(new Item.Properties().stacksTo(1),
                    new ResourceLocation(SbwArmoryMod.MODID, "geo/armor/helmet_blue.geo.json"),
                    new ResourceLocation(SbwArmoryMod.MODID, "textures/armor/helmet_blue.png")));

    public static final RegistryObject<Item> HELMET_RED_ITEM = ITEMS.register("helmet_red",
            () -> new uk.iwaservice.sbwarmory.item.HelmetItem(new Item.Properties().stacksTo(1),
                    new ResourceLocation(SbwArmoryMod.MODID, "geo/armor/helmet_red.geo.json"),
                    new ResourceLocation(SbwArmoryMod.MODID, "textures/armor/helmet_red.png")));

    public static final RegistryObject<Block> CONCRETE_BARRIER = BLOCKS.register("concrete_barrier",
            () -> new ConcreteBarrierBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(5.0f, 30.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)));

    public static final RegistryObject<Item> CONCRETE_BARRIER_ITEM = ITEMS.register("concrete_barrier",
            () -> new BlockItem(CONCRETE_BARRIER.get(), new Item.Properties()));

    public static final RegistryObject<Block> CONCRETE_BLOCK = BLOCKS.register("concrete_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(5.0f, 30.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)));

    public static final RegistryObject<Item> CONCRETE_BLOCK_ITEM = ITEMS.register("concrete_block",
            () -> new BlockItem(CONCRETE_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<EntityType<CruiseMissileEntity>> CRUISE_MISSILE = ENTITY_TYPES.register(
            "cruise_missile",
            () -> EntityType.Builder.<CruiseMissileEntity>of(CruiseMissileEntity::new, MobCategory.MISC)
                    .sized(0.6f, 0.6f)
                    .clientTrackingRange(10)
                    .updateInterval(5)
                    .build("cruise_missile"));

    public static final RegistryObject<EntityType<uk.iwaservice.sbwarmory.entity.BeastMissileEntity>> BEAST_MISSILE = ENTITY_TYPES.register(
            "beast_missile",
            () -> EntityType.Builder.<uk.iwaservice.sbwarmory.entity.BeastMissileEntity>of(
                            uk.iwaservice.sbwarmory.entity.BeastMissileEntity::new, MobCategory.MISC)
                    .sized(0.6f, 0.6f)
                    .clientTrackingRange(10)
                    .updateInterval(5)
                    .build("beast_missile"));

    public static final RegistryObject<EntityType<uk.iwaservice.sbwarmory.entity.ActiveDefenseSystemEntity>> ACTIVE_DEFENSE_SYSTEM = ENTITY_TYPES.register(
            "active_defense_system",
            () -> EntityType.Builder.<uk.iwaservice.sbwarmory.entity.ActiveDefenseSystemEntity>of(
                            uk.iwaservice.sbwarmory.entity.ActiveDefenseSystemEntity::new, MobCategory.MISC)
                    .sized(0.75f, 0.75f)
                    .clientTrackingRange(10)
                    .updateInterval(20)
                    .build("active_defense_system"));

    public static final RegistryObject<Item> ACTIVE_DEFENSE_SYSTEM_ITEM = ITEMS.register("active_defense_system",
            () -> new uk.iwaservice.sbwarmory.item.ActiveDefenseSystemItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<EntityType<MushroomCloudEntity>> MUSHROOM_CLOUD = ENTITY_TYPES.register(
            "mushroom_cloud",
            () -> EntityType.Builder.<MushroomCloudEntity>of(MushroomCloudEntity::new, MobCategory.MISC)
                    .sized(0.1f, 0.1f)
                    .clientTrackingRange(10)
                    .updateInterval(Integer.MAX_VALUE)
                    .build("mushroom_cloud"));

    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.smokelauncher"))
            .icon(() -> new ItemStack(TAB_ICON_ITEM.get()))
            .displayItems((params, output) -> {
                output.accept(SMOKE_CLUSTER_GRENADE_ITEM.get());
                output.accept(CLUSTER_GRENADE_ITEM.get());
                output.accept(SPRING_GRENADE_ITEM.get());
                output.accept(THROWING_KNIFE_ITEM.get());
                output.accept(GRENADE_CARBINE_ITEM.get());
                output.accept(GRENADE_CARBINE_BLUEPRINT_ITEM.get());
                output.accept(CLUSTER_GRENADE_AMMO_ITEM.get());
                output.accept(CLUSTER_SMOKE_AMMO_ITEM.get());
                output.accept(SRAW_LAUNCHER_ITEM.get());
                output.accept(SRAW_MISSILE_ITEM.get());
                output.accept(SRAW_MISSILE_BLUEPRINT_ITEM.get());
                output.accept(LASER_DESIGNATOR_ITEM.get());
                output.accept(BEAST_MISSILE_DESIGNATOR_ITEM.get());
                output.accept(ACTIVE_DEFENSE_SYSTEM_ITEM.get());
                output.accept(NIGHT_VISION_GOGGLES_ITEM.get());
                output.accept(NIGHT_VISION_GOGGLES_RED_ITEM.get());
                output.accept(HELMET_BLUE_ITEM.get());
                output.accept(HELMET_RED_ITEM.get());
                output.accept(CONCRETE_BARRIER_ITEM.get());
                output.accept(CONCRETE_BLOCK_ITEM.get());
            })
            .build());

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
        BLOCKS.register(modBus);
        ENTITY_TYPES.register(modBus);
        TABS.register(modBus);
        PARTICLE_TYPES.register(modBus);
        SOUND_EVENTS.register(modBus);
    }

    private ModRegistry() {}
}
