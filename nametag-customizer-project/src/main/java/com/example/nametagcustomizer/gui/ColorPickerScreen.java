package com.example.nametagcustomizer.gui;

import com.example.nametagcustomizer.config.NametagConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.Locale;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/** Small screen with R / G / B sliders, presets and a live preview. */
public class ColorPickerScreen extends Screen {
    private static final int PW = 250;
    private static final int PH = 172;
    private static final int[] PRESETS = {
            0xFFFFFF, 0xFF5555, 0xFF7518, 0xFFFF55, 0x55FF55, 0x55FFFF, 0x5555FF, 0xB44DFF, 0xFF55FF, 0x000000
    };
    private static final String[] CHANNEL = {"R", "G", "B"};

    private final Screen parent;
    private final Text subject;
    private final IntConsumer setter;
    private int rgb;
    private int px;
    private int py;

    public ColorPickerScreen(Screen parent, Text subject, IntSupplier getter, IntConsumer setter) {
        super(Text.translatable("nametagcustomizer.picker.title"));
        this.parent = parent;
        this.subject = subject;
        this.setter = setter;
        this.rgb = getter.getAsInt() & 0xFFFFFF;
    }

    private void apply(int newRgb) {
        this.rgb = newRgb & 0xFFFFFF;
        setter.accept(this.rgb);
    }

    @Override
    protected void init() {
        px = (this.width - PW) / 2;
        py = (this.height - PH) / 2;

        for (int ch = 0; ch < 3; ch++) {
            addDrawableChild(new ChannelSlider(px + 14, py + 34 + ch * 22, 170, 18, ch));
        }

        int pw = 18;
        int gap = (PW - 28 - PRESETS.length * pw) / (PRESETS.length - 1);
        for (int i = 0; i < PRESETS.length; i++) {
            final int color = PRESETS[i];
            addDrawableChild(new Preset(px + 14 + i * (pw + gap), py + 112, pw, 14, color));
        }

        addDrawableChild(new ThemedButton(px + PW / 2 - 55, py + PH - 30, 110, 18,
                Text.translatable("nametagcustomizer.gui.done"), this::close));
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        // drawn manually in render()
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        context.fill(0, 0, this.width, this.height, 0xA0000000);
        Theme.panel(context, px, py, PW, PH);
        if (Theme.halloween()) {
            Theme.web(context, px + 2, py + 2, 1, 1, 30);
            Theme.web(context, px + PW - 3, py + 2, -1, 1, 30);
        }

        TextRenderer font = this.textRenderer;
        Text head = Text.empty().append(this.title).append(Text.literal(" - ")).append(subject);
        context.drawText(font, head, px + (PW - font.getWidth(head)) / 2, py + 12, Theme.titleColor(), true);

        // preview
        int bx = px + PW - 14 - 44;
        int by = py + 34;
        context.fill(bx - 1, by - 1, bx + 45, by + 63, Theme.accent());
        context.fill(bx, by, bx + 44, by + 62, 0xFF000000 | rgb);
        String hex = String.format(Locale.ROOT, "#%06X", rgb);
        context.drawText(font, hex, bx + (44 - font.getWidth(hex)) / 2, by + 68, 0xFFFFFFFF, true);

        super.render(context, mouseX, mouseY, deltaTicks);
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }

    @Override
    public void removed() {
        NametagConfig.get().save();
    }

    // ------------------------------------------------------------------

    private class ChannelSlider extends ClickableWidget {
        private final int channel;

        ChannelSlider(int x, int y, int w, int h, int channel) {
            super(x, y, w, h, Text.literal(CHANNEL[channel]));
            this.channel = channel;
        }

        private int shift() {
            return 16 - 8 * channel;
        }

        private int value() {
            return (rgb >> shift()) & 0xFF;
        }

        private int withValue(int v) {
            return (rgb & ~(0xFF << shift())) | (v << shift());
        }

        private void setFromMouse(double mouseX) {
            double t = (mouseX - (getX() + 2)) / (double) (getWidth() - 4);
            t = Math.max(0.0, Math.min(1.0, t));
            apply(withValue((int) Math.round(t * 255.0)));
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            setFromMouse(mouseX);
        }

        @Override
        public void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
            setFromMouse(mouseX);
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
            Theme.box(context, getX(), getY(), getWidth(), getHeight(), isHovered(), active, false);
            int inner = getWidth() - 4;
            for (int i = 0; i < inner; i += 2) {
                int v = Math.round(i * 255f / (inner - 1));
                int col = 0xFF000000 | withValue(v);
                context.fill(getX() + 2 + i, getY() + 2, Math.min(getX() + 2 + i + 2, getX() + 2 + inner), getY() + getHeight() - 2, col);
            }
            int kx = getX() + 2 + Math.round((inner - 1) * value() / 255f);
            context.fill(kx - 2, getY(), kx + 3, getY() + getHeight(), 0xFF000000);
            context.fill(kx - 1, getY() + 1, kx + 2, getY() + getHeight() - 1, 0xFFFFFFFF);

            TextRenderer font = MinecraftClient.getInstance().textRenderer;
            Text shown = Text.literal(CHANNEL[channel] + ": " + value());
            int tw = font.getWidth(shown);
            context.drawText(font, shown, getX() + (getWidth() - tw) / 2, getY() + (getHeight() - 8) / 2, 0xFFFFFFFF, true);
        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        }
    }

    private class Preset extends ClickableWidget {
        private final int color;

        Preset(int x, int y, int w, int h, int color) {
            super(x, y, w, h, Text.empty());
            this.color = color;
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            apply(color);
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
            context.fill(getX() - 1, getY() - 1, getX() + getWidth() + 1, getY() + getHeight() + 1,
                    isHovered() ? 0xFFFFFFFF : 0xFF000000);
            context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0xFF000000 | color);
        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        }
    }
}
