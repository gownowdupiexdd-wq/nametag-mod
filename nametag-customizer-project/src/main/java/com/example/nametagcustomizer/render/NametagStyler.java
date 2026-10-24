package com.example.nametagcustomizer.render;

import com.example.nametagcustomizer.config.NametagConfig;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.math.MathHelper;

/** Builds the (possibly animated) text shown on a nametag. Shared by the world renderer and the GUI preview. */
public final class NametagStyler {
    private NametagStyler() {}

    /** Glow = a few rings of translucent copies of the text. */
    public static final float[] RING_RADIUS = {0.7f, 1.4f, 2.1f};
    public static final float[] RING_ALPHA = {0.50f, 0.28f, 0.14f};
    public static final float[][] DIRS8 = {
            {1f, 0f}, {0.7071f, 0.7071f}, {0f, 1f}, {-0.7071f, 0.7071f},
            {-1f, 0f}, {-0.7071f, -0.7071f}, {0f, -1f}, {0.7071f, -0.7071f}
    };

    public static final int HALLOWEEN_ORANGE = 0xFF7518;
    public static final int HALLOWEEN_PURPLE = 0xB44DFF;

    public static double time() {
        return System.currentTimeMillis() / 1000.0;
    }

    public static Text style(Text original, String raw, NametagConfig cfg) {
        if (cfg.halloweenText) {
            return halloween(raw);
        }
        if (cfg.rainbow) {
            return rainbow(raw, cfg.rainbowSpeed);
        }
        if (cfg.forceTextColor) {
            return Text.literal(raw);
        }
        return original;
    }

    /** Same characters, no styles - used for outline / glow / shadow copies. */
    public static Text plain(Text styled) {
        return Text.literal(styled.getString());
    }

    private static Style colored(int rgb) {
        return Style.EMPTY.withColor(TextColor.fromRgb(rgb & 0xFFFFFF));
    }

    private static Text rainbow(String s, float speed) {
        MutableText out = Text.empty();
        double t = time() * speed * 0.5;
        int i = 0;
        for (int off = 0; off < s.length(); ) {
            int cp = s.codePointAt(off);
            off += Character.charCount(cp);
            float hue = (float) ((t + i * 0.06) % 1.0);
            int rgb = MathHelper.hsvToRgb(hue, 0.75f, 1.0f) & 0xFFFFFF;
            out.append(Text.literal(new String(Character.toChars(cp))).setStyle(colored(rgb)));
            i++;
        }
        return out;
    }

    private static Text halloween(String s) {
        MutableText out = Text.empty();
        double t = time();
        out.append(Text.literal("\u2620 ").setStyle(colored(0xFFB347)));
        int i = 0;
        for (int off = 0; off < s.length(); ) {
            int cp = s.codePointAt(off);
            off += Character.charCount(cp);
            float mix = 0.5f + 0.5f * (float) Math.sin(t * 2.2 + i * 0.9);
            int rgb = lerpRgb(HALLOWEEN_ORANGE, HALLOWEEN_PURPLE, mix);
            // candle-like flicker: now and then a letter flashes pale
            if (Math.sin(t * 9.0 + i * 3.1) > 0.94) {
                rgb = 0xFFF2CC;
            }
            out.append(Text.literal(new String(Character.toChars(cp))).setStyle(colored(rgb)));
            i++;
        }
        out.append(Text.literal(" \u2620").setStyle(colored(0xFFB347)));
        return out;
    }

    public static int lerpRgb(int a, int b, float t) {
        int r = (int) (((a >> 16) & 255) * (1f - t) + ((b >> 16) & 255) * t);
        int g = (int) (((a >> 8) & 255) * (1f - t) + ((b >> 8) & 255) * t);
        int bl = (int) ((a & 255) * (1f - t) + (b & 255) * t);
        return (r << 16) | (g << 8) | bl;
    }
}
