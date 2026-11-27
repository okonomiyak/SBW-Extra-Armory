package uk.iwaservice.sbwarmory;

import uk.iwaservice.sbwarmory.item.LaserDesignatorItem;
import uk.iwaservice.sbwarmory.item.NightVisionGogglesItem;
import uk.iwaservice.sbwarmory.network.FireCruiseMissilePacket;
import uk.iwaservice.sbwarmory.network.ModNetworking;
import uk.iwaservice.sbwarmory.network.ToggleNightVisionPacket;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
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
import org.joml.Matrix4f;
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

    // Night vision overlay (see drawNightVisionOverlay). Textures are generated, not hand-drawn:
    // nvg_noise is 256x256 gray around 128 (sd 17), nvg_vignette a 512x256 black alpha oval.
    private static final ResourceLocation NVG_NOISE =
            new ResourceLocation(SbwArmoryMod.MODID, "textures/overlay/nvg_noise.png");
    private static final ResourceLocation NVG_VIGNETTE =
            new ResourceLocation(SbwArmoryMod.MODID, "textures/overlay/nvg_vignette.png");
    private static final int NVG_NOISE_SIZE = 256;
    private static final float[] NVG_TINT = {0.22F, 1.0F, 0.12F};
    private static final float NVG_GAIN = 1.0F;
    private static final float[] NVG_LIFT = {0.01F, 0.045F, 0.0F};
    private static final RandomSource NOISE_RANDOM = RandomSource.create();

    private static double zoomLevel = MIN_ZOOM;
    private static int fireHoldTicks = 0;
    private static boolean fireConsumed = false;
    // Cancelling the Pre event below stops vanilla's KeyMapping from ever seeing the press, so
    // keyAttack.isDown() would never go true - track the raw button state ourselves instead.
    private static boolean leftMouseHeld = false;

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

    /**
     * Image-intensifier look (phosphor green, gain, grain, tube vignette) built purely from HUD-layer
     * blends over the already-rendered scene, so it keeps working where PostChain shaders get
     * disabled (Rubidium/Oculus). GuiGraphics#fill always resets to the default alpha blend func
     * internally, so the blend passes draw their quads directly.
     */
    private static void drawNightVisionOverlay(GuiGraphics graphics, int width, int height) {
        Matrix4f matrix = graphics.pose().last().pose();
        RenderSystem.enableBlend();

        // Multiply by a phosphor green: keeps each pixel's brightness (mostly via its green channel)
        // while nearly zeroing red/blue, instead of a flat tinted haze.
        RenderSystem.blendFunc(GlStateManager.SourceFactor.DST_COLOR, GlStateManager.DestFactor.ZERO);
        drawColorQuad(matrix, width, height, NVG_TINT[0], NVG_TINT[1], NVG_TINT[2]);
        // Gain: dst * src + dst, i.e. doubles the brightness like the intensifier tube does.
        RenderSystem.blendFunc(GlStateManager.SourceFactor.DST_COLOR, GlStateManager.DestFactor.ONE);
        drawColorQuad(matrix, width, height, NVG_GAIN, NVG_GAIN, NVG_GAIN);
        // Lift pure black to a very dark green, as a real tube never shows true black.
        RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
        drawColorQuad(matrix, width, height, NVG_LIFT[0], NVG_LIFT[1], NVG_LIFT[2]);

        // Grain: modulate-2x (dst * src * 2) with a mid-gray noise texture, so texels above 128
        // brighten and below darken. Tiled at one texel per screen pixel and re-offset every frame.
        RenderSystem.blendFunc(GlStateManager.SourceFactor.DST_COLOR, GlStateManager.DestFactor.SRC_COLOR);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, NVG_NOISE);
        float tile = (float) (NVG_NOISE_SIZE / Minecraft.getInstance().getWindow().getGuiScale());
        float offsetX = NOISE_RANDOM.nextFloat() * tile;
        float offsetY = NOISE_RANDOM.nextFloat() * tile;
        boolean flipU = NOISE_RANDOM.nextBoolean();
        float u0 = flipU ? 1 : 0;
        float u1 = flipU ? 0 : 1;
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        for (float x = -offsetX; x < width; x += tile) {
            for (float y = -offsetY; y < height; y += tile) {
                buffer.vertex(matrix, x, y + tile, 0).uv(u0, 1).endVertex();
                buffer.vertex(matrix, x + tile, y + tile, 0).uv(u1, 1).endVertex();
                buffer.vertex(matrix, x + tile, y, 0).uv(u1, 0).endVertex();
                buffer.vertex(matrix, x, y, 0).uv(u0, 0).endVertex();
            }
        }
        BufferUploader.drawWithShader(buffer.end());

        // Tube vignette: wide oval, gradually darker toward the rim, black outside. Linear filtering
        // so the stretched 512x256 mask stays smooth.
        RenderSystem.defaultBlendFunc();
        Minecraft.getInstance().getTextureManager().getTexture(NVG_VIGNETTE).setFilter(true, false);
        graphics.blit(NVG_VIGNETTE, 0, 0, 0, 0, width, height, width, height);
    }

    private static void drawColorQuad(Matrix4f matrix, int width, int height, float r, float g, float b) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buffer.vertex(matrix, 0, height, 0).color(r, g, b, 1F).endVertex();
        buffer.vertex(matrix, width, height, 0).color(r, g, b, 1F).endVertex();
        buffer.vertex(matrix, width, 0, 0).color(r, g, b, 1F).endVertex();
        buffer.vertex(matrix, 0, 0, 0).color(r, g, b, 1F).endVertex();
        BufferUploader.drawWithShader(buffer.end());
    }

    @SubscribeEvent
    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        if (aimingStack(Minecraft.getInstance().player) == null) {
            return;
        }
        event.setFOV(event.getFOV() / zoomLevel);
    }

    /** LOWEST so the white-out also covers the scope/goggle overlays drawn in {@link #onRenderGui}. */
    @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOWEST)
    public static void onRenderFlashbang(RenderGuiEvent.Post event) {
        Player player = Minecraft.getInstance().player;
        var flash = player == null ? null : player.getEffect(uk.iwaservice.sbwarmory.ModRegistry.FLASHBANG.get());
        if (flash == null) return;
        float alpha = Math.min(1f, flash.getDuration() / (float) uk.iwaservice.sbwarmory.effect.FlashbangMobEffect.FADE_TICKS);
        GuiGraphics graphics = event.getGuiGraphics();
        graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), (Math.round(alpha * 255) << 24) | 0xFFFFFF);
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        GuiGraphics graphics = event.getGuiGraphics();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        if (wearingActiveGoggles(player) && mc.options.getCameraType().isFirstPerson()) {
            drawNightVisionOverlay(graphics, screenWidth, screenHeight);
        }

        if (aimingStack(player) == null) {
            return;
        }

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

        // The flashbang white-out is a HUD layer, and vanilla skips the whole HUD while it's hidden (F1) -
        // un-hide it while flashed so F1 can't be used to dodge the effect.
        if (mc.player != null && mc.options.hideGui && mc.player.hasEffect(ModRegistry.FLASHBANG.get())) {
            mc.options.hideGui = false;
        }

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
