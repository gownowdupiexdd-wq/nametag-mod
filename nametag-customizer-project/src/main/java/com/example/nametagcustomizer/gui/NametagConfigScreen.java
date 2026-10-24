package com.example.nametagcustomizer.gui;

import com.example.nametagcustomizer.config.NametagConfig;
import com.example.nametagcustomizer.render.NametagStyler;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class NametagConfigScreen extends Screen {
    private static final int PW = 340;
    private static final int PH = 236;
    private static final String[] TABS = {"text", "background", "position", "effects", "glow"};
    private static final int GLOW_PER_PAGE = 4;

    private static final Function<Float, String> F2 = v -> String.format(Locale.ROOT, "%.2f", v);
    private static final Function<Float, String> F1 = v -> String.format(Locale.ROOT, "%.1f", v);

    private final Screen parent;
    private int page = 0;
    private int glowPage = 0;
    private String glowInput = "";
    private int px;
    private int py;

    public NametagConfigScreen(Screen parent) {
        super(Text.translatable("nametagcustomizer.screen.title"));
        this.parent = parent;
    }

    private static MutableText tr(String key) {
        return Text.translatable("nametagcustomizer." + key);
    }

    private int colX(int col) {
        return px + 14 + col * 162;
    }

    private int rowY(int row) {
        return py + 100 + row * 22;
    }

    private void rebuild() {
        this.clearChildren();
        this.init();
    }

    // ------------------------------------------------------------------ widgets helpers

    private ToggleButton toggle(int col, int row, String key, BooleanSupplier g, Consumer<Boolean> s) {
        return addDrawableChild(new ToggleButton(colX(col), rowY(row), 150, 18, tr("option." + key), g, s));
    }

    private ValueSlider slider(int col, int row, String key, float min, float max, float step,
                               Supplier<Float> g, Consumer<Float> s, Function<Float, String> fmt) {
        return addDrawableChild(new ValueSlider(colX(col), rowY(row), 150, 18, tr("option." + key),
                min, max, step, g, s, fmt));
    }

    private ColorButton color(int col, int row, String key, IntSupplier g, IntConsumer s) {
        return addDrawableChild(new ColorButton(colX(col), rowY(row), 150, 18, tr("option." + key), g,
                () -> this.client.setScreen(new ColorPickerScreen(this, tr("option." + key), g, s))));
    }

    // ------------------------------------------------------------------ init

    @Override
    protected void init() {
        px = (this.width - PW) / 2;
        py = (this.height - PH) / 2;

        // header: Halloween GUI switch
        addDrawableChild(new ToggleButton(px + PW - 14 - 140, py + 6, 140, 16, tr("gui.halloween"),
                () -> NametagConfig.get().halloweenGui, v -> NametagConfig.get().halloweenGui = v));

        // tabs
        for (int i = 0; i < TABS.length; i++) {
            final int idx = i;
            addDrawableChild(new ThemedButton(px + 14 + i * 63, py + 76, 60, 18, tr("tab." + TABS[i]),
                    () -> {
                        page = idx;
                        rebuild();
                    },
                    () -> page == idx));
        }

        switch (page) {
            case 0 -> textPage();
            case 1 -> backgroundPage();
            case 2 -> positionPage();
            case 3 -> effectsPage();
            default -> glowPage();
        }

        // footer
        addDrawableChild(new ThemedButton(px + 14, py + PH - 28, 150, 18, tr("gui.reset"), () -> {
            NametagConfig.reset();
            rebuild();
        }));
        addDrawableChild(new ThemedButton(px + PW - 14 - 150, py + PH - 28, 150, 18, tr("gui.done"), this::close));
    }

    private void textPage() {
        toggle(0, 0, "text_visible", () -> NametagConfig.get().textVisible, v -> NametagConfig.get().textVisible = v);
        color(1, 0, "text_color", () -> NametagConfig.get().textColor, v -> NametagConfig.get().textColor = v);

        slider(0, 1, "text_scale", NametagConfig.SCALE_MIN, NametagConfig.SCALE_MAX, 0.05f,
                () -> NametagConfig.get().textScale, v -> NametagConfig.get().textScale = v, F2);
        toggle(1, 1, "force_color", () -> NametagConfig.get().forceTextColor, v -> NametagConfig.get().forceTextColor = v);

        toggle(0, 2, "shadow", () -> NametagConfig.get().shadowEnabled, v -> NametagConfig.get().shadowEnabled = v);
        color(1, 2, "shadow_color", () -> NametagConfig.get().shadowColor, v -> NametagConfig.get().shadowColor = v);

        toggle(0, 3, "outline", () -> NametagConfig.get().outlineEnabled, v -> NametagConfig.get().outlineEnabled = v);
        color(1, 3, "outline_color", () -> NametagConfig.get().outlineColor, v -> NametagConfig.get().outlineColor = v);

        slider(0, 4, "outline_thickness", NametagConfig.THICKNESS_MIN, NametagConfig.THICKNESS_MAX, 0.1f,
                () -> NametagConfig.get().outlineThickness, v -> NametagConfig.get().outlineThickness = v, F1);
        toggle(1, 4, "enabled", () -> NametagConfig.get().enabled, v -> NametagConfig.get().enabled = v);
    }

    private void backgroundPage() {
        toggle(0, 0, "bg_visible", () -> NametagConfig.get().backgroundVisible, v -> NametagConfig.get().backgroundVisible = v);
        color(1, 0, "bg_color", () -> NametagConfig.get().backgroundColor, v -> NametagConfig.get().backgroundColor = v);

        slider(0, 1, "bg_opacity", 0f, 1f, 0.05f,
                () -> NametagConfig.get().backgroundOpacity, v -> NametagConfig.get().backgroundOpacity = v, F2);

        slider(0, 2, "pad_x", 0f, NametagConfig.PADDING_MAX, 0.5f,
                () -> NametagConfig.get().paddingX, v -> NametagConfig.get().paddingX = v, F1);
        slider(1, 2, "pad_y", 0f, NametagConfig.PADDING_MAX, 0.5f,
                () -> NametagConfig.get().paddingY, v -> NametagConfig.get().paddingY = v, F1);

        toggle(0, 3, "border", () -> NametagConfig.get().borderEnabled, v -> NametagConfig.get().borderEnabled = v);
        color(1, 3, "border_color", () -> NametagConfig.get().borderColor, v -> NametagConfig.get().borderColor = v);

        slider(0, 4, "border_thickness", NametagConfig.THICKNESS_MIN, NametagConfig.THICKNESS_MAX, 0.1f,
                () -> NametagConfig.get().borderThickness, v -> NametagConfig.get().borderThickness = v, F1);
    }

    private void positionPage() {
        slider(0, 0, "offset_x", NametagConfig.OFFSET_MIN, NametagConfig.OFFSET_MAX, 1f,
                () -> NametagConfig.get().offsetX, v -> NametagConfig.get().offsetX = v, F1);
        slider(1, 0, "offset_y", NametagConfig.OFFSET_MIN, NametagConfig.OFFSET_MAX, 1f,
                () -> NametagConfig.get().offsetY, v -> NametagConfig.get().offsetY = v, F1);

        slider(0, 1, "stretch_x", NametagConfig.STRETCH_MIN, NametagConfig.STRETCH_MAX, 0.05f,
                () -> NametagConfig.get().stretchX, v -> NametagConfig.get().stretchX = v, F2);
        slider(1, 1, "stretch_y", NametagConfig.STRETCH_MIN, NametagConfig.STRETCH_MAX, 0.05f,
                () -> NametagConfig.get().stretchY, v -> NametagConfig.get().stretchY = v, F2);

        toggle(0, 2, "players_only", () -> NametagConfig.get().playersOnly, v -> NametagConfig.get().playersOnly = v);
        toggle(1, 2, "through_walls", () -> NametagConfig.get().throughWalls, v -> NametagConfig.get().throughWalls = v);

        slider(0, 3, "max_distance", 0f, NametagConfig.DISTANCE_MAX, 1f,
                () -> NametagConfig.get().maxDistance, v -> NametagConfig.get().maxDistance = v,
                v -> v <= 0f ? Text.translatable("nametagcustomizer.gui.off").getString() : String.format(Locale.ROOT, "%.0f", v));
    }

    private void effectsPage() {
        toggle(0, 0, "rainbow", () -> NametagConfig.get().rainbow, v -> NametagConfig.get().rainbow = v);
        slider(1, 0, "rainbow_speed", 0.1f, 5f, 0.1f,
                () -> NametagConfig.get().rainbowSpeed, v -> NametagConfig.get().rainbowSpeed = v, F1);

        toggle(0, 1, "pulse", () -> NametagConfig.get().pulse, v -> NametagConfig.get().pulse = v);
        toggle(1, 1, "halloween_text", () -> NametagConfig.get().halloweenText, v -> NametagConfig.get().halloweenText = v);
    }

    private void glowPage() {
        toggle(0, 0, "glow", () -> NametagConfig.get().glowEnabled, v -> NametagConfig.get().glowEnabled = v);
        color(1, 0, "glow_color", () -> NametagConfig.get().glowColor, v -> NametagConfig.get().glowColor = v);

        slider(0, 1, "glow_strength", 0.5f, 3f, 0.1f,
                () -> NametagConfig.get().glowStrength, v -> NametagConfig.get().glowStrength = v, F1);

        List<String> list = NametagConfig.get().glowPlayers;
        final int pages = Math.max(1, (list.size() + GLOW_PER_PAGE - 1) / GLOW_PER_PAGE);
        glowPage = Math.max(0, Math.min(glowPage, pages - 1));

        addDrawableChild(new ThemedButton(colX(1), rowY(1), 36, 18, Text.literal("<"), () -> {
            if (glowPage > 0) {
                glowPage--;
                rebuild();
            }
        }));
        ThemedButton pageLabel = addDrawableChild(new ThemedButton(colX(1) + 38, rowY(1), 74, 18,
                Text.literal((glowPage + 1) + " / " + pages), null));
        pageLabel.active = false;
        addDrawableChild(new ThemedButton(colX(1) + 114, rowY(1), 36, 18, Text.literal(">"), () -> {
            if (glowPage < pages - 1) {
                glowPage++;
                rebuild();
            }
        }));

        TextFieldWidget field = new TextFieldWidget(this.textRenderer, colX(0), rowY(2), 150, 18, Text.empty());
        field.setMaxLength(16);
        field.setPlaceholder(tr("gui.glow_hint"));
        field.setText(glowInput);
        field.setChangedListener(s -> glowInput = s);
        addDrawableChild(field);

        addDrawableChild(new ThemedButton(colX(1), rowY(2), 150, 18, tr("gui.glow_add"), this::addGlow));

        for (int i = 0; i < GLOW_PER_PAGE; i++) {
            int idx = glowPage * GLOW_PER_PAGE + i;
            if (idx >= list.size()) {
                break;
            }
            final String nick = list.get(idx);
            int col = i % 2;
            int row = 3 + i / 2;
            addDrawableChild(new ThemedButton(colX(col), rowY(row), 150, 18, Text.literal("[x] " + nick), () -> {
                NametagConfig.get().glowPlayers.remove(nick);
                rebuild();
            }));
        }
    }

    private void addGlow() {
        String nick = glowInput.trim().toLowerCase(Locale.ROOT);
        if (nick.isEmpty()) {
            return;
        }
        List<String> list = NametagConfig.get().glowPlayers;
        if (!list.contains(nick)) {
            list.add(nick);
        }
        glowInput = "";
        rebuild();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (page == 4 && (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) && !glowInput.isEmpty()) {
            addGlow();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    // ------------------------------------------------------------------ rendering

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        // drawn manually in render()
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        context.fill(0, 0, this.width, this.height, 0x90000000);
        Theme.panel(context, px, py, PW, PH);

        if (Theme.halloween()) {
            Theme.web(context, px + 2, py + 2, 1, 1, 40);
            Theme.web(context, px + PW - 3, py + 2, -1, 1, 40);
            Theme.pumpkin(context, px + 150, py + 6);
        }

        context.drawText(this.textRenderer, this.title, px + 14, py + 11, Theme.titleColor(), true);

        renderPreview(context);
        super.render(context, mouseX, mouseY, deltaTicks);
    }

    private static int r(float v) {
        return Math.round(v);
    }

    private void renderPreview(DrawContext ctx) {
        NametagConfig c = NametagConfig.get();
        boolean halloween = Theme.halloween();

        int bx = px + 14;
        int by = py + 28;
        int bw = PW - 28;
        int bh = 44;
        ctx.fillGradient(bx, by, bx + bw, by + bh, halloween ? 0xFF1B0B3A : 0xFF6FA3D8, halloween ? 0xFF45175E : 0xFF3F6D99);
        ctx.drawBorder(bx - 1, by - 1, bw + 2, bh + 2, Theme.accent());

        TextRenderer font = this.textRenderer;
        String raw = "Steve";
        Text text = NametagStyler.style(Text.literal(raw), raw, c);
        Text plain = NametagStyler.plain(text);
        int w = font.getWidth(text);

        float x = -w / 2f;
        float y = 0f;
        float bx1 = x - c.paddingX;
        float bx2 = x + w + c.paddingX;
        float by1 = y - c.paddingY;
        float by2 = y + 8f + c.paddingY;

        float scale = c.textScale;
        if (c.pulse) {
            scale *= 1f + 0.07f * (float) Math.sin(NametagStyler.time() * 4.0);
        }
        float sx = scale * c.stretchX;
        float sy = scale * c.stretchY;
        float zoom = 1.6f;

        ctx.enableScissor(bx, by, bx + bw, by + bh);
        var ms = ctx.getMatrices();
        ms.pushMatrix();
        // the bottom edge of the label stays at the same screen position (like in the world)
        ms.translate(bx + bw / 2f, by + bh - 6f - by2 * zoom);
        ms.scale(zoom, zoom);
        ms.translate(0f, by2);
        ms.scale(sx, sy);
        ms.translate(0f, -by2);

        if (c.backgroundVisible) {
            ctx.fill(r(bx1), r(by1), r(bx2), r(by2), c.backgroundArgb());
        }
        if (c.borderEnabled) {
            int b = Math.max(1, r(c.borderThickness));
            int col = 0xFF000000 | (c.borderColor & 0xFFFFFF);
            ctx.fill(r(bx1) - b, r(by1) - b, r(bx2) + b, r(by1), col);
            ctx.fill(r(bx1) - b, r(by2), r(bx2) + b, r(by2) + b, col);
            ctx.fill(r(bx1) - b, r(by1), r(bx1), r(by2), col);
            ctx.fill(r(bx2), r(by1), r(bx2) + b, r(by2), col);
        }

        if (c.textVisible) {
            int glowRgb = -1;
            float glowStrength = 1f;
            float glowAlpha = 1f;
            if (c.glowEnabled && !c.glowPlayers.isEmpty()) {
                glowRgb = c.glowColor & 0xFFFFFF;
                glowStrength = c.glowStrength;
            } else if (c.halloweenText) {
                glowRgb = NametagStyler.HALLOWEEN_ORANGE;
                glowStrength = 0.8f;
                glowAlpha = 0.6f;
            }
            if (glowRgb >= 0) {
                float pulse = 0.8f + 0.2f * (float) Math.sin(NametagStyler.time() * 3.0);
                for (int ring = 0; ring < NametagStyler.RING_RADIUS.length; ring++) {
                    float rad = NametagStyler.RING_RADIUS[ring] * glowStrength;
                    int a = Math.round(255f * NametagStyler.RING_ALPHA[ring] * pulse * glowAlpha);
                    for (float[] d : NametagStyler.DIRS8) {
                        ctx.drawText(font, plain, r(x + d[0] * rad), r(y + d[1] * rad), (a << 24) | glowRgb, false);
                    }
                }
            }
            if (c.outlineEnabled) {
                float t = c.outlineThickness;
                for (float[] d : NametagStyler.DIRS8) {
                    ctx.drawText(font, plain, r(x + d[0] * t), r(y + d[1] * t), 0xFF000000 | (c.outlineColor & 0xFFFFFF), false);
                }
            }
            if (c.shadowEnabled) {
                ctx.drawText(font, plain, r(x) + 1, r(y) + 1, 0xFF000000 | (c.shadowColor & 0xFFFFFF), false);
            }
            ctx.drawText(font, text, r(x), r(y), 0xFF000000 | (c.textColor & 0xFFFFFF), false);
        }

        ms.popMatrix();
        ctx.disableScissor();

        ctx.drawText(font, tr("gui.preview"), bx + 4, by + 4, 0xFFFFFFFF, true);
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }

    @Override
    public void removed() {
        NametagConfig.get().save();
    }
}
