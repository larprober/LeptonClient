package dev.lepton.utils.render.color;

public class Color {
    public int r, g, b, a;

    public Color() {
        this(255, 255, 255, 255);
    }

    public Color(int r, int g, int b) {
        this(r, g, b, 255);
    }

    public Color(int r, int g, int b, int a) {
        set(r, g, b, a);
    }

    public Color(Color other) {
        set(other);
    }

    /** Accepts a packed ARGB integer. */
    public Color(int packed) {
        set((packed >> 16) & 0xFF, (packed >> 8) & 0xFF, packed & 0xFF, (packed >> 24) & 0xFF);
    }

    public Color set(int r, int g, int b, int a) {
        this.r = clamp(r);
        this.g = clamp(g);
        this.b = clamp(b);
        this.a = clamp(a);
        return this;
    }

    public Color set(Color other) {
        return set(other.r, other.g, other.b, other.a);
    }

    public Color a(int a) {
        this.a = clamp(a);
        return this;
    }

    public Color copy() {
        return new Color(this);
    }

    /** Returns a new colour with alpha scaled by {@code factor}. */
    public Color withAlpha(double factor) {
        return copy().a((int) Math.round(this.a * factor));
    }

    /** Linear interpolation towards {@code target}. */
    public Color lerp(Color target, double t) {
        return new Color(
            (int) Math.round(r + (target.r - r) * t),
            (int) Math.round(g + (target.g - g) * t),
            (int) Math.round(b + (target.b - b) * t),
            (int) Math.round(a + (target.a - a) * t)
        );
    }

    /** Packed ARGB, the format Minecraft's 2D draw calls expect. */
    public int packed() {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /** Packed RGBA, used by some vertex consumers. */
    public int packedRgba() {
        return (r << 24) | (g << 16) | (b << 8) | a;
    }

    public float rf() { return r / 255f; }
    public float gf() { return g / 255f; }
    public float bf() { return b / 255f; }
    public float af() { return a / 255f; }

    /** Hue/saturation/value in 0..1 to RGB. Implemented inline so AWT never loads. */
    public static Color fromHsv(float h, float s, float v, int a) {
        h = ((h % 1f) + 1f) % 1f;

        int i = (int) (h * 6f);
        float f = h * 6f - i;
        float p = v * (1f - s);
        float q = v * (1f - f * s);
        float t = v * (1f - (1f - f) * s);

        float r, g, b;

        switch (i % 6) {
            case 0 -> { r = v; g = t; b = p; }
            case 1 -> { r = q; g = v; b = p; }
            case 2 -> { r = p; g = v; b = t; }
            case 3 -> { r = p; g = q; b = v; }
            case 4 -> { r = t; g = p; b = v; }
            default -> { r = v; g = p; b = q; }
        }

        return new Color(Math.round(r * 255f), Math.round(g * 255f), Math.round(b * 255f), a);
    }

    /** Returns {hue, saturation, value}, each in 0..1. */
    public float[] toHsv() {
        float rf = r / 255f, gf = g / 255f, bf = b / 255f;

        float max = Math.max(rf, Math.max(gf, bf));
        float min = Math.min(rf, Math.min(gf, bf));
        float d = max - min;

        float h = 0f;

        if (d != 0f) {
            if (max == rf) h = ((gf - bf) / d) % 6f;
            else if (max == gf) h = (bf - rf) / d + 2f;
            else h = (rf - gf) / d + 4f;

            h /= 6f;
            if (h < 0f) h += 1f;
        }

        return new float[] { h, max == 0f ? 0f : d / max, max };
    }

    public String toHex() {
        return String.format("#%02X%02X%02X%02X", r, g, b, a);
    }

    public static Color fromHex(String hex) {
        String s = hex.startsWith("#") ? hex.substring(1) : hex;

        if (s.length() == 6) {
            return new Color(
                Integer.parseInt(s.substring(0, 2), 16),
                Integer.parseInt(s.substring(2, 4), 16),
                Integer.parseInt(s.substring(4, 6), 16)
            );
        }

        if (s.length() == 8) {
            return new Color(
                Integer.parseInt(s.substring(0, 2), 16),
                Integer.parseInt(s.substring(2, 4), 16),
                Integer.parseInt(s.substring(4, 6), 16),
                Integer.parseInt(s.substring(6, 8), 16)
            );
        }

        throw new IllegalArgumentException("Not a hex colour: " + hex);
    }

    private static int clamp(int v) {
        return v < 0 ? 0 : Math.min(v, 255);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Color c)) return false;
        return r == c.r && g == c.g && b == c.b && a == c.a;
    }

    @Override
    public int hashCode() {
        return packed();
    }

    @Override
    public String toString() {
        return toHex();
    }
}
