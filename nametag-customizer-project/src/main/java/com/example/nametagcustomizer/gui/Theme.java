package com.example.nametagcustomizer.gui;

import com.example.nametagcustomizer.config.NametagConfig;
import net.minecraft.client.gui.DrawContext;

/** Colors and drawing helpers for the normal and the Halloween GUI look. */
public final class Theme {
    private Theme() {}

    public static boolean halloween() {
        return NametagConfig.get().halloweenGui;
    }

    public static int titleColor() {
        return halloween() ? 0xFFFF9A3C : 0xFFFFFFFF;
    }

    public static int accent() {
        return halloween() ? 0xFFFF7518 : 0xFF8BA3FF;
    }

    public static int sliderFill() {
        return halloween() ? 0xA0FF7518 : 0xA05A7AFF;
    }

    public static int onColor() {
        return halloween() ? 0xFF8CFF4D : 0xFF55FF55;
    }

    public static int offColor() {
        return halloween() ? 0xFFFF6A3D : 0xFFFF5555;
    }

    public static int label(boolean hovered, boolean active) {
        if (!active) {
            return 0xFF808080;
        }
        if (halloween()) {
            return hovered ? 0xFFFFFFFF : 0xFFFFA64D;
        }
        return hovered ? 0xFFFFFFFF : 0xFFE0E0E0;
    }

    public static void panel(DrawContext c, int x, int y, int w, int h) {
        if (halloween()) {
            c.fill(x - 2, y - 2, x + w + 2, y + h + 2, 0xFFFF7518);
            c.fill(x, y, x + w, y + h, 0xFF4B1A6E);
            c.fillGradient(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF230B36, 0xFF08010F);
        } else {
            c.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xFF000000);
            c.fillGradient(x, y, x + w, y + h, 0xF0262626, 0xF0161616);
        }
    }

    public static void box(DrawContext c, int x, int y, int w, int h, boolean hovered, boolean active, boolean selected) {
        int border;
        int fill;
        if (halloween()) {
            border = selected ? 0xFFFFC857 : hovered && active ? 0xFFFFB347 : 0xFFFF7518;
            fill = !active ? 0xFF1A0A26 : selected ? 0xFF6B2A00 : hovered ? 0xFF5A1F85 : 0xFF2B0F42;
            if (!active) {
                border = 0xFF6B3A12;
            }
        } else {
            border = selected ? 0xFFB8C4FF : 0xFF000000;
            fill = !active ? 0xFF2A2A2A : selected ? 0xFF4A5A8A : hovered ? 0xFF5A5A5A : 0xFF3C3C3C;
        }
        c.fill(x, y, x + w, y + h, border);
        c.fill(x + 1, y + 1, x + w - 1, y + h - 1, fill);
        c.fill(x + 1, y + 1, x + w - 1, y + 2, 0x30FFFFFF);
    }

    /** Little pixel-art jack-o'-lantern, 16x13 px. */
    public static void pumpkin(DrawContext c, int x, int y) {
        int orange = 0xFFFF7518, dark = 0xFFC4500F, green = 0xFF3DA33D, black = 0xFF000000, glow = 0xFFFFD34D;
        c.fill(x + 7, y, x + 9, y + 3, green);
        c.fill(x + 3, y + 3, x + 13, y + 13, orange);
        c.fill(x + 1, y + 5, x + 15, y + 11, orange);
        c.fill(x, y + 6, x + 16, y + 10, orange);
        c.fill(x + 5, y + 3, x + 6, y + 5, dark);
        c.fill(x + 10, y + 3, x + 11, y + 5, dark);
        // eyes
        c.fill(x + 3, y + 6, x + 6, y + 8, black);
        c.fill(x + 4, y + 5, x + 5, y + 6, black);
        c.fill(x + 10, y + 6, x + 13, y + 8, black);
        c.fill(x + 11, y + 5, x + 12, y + 6, black);
        c.fill(x + 4, y + 6, x + 5, y + 7, glow);
        c.fill(x + 11, y + 6, x + 12, y + 7, glow);
        // mouth
        c.fill(x + 4, y + 10, x + 12, y + 11, black);
        c.fill(x + 5, y + 9, x + 6, y + 10, black);
        c.fill(x + 10, y + 9, x + 11, y + 10, black);
        c.fill(x + 7, y + 9, x + 9, y + 10, glow);
    }

    /** Cobweb in a corner. (sx, sy) = direction the web spreads (+1 / -1). */
    public static void web(DrawContext c, int cx, int cy, int sx, int sy, int len) {
        int col = 0x70FFFFFF;
        double[] angles = {0, 22.5, 45, 67.5, 90};
        for (double a : angles) {
            double rad = Math.toRadians(a);
            for (int r = 0; r <= len; r++) {
                int px = cx + sx * (int) Math.round(Math.cos(rad) * r);
                int py = cy + sy * (int) Math.round(Math.sin(rad) * r);
                c.fill(px, py, px + 1, py + 1, col);
            }
        }
        for (int ring = 1; ring <= 3; ring++) {
            int r = ring * len / 3;
            for (double a = 0; a <= 90; a += 2) {
                double rad = Math.toRadians(a);
                double sag = 1.0 - 0.10 * Math.sin(Math.toRadians((a % 22.5) * 8.0));
                int px = cx + sx * (int) Math.round(Math.cos(rad) * r * sag);
                int py = cy + sy * (int) Math.round(Math.sin(rad) * r * sag);
                c.fill(px, py, px + 1, py + 1, col);
            }
        }
    }
}
