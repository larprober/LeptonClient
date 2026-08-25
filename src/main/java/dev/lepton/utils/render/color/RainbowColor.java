package dev.lepton.utils.render.color;

/** A colour that can optionally cycle its hue over time. */
public class RainbowColor extends Color {
    public boolean rainbow;
    public double rainbowSpeed = 0.5;
    public double rainbowSpread;

    private static double globalHue;

    public RainbowColor() {
        super();
    }

    public RainbowColor(int r, int g, int b, int a) {
        super(r, g, b, a);
    }

    public RainbowColor(Color other) {
        super(other);
    }

    public static void updateGlobal(double deltaSeconds) {
        globalHue = (globalHue + deltaSeconds * 0.1) % 1.0;
    }

    /** The colour to actually draw with this frame. */
    public Color resolve() {
        if (!rainbow) return this;

        double hue = (globalHue * rainbowSpeed * 4 + rainbowSpread) % 1.0;
        return Color.fromHsv((float) hue, 1f, 1f, a);
    }

    public RainbowColor copyRainbow() {
        RainbowColor c = new RainbowColor(this);
        c.rainbow = rainbow;
        c.rainbowSpeed = rainbowSpeed;
        c.rainbowSpread = rainbowSpread;
        return c;
    }
}
