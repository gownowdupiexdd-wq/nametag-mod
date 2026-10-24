package com.example.nametagcustomizer.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.function.BooleanSupplier;

public class ThemedButton extends ClickableWidget {
    private final Runnable action;
    private final BooleanSupplier selected;

    public ThemedButton(int x, int y, int w, int h, Text label, Runnable action) {
        this(x, y, w, h, label, action, null);
    }

    public ThemedButton(int x, int y, int w, int h, Text label, Runnable action, BooleanSupplier selected) {
        super(x, y, w, h, label);
        this.action = action;
        this.selected = selected;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (action != null) {
            action.run();
        }
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        boolean sel = selected != null && selected.getAsBoolean();
        Theme.box(context, getX(), getY(), getWidth(), getHeight(), isHovered(), active, sel);
        TextRenderer font = MinecraftClient.getInstance().textRenderer;
        Text label = getMessage();
        int tw = font.getWidth(label);
        int tx = getX() + (getWidth() - tw) / 2;
        int ty = getY() + (getHeight() - 8) / 2;
        context.drawText(font, label, tx, ty, Theme.label(isHovered() || sel, active), false);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    }
}
