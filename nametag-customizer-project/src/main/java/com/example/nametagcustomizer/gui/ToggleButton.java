package com.example.nametagcustomizer.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class ToggleButton extends ClickableWidget {
    private final BooleanSupplier getter;
    private final Consumer<Boolean> setter;

    public ToggleButton(int x, int y, int w, int h, Text label, BooleanSupplier getter, Consumer<Boolean> setter) {
        super(x, y, w, h, label);
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        setter.accept(!getter.getAsBoolean());
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        Theme.box(context, getX(), getY(), getWidth(), getHeight(), isHovered(), active, false);
        TextRenderer font = MinecraftClient.getInstance().textRenderer;
        int ty = getY() + (getHeight() - 8) / 2;
        context.drawText(font, getMessage(), getX() + 6, ty, Theme.label(isHovered(), active), false);

        boolean on = getter.getAsBoolean();
        Text state = Text.translatable(on ? "nametagcustomizer.gui.on" : "nametagcustomizer.gui.off");
        int sw = font.getWidth(state);
        context.drawText(font, state, getX() + getWidth() - 6 - sw, ty, on ? Theme.onColor() : Theme.offColor(), false);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    }
}
