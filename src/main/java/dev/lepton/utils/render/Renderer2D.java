package dev.lepton.utils.render;

import dev.lepton.utils.render.color.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/**
 * Screen-space drawing helpers. Thin wrappers over {@link DrawContext} that take
 * {@link Color} objects and doubles, so call sites stay readable.
 */
public class Renderer2D {
    private static final Renderer2D INSTANCE = new Renderer2D();

    private DrawContext context;

    public static Renderer2D of(DrawContext context) {
        INSTANCE.context = context;
        return INSTANCE;
    }

    public DrawContext context() {
        return context;
    }

    public static TextRenderer font() {
        return MinecraftClient.getInstance().textRenderer;
    }

    public static int textWidth(String text) {
        return font().getWidth(text);
    }

    public static int textHeight() {
        return font().fontHeight;
    }

    // -- fills ----------------------------------------------------------------

    public void rect(double x, double y, double width, double height, Color color) {
        context.fill((int) x, (int) y, (int) (x + width), (int) (y + height), color.packed());
    }

    public void gradientRect(double x, double y, double width, double height, Color top, Color bottom) {
        context.fillGradient((int) x, (int) y, (int) (x + width), (int) (y + height), top.packed(), bottom.packed());
    }

    /** A one-pixel outline drawn just inside the given bounds. */
    public void outline(double x, double y, double width, double height, Color color) {
        rect(x, y, width, 1, color);
        rect(x, y + height - 1, width, 1, color);
        rect(x, y + 1, 1, height - 2, color);
        rect(x + width - 1, y + 1, 1, height - 2, color);
    }

    public void outlinedRect(double x, double y, double width, double height, Color fill, Color border) {
        rect(x, y, width, height, fill);
        outline(x, y, width, height, border);
    }

    /**
     * A left-to-right accent bar, used for slider fills and the active marker on
     * module buttons.
     */
    public void bar(double x, double y, double width, double height, double progress, Color track, Color fill) {
        rect(x, y, width, height, track);

        double filled = Math.max(0, Math.min(1, progress)) * width;
        if (filled > 0) rect(x, y, filled, height, fill);
    }

    // -- text -----------------------------------------------------------------

    public void text(String text, double x, double y, Color color) {
        context.drawText(font(), text, (int) x, (int) y, color.packed(), false);
    }

    public void textShadowed(String text, double x, double y, Color color) {
        context.drawText(font(), text, (int) x, (int) y, color.packed(), true);
    }

    public void textCentered(String text, double centerX, double y, Color color) {
        text(text, centerX - textWidth(text) / 2.0, y, color);
    }

    public void textRight(String text, double rightX, double y, Color color) {
        text(text, rightX - textWidth(text), y, color);
    }

    // -- clipping -------------------------------------------------------------

    public void pushClip(double x, double y, double width, double height) {
        context.enableScissor((int) x, (int) y, (int) (x + width), (int) (y + height));
    }

    public void popClip() {
        context.disableScissor();
    }
}
