package uk.iwaservice.sbwarmory;

import com.atsuishio.superbwarfare.api.event.LoadingJsonEvent;
import com.atsuishio.superbwarfare.tools.OBB;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraftforge.fml.ModList;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

import java.util.Arrays;

/**
 * Lets one jar run on both SuperbWarfare 0.8.9.0 and 0.8.9.1+. Compiled against 0.8.9.1+; the few
 * APIs that differ are bridged here. Check with `gradlew compileJava -Psbw_file=8104849` (0.8.9.0).
 */
public final class SbwCompat {

    /**
     * 0.8.9.0 renders vehicles through GeckoLib (VehicleRenderer + VehicleModel); 0.8.9.1 switched to
     * BasicVehicleRenderer. 0.8.9.0's mods.toml reports itself as plain "0.8.9".
     */
    public static final boolean LEGACY_VEHICLE_RENDERING = ModList.get().getModContainerById("superbwarfare")
            .map(c -> c.getModInfo().getVersion().compareTo(new DefaultArtifactVersion("0.8.9.1")) < 0)
            .orElse(false);

    /** OBB part "Collision" only exists from 0.8.9.1; 0.8.9.0 fails to parse the whole vehicle file on it. */
    private static final boolean HAS_COLLISION_OBB = Arrays.stream(OBB.Part.values())
            .anyMatch(p -> p.name().equals("COLLISION"));

    /** ProjectileEntity's setGravity(float) was renamed setCustomGravity(float) in 0.8.9.1. */
    public static void disableProjectileGravity(Object projectile) {
        for (String name : new String[]{"setCustomGravity", "setGravity"}) {
            try {
                projectile.getClass().getMethod(name, float.class).invoke(projectile, 0f);
                return;
            } catch (NoSuchMethodException ignored) {
                // try the other name
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
        throw new IllegalStateException("No gravity setter on " + projectile.getClass());
    }

    /** Forge-bus listener: strips our "Collision" OBB entries before 0.8.9.0 parses our vehicle data. */
    public static void onLoadingJson(LoadingJsonEvent event) {
        if (HAS_COLLISION_OBB || !event.getId().startsWith(SbwArmoryMod.MODID + ":")) return;
        JsonObject json = event.getAsGsonObject();
        if (!(json.get("OBB") instanceof JsonArray obbs)) return;

        JsonArray kept = new JsonArray();
        for (JsonElement obb : obbs) {
            JsonElement part = obb.isJsonObject() ? obb.getAsJsonObject().get("Part") : null;
            if (part == null || !part.getAsString().equals("Collision")) kept.add(obb);
        }
        json.add("OBB", kept);
        event.setJsonStr(json.toString());
        SbwArmoryMod.LOGGER.debug("Stripped Collision OBBs from {} for SuperbWarfare 0.8.9.0", event.getId());
    }

    private SbwCompat() {}
}
