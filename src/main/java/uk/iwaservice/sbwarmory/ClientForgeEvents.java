package uk.iwaservice.sbwarmory;

import uk.iwaservice.sbwarmory.item.LaserDesignatorItem;
import uk.iwaservice.sbwarmory.item.NightVisionGogglesItem;
import uk.iwaservice.sbwarmory.network.FireCruiseMissilePacket;
import uk.iwaservice.sbwarmory.network.ModNetworking;
import uk.iwaservice.sbwarmory.network.ToggleNightVisionPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/** Forge-bus client events (as opposed to {@link ClientModEvents}, which needs the mod bus). */
@Mod.EventBusSubscriber(modid = SbwArmoryMod.MODID, value = Dist.CLIENT)
public final class ClientForgeEvents {

    private static final double MIN_ZOOM = 1.5;
    private static final double MAX_ZOOM = 8.0;
    private static final double ZOOM_STEP = 0.5;

    // Reusing SuperbWarfare's own spyglass/artillery-indicator scope mask rather than shipping new art.
    private static final ResourceLocation SCOPE_TEXTURE =
            new ResourceLocation("superbwarfare", "textures/overlay/spyglass/spyglass.png");
    private static final float SCOPE_SCALE = 1.35F;
    private static final float SCOPE_ASPECT = 21F / 9F; // the source texture is a wide oval, not square

    // Our own shader: vanilla's built-in "green" effect (zero red/blue, keep only green - the classic
    // NVG monochrome look) minus its "bits" pass, which posterizes/mosaics the image and looked too
    // coarse/blocky for this.
    private static final ResourceLocation NIGHT_VISION_SHADER =
            new ResourceLocation(SbwArmoryMod.MODID, "shaders/post/night_vision.json");

    private static double zoomLevel = MIN_ZOOM;
    private static int fireHoldTicks = 0;
    private static boolean fireConsumed = false;
    // Cancelling the Pre event below stops vanilla's KeyMapping from ever seeing the press, so
    // keyAttack.isDown() would never go true - track the raw button state ourselves instead.
    private static boolean leftMouseHeld = false;
    private static boolean nightVisionShaderActive = false;

    /** The player's currently-held {@link LaserDesignatorItem} stack while it's toggled to aim, or null. */
    private static ItemStack aimingStack(Player player) {
        if (player == null) {
            return null;
        }
        ItemStack main = player.getMainHandItem();
        if (main.getItem() instanceof LaserDesignatorItem && LaserDesignatorItem.isAiming(main)) {
            return main;
        }
        ItemStack off = player.getOffhandItem();
        if (off.getItem() instanceof LaserDesignatorItem && LaserDesignatorItem.isAiming(off)) {
            return off;
        }
        return null;
    }

    private static boolean wearingActiveGoggles(Player player) {
        if (player == null) {
            return false;
        }
        ItemStack head = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD);
        return head.getItem() instanceof NightVisionGogglesItem && NightVisionGogglesItem.isActive(head);
    }

    @SubscribeEvent
    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        if (aimingStack(Minecraft.getInstance().player) == null) {
            return;
        }
        event.setFOV(event.getFOV() / zoomLevel);
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (aimingStack(player) == null) {
            return;
        }

        GuiGraphics graphics = event.getGuiGraphics();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // Wide oval scope mask, same shape/texture SuperbWarfare's own spyglass and artillery
        // indicator use. Passing the draw size as the "texture size" too forces a full 0-1 UV
        // stretch, so this doesn't depend on the PNG's real resolution.
        float height = Math.min(screenWidth, screenHeight) * SCOPE_SCALE;
        float width = height * SCOPE_ASPECT;
        int x = Math.round((screenWidth - width) / 2);
        int y = Math.round((screenHeight - height) / 2);
        graphics.blit(SCOPE_TEXTURE, x, y, 0, 0, Math.round(width), Math.round(height), Math.round(width), Math.round(height));

        // Vanilla's own crosshair still renders underneath as normal - no need for a custom reticle.

        double distance = player.getEyePosition(1f).distanceTo(
                mc.level.clip(new ClipContext(player.getEyePosition(1f),
                        player.getEyePosition(1f).add(player.getViewVector(1f).scale(uk.iwaservice.sbwarmory.item.LaserDesignatorItem.MAX_RANGE)),
                        ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player)).getLocation());
        graphics.drawString(mc.font, Component.translatable("hud.sbwarmory.distance")
                        .append(Component.literal(String.format(": %.1fM", distance))),
                screenWidth / 2 + 12, screenHeight / 2 - 28, 0xFFAA00, false);

        if (fireHoldTicks > 0) {
            int barWidth = 100;
            int barHeight = 6;
            int bx = screenWidth / 2 - barWidth / 2;
            int by = screenHeight / 2 + 40;
            float progress = Math.min(1F, fireHoldTicks / (float) LaserDesignatorItem.LOCK_TICKS);
            graphics.fill(bx, by, bx + barWidth, by + barHeight, 0x80000000);
            graphics.fill(bx, by, bx + Math.round(barWidth * progress), by + barHeight, 0xFFFF3300);
        }
    }

    /** Scroll to zoom while aiming, instead of switching hotbar slots. */
    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        if (aimingStack(Minecraft.getInstance().player) == null) {
            return;
        }
        zoomLevel = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoomLevel + event.getScrollDelta() * ZOOM_STEP));
        event.setCanceled(true);
    }

    /** Suppress vanilla left-click (attack/mine) while aiming, so a long hold doesn't break blocks. */
    @SubscribeEvent
    public static void onMouseButton(InputEvent.MouseButton.Pre event) {
        if (event.getButton() != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return;
        }
        leftMouseHeld = event.getAction() != GLFW.GLFW_RELEASE;
        if (aimingStack(Minecraft.getInstance().player) != null) {
            event.setCanceled(true);
        }
    }

    /** Tracks how long left-click has been held while aiming; fires the call-in once it's full. */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();

        boolean wantsNightVisionShader = wearingActiveGoggles(mc.player) && mc.options.getCameraType().isFirstPerson();
        if (wantsNightVisionShader) {
            // Reasserted every tick (not just on the on/off edge): something else touching the
            // current post-process effect slot (another mod, or vanilla's own effect handling) can
            // silently knock ours out without notifying us, which left it stuck off on some setups.
            mc.gameRenderer.loadEffect(NIGHT_VISION_SHADER);
        } else if (nightVisionShaderActive) {
            mc.gameRenderer.shutdownEffect();
        }
        nightVisionShaderActive = wantsNightVisionShader;

        if (mc.player != null && mc.player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).getItem()
                instanceof NightVisionGogglesItem) {
            while (ModKeyMappings.TOGGLE_NIGHT_VISION.consumeClick()) {
                ModNetworking.CHANNEL.sendToServer(new ToggleNightVisionPacket());
            }
        }

        if (aimingStack(mc.player) == null || !leftMouseHeld) {
            fireHoldTicks = 0;
            fireConsumed = false;
            return;
        }
        if (fireConsumed) {
            return;
        }
        if (++fireHoldTicks >= LaserDesignatorItem.LOCK_TICKS) {
            ModNetworking.CHANNEL.sendToServer(new FireCruiseMissilePacket());
            fireConsumed = true;
        }
    }

    private ClientForgeEvents() {}
}
