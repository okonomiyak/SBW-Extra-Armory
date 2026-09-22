package uk.iwaservice.sbwarmory;

import net.minecraftforge.common.ForgeConfigSpec;

/** Server-authoritative - all of this only ever gets read from server-side code. */
public final class ModConfig {

    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.DoubleValue SIREN_VOLUME;
    public static final ForgeConfigSpec.DoubleValue CRUISE_MISSILE_FOOTPRINT;
    public static final ForgeConfigSpec.DoubleValue CRUISE_MISSILE_HEIGHT;
    public static final ForgeConfigSpec.DoubleValue CRUISE_MISSILE_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue CRUISE_MISSILE_VEHICLE_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue CRUISE_MISSILE_EXPLOSION_RADIUS;
    public static final ForgeConfigSpec.IntValue CRUISE_MISSILE_WARMUP_SECONDS;
    public static final ForgeConfigSpec.DoubleValue CRUISE_MISSILE_FALL_SPEED;

    public static final ForgeConfigSpec.DoubleValue ACTIVE_DEFENSE_RADIUS;
    public static final ForgeConfigSpec.IntValue ACTIVE_DEFENSE_COOLDOWN_SECONDS;
    public static final ForgeConfigSpec.IntValue ACTIVE_DEFENSE_HEALTH;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("cruiseMissile");
        CRUISE_MISSILE_FOOTPRINT = builder
                .comment("Side length in blocks of the guaranteed-kill square, e.g. 30 for a 30x30 area, 60 for 60x60.")
                .defineInRange("footprintSize", 30.0, 1.0, 500.0);
        CRUISE_MISSILE_HEIGHT = builder
                .comment("Vertical extent in blocks of the damage area, centered on the impact point.")
                .defineInRange("damageHeight", 24.0, 1.0, 500.0);
        CRUISE_MISSILE_DAMAGE = builder
                .comment("Damage dealt to living entities inside the footprint.")
                .defineInRange("damage", 114514.0, 0.0, 1000000.0);
        CRUISE_MISSILE_VEHICLE_DAMAGE = builder
                .comment("Damage dealt to SuperbWarfare vehicles inside the footprint.")
                .defineInRange("vehicleDamage", 114514.0, 0.0, 1000000.0);
        CRUISE_MISSILE_EXPLOSION_RADIUS = builder
                .comment("Radius of the actual block-destroying explosion at the impact point.")
                .defineInRange("explosionRadius", 15.0, 0.0, 500.0);
        CRUISE_MISSILE_WARMUP_SECONDS = builder
                .comment("How many seconds the missile waits, out of sight, before falling.")
                .defineInRange("warmupSeconds", 5, 0, 600);
        CRUISE_MISSILE_FALL_SPEED = builder
                .comment("Fall speed in blocks/tick once the warmup ends.")
                .defineInRange("fallSpeed", 4.0, 0.1, 100.0);
        builder.pop();

        builder.push("activeDefenseSystem");
        ACTIVE_DEFENSE_RADIUS = builder
                .comment("Detection radius in blocks - grenades entering this range are intercepted.")
                .defineInRange("radius", 5.0, 1.0, 100.0);
        ACTIVE_DEFENSE_COOLDOWN_SECONDS = builder
                .comment("Seconds between interceptions - only one grenade can be shot down per cooldown window.")
                .defineInRange("cooldownSeconds", 5, 0, 3600);
        ACTIVE_DEFENSE_HEALTH = builder
                .comment("Hit points of the placed device itself - it's fragile by design.")
                .defineInRange("health", 1, 1, 1000);
        builder.pop();

        SIREN_VOLUME = builder
                .comment("Volume multiplier for the cruise missile call-in's air raid siren. 0 mutes it, 1 is the default level.")
                .defineInRange("sirenVolume", 1.0, 0.0, 5.0);

        SPEC = builder.build();
    }

    private ModConfig() {}
}
