package com.example.nametagcustomizer.render;

import com.example.nametagcustomizer.config.NametagConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

/**
 * Replacement for vanilla's nametag drawing.
 * Layers (back to front): background / border, glow, outline, shadow, text.
 */
public final class NametagRenderer {
    private NametagRenderer() {}

    private static final float Z_BG = -0.30f;
    private static final float Z_GLOW = -0.20f;
    private static final float Z_OUTLINE = -0.10f;
    private static final float Z_SHADOW = -0.05f;

    /** @return true when the vanilla label should be cancelled (we drew it ourselves). */
    public static boolean render(EntityRenderState state, Text original, MatrixStack matrices,
                                 VertexConsumerProvider consumers, int light) {
        NametagConfig cfg = NametagConfig.get();
        if (!cfg.enabled) {
            return false;
        }
        boolean isPlayer = state instanceof PlayerEntityRenderState;
        if (cfg.playersOnly && !isPlayer) {
            return false;
        }
        Vec3d labelPos = state.nameLabelPos;
        if (labelPos == null) {
            return true;
        }

        MinecraftClient client = MinecraftClient.getInstance();

        if (cfg.maxDistance > 0f) {
            Vec3d cam = client.gameRenderer.getCamera().getPos();
            double dx = state.x - cam.x;
            double dy = state.y - cam.y;
            double dz = state.z - cam.z;
            double max = cfg.maxDistance;
            if (dx * dx + dy * dy + dz * dz > max * max) {
                return true;
            }
        }

        TextRenderer font = client.textRenderer;
        boolean seeThrough = cfg.throughWalls && !state.sneaking;

        String raw = original.getString();
        Text text = NametagStyler.style(original, raw, cfg);
        Text plain = NametagStyler.plain(text);

        int width = font.getWidth(text);
        float x = -width / 2f;
        float y = "deadmau5".equals(raw) ? -10f : 0f;

        float padX = cfg.paddingX;
        float padY = cfg.paddingY;
        float bx1 = x - padX;
        float bx2 = x + width + padX;
        float by1 = y - padY;
        float by2 = y + 8f + padY;

        float scale = cfg.textScale;
        if (cfg.pulse) {
            scale *= 1f + 0.07f * (float) Math.sin(NametagStyler.time() * 4.0);
        }
        float sx = scale * cfg.stretchX;
        float sy = scale * cfg.stretchY;

        matrices.push();
        matrices.translate(labelPos.x, labelPos.y + 0.5, labelPos.z);
        matrices.multiply(client.getEntityRenderDispatcher().getRotation());
        matrices.scale(0.025f, -0.025f, 0.025f);

        // manual offset (label units, +Y = up)
        matrices.translate(cfg.offsetX, -cfg.offsetY, 0f);

        // Scale around the BOTTOM-CENTER of the label, so bigger text grows upwards
        // and never sinks into the player's head.
        matrices.translate(0f, by2, 0f);
        matrices.scale(sx, sy, 1f);
        matrices.translate(0f, -by2, 0f);

        Matrix4f base = matrices.peek().getPositionMatrix();

        // ---------- background + border ----------
        if (cfg.backgroundVisible) {
            quad(consumers, base, bx1, by1, bx2, by2, Z_BG, cfg.backgroundArgb(), light, seeThrough);
        }
        if (cfg.borderEnabled) {
            float b = cfg.borderThickness;
            int col = 0xFF000000 | (cfg.borderColor & 0xFFFFFF);
            quad(consumers, base, bx1 - b, by1 - b, bx2 + b, by1, Z_BG, col, light, seeThrough);
            quad(consumers, base, bx1 - b, by2, bx2 + b, by2 + b, Z_BG, col, light, seeThrough);
            quad(consumers, base, bx1 - b, by1, bx1, by2, Z_BG, col, light, seeThrough);
            quad(consumers, base, bx2, by1, bx2 + b, by2, Z_BG, col, light, seeThrough);
        }

        if (cfg.textVisible) {
            // ---------- glow ----------
            int glowRgb = -1;
            float glowStrength = 1f;
            float glowAlpha = 1f;
            if (isPlayer && cfg.hasGlow(raw)) {
                glowRgb = cfg.glowColor & 0xFFFFFF;
                glowStrength = cfg.glowStrength;
            } else if (cfg.halloweenText) {
                glowRgb = NametagStyler.HALLOWEEN_ORANGE;
                glowStrength = 0.8f;
                glowAlpha = 0.6f;
            }
            if (glowRgb >= 0) {
                float pulse = 0.8f + 0.2f * (float) Math.sin(NametagStyler.time() * 3.0);
                Matrix4f m = new Matrix4f(base).translate(0f, 0f, Z_GLOW);
                for (int ring = 0; ring < NametagStyler.RING_RADIUS.length; ring++) {
                    float r = NametagStyler.RING_RADIUS[ring] * glowStrength;
                    float a = NametagStyler.RING_ALPHA[ring] * pulse * glowAlpha;
                    for (float[] d : NametagStyler.DIRS8) {
                        drawText(font, plain, x + d[0] * r, y + d[1] * r, glowRgb, a, m, consumers, light, seeThrough);
                    }
                }
            }

            // ---------- outline ----------
            if (cfg.outlineEnabled) {
                float t = cfg.outlineThickness;
                Matrix4f m = new Matrix4f(base).translate(0f, 0f, Z_OUTLINE);
                for (float[] d : NametagStyler.DIRS8) {
                    drawText(font, plain, x + d[0] * t, y + d[1] * t, cfg.outlineColor & 0xFFFFFF, 1f, m, consumers, light, seeThrough);
                }
            }

            // ---------- shadow ----------
            if (cfg.shadowEnabled) {
                Matrix4f m = new Matrix4f(base).translate(0f, 0f, Z_SHADOW);
                drawText(font, plain, x + 1f, y + 1f, cfg.shadowColor & 0xFFFFFF, 1f, m, consumers, light, seeThrough);
            }

            // ---------- text ----------
            drawText(font, text, x, y, cfg.textColor & 0xFFFFFF, 1f, base, consumers, light, seeThrough);
        }

        matrices.pop();
        return true;
    }

    private static void quad(VertexConsumerProvider consumers, Matrix4f m, float x1, float y1, float x2, float y2,
                             float z, int argb, int light, boolean seeThrough) {
        VertexConsumer vc = consumers.getBuffer(
                seeThrough ? RenderLayer.getTextBackgroundSeeThrough() : RenderLayer.getTextBackground());
        vc.vertex(m, x1, y1, z).color(argb).light(light);
        vc.vertex(m, x1, y2, z).color(argb).light(light);
        vc.vertex(m, x2, y2, z).color(argb).light(light);
        vc.vertex(m, x2, y1, z).color(argb).light(light);
    }

    /** Draws text the way vanilla does: a faint see-through pass first, then the normal (depth-tested) pass. */
    private static void drawText(TextRenderer font, Text t, float x, float y, int rgb, float alpha,
                                 Matrix4f m, VertexConsumerProvider consumers, int light, boolean seeThrough) {
        int a = Math.max(0, Math.min(255, Math.round(alpha * 255f)));
        if (a == 0) {
            return;
        }
        if (seeThrough) {
            int a2 = Math.max(1, a / 2);
            font.draw(t, x, y, (a2 << 24) | rgb, false, m, consumers,
                    TextRenderer.TextLayerType.SEE_THROUGH, 0, light);
        }
        font.draw(t, x, y, (a << 24) | rgb, false, m, consumers,
                TextRenderer.TextLayerType.NORMAL, 0, LightmapTextureManager.applyEmission(light, 2));
    }
}
