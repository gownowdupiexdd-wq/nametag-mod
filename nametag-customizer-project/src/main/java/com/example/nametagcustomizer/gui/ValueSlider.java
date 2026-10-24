package com.example.nametagcustomizer.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ValueSlider extends ClickableWidget {
    private final Text label;
    private final float min;
    private final float max;
    private final float step;
    private final Supplier<Float> getter;
    private final Consumer<Float> setter;
    private final Function<Float, String> formatter;

    public ValueSlider(int x, int y, int w, int h, Text label, float min, float max, float step,
                       Supplier<Float> getter, Consumer<Float> setter, Function<Float, String> formatter) {
        super(x, y, w, h, label);
        this.label = label;
        this.min = min;
        this.max = max;
        this.step = step;
        this.getter = getter;
        this.setter = setter;
        this.formatter = formatter;
    }

    private float fraction() {
        float v = getter.get();
        float t = (v - min) / (max - min);
        return Math.max(0f, Math.min(1f, t));
    }

    private void setFromMouse(double mouseX) {
        double t = (mouseX - (getX() + 2)) / (double) (getWidth() - 4);
        t = Math.max(0.0, Math.min(1.0, t));
        float v = (float) (min + t * (max - min));
        if (step > 0f) {
            v = Math.round(v / step) * step;
        }
        v = Math.max(min, Math.min(max, v));
        setter.accept(v);
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
        int filled = Math.round(inner * fraction());
        context.fill(getX() + 2, getY() + 2, getX() + 2 + filled, getY() + getHeight() - 2, Theme.sliderFill());
        context.fill(getX() + 2 + filled - 1, getY() + 1, getX() + 2 + filled + 1, getY() + getHeight() - 1, 0xFFFFFFFF);

        TextRenderer font = MinecraftClient.getInstance().textRenderer;
        Text shown = Text.empty().append(label).append(Text.literal(": " + formatter.apply(getter.get())));
        int tw = font.getWidth(shown);
        int tx = getX() + (getWidth() - tw) / 2;
        int ty = getY() + (getHeight() - 8) / 2;
        context.drawText(font, shown, tx, ty, Theme.label(isHovered(), active), true);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    }
}
