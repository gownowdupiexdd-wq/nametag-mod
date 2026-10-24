package com.example.nametagcustomizer.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.function.IntSupplier;

/** A row with a label and a colored square. Clicking opens the color picker. */
public class ColorButton extends ClickableWidget {
    private final IntSupplier color;
    private final Runnable open;

    public ColorButton(int x, int y, int w, int h, Text label, IntSupplier color, Runnable open) {
        super(x, y, w, h, label);
        this.color = color;
        this.open = open;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        open.run();
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        Theme.box(context, getX(), getY(), getWidth(), getHeight(), isHovered(), active, false);
        TextRenderer font = MinecraftClient.getInstance().textRenderer;
        int ty = getY() + (getHeight() - 8) / 2;
        context.drawText(font, getMessage(), getX() + 6, ty, Theme.label(isHovered(), active), false);

        int size = getHeight() - 6;
        int sx = getX() + getWidth() - 4 - size;
        int sy = getY() + 3;
        boolean overSwatch = mouseX >= sx && mouseX < sx + size && mouseY >= sy && mouseY < sy + size;
        context.fill(sx - 1, sy - 1, sx + size + 1, sy + size + 1, overSwatch || isHovered() ? 0xFFFFFFFF : 0xFF000000);
        context.fill(sx, sy, sx + size, sy + size, 0xFF000000 | (color.getAsInt() & 0xFFFFFF));
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    }
}
