package dev.lepton.gui.theme;

import dev.lepton.utils.render.color.Color;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Lepton's shipped palettes. All of them share the same dark neutral base and differ
 * only in the accent triad, so switching theme never changes the layout's contrast --
 * just its identity colour.
 *
 * <p>The default, Lepton Blue, is the one the client is named around: a cold electric
 * blue against near-black navy.
 */
public class Themes {
    private static final Map<String, Theme> BY_NAME = new LinkedHashMap<>();

    public static final Theme LEPTON_BLUE = register(base("Lepton Blue",
        new Color(0x2E, 0x7D, 0xF6),
        new Color(0x5F, 0xB0, 0xFF),
        new Color(0x1B, 0x4F, 0xBF)));

    public static final Theme CHERENKOV = register(base("Cherenkov",
        new Color(0x00, 0xB8, 0xD4),
        new Color(0x4F, 0xE8, 0xFF),
        new Color(0x00, 0x77, 0x91)));

    public static final Theme MUON = register(base("Muon",
        new Color(0x7C, 0x5C, 0xFF),
        new Color(0xA8, 0x92, 0xFF),
        new Color(0x51, 0x35, 0xC4)));

    public static final Theme NEUTRINO = register(base("Neutrino",
        new Color(0x17, 0xC9, 0x8D),
        new Color(0x5B, 0xEF, 0xBB),
        new Color(0x0D, 0x8A, 0x60)));

    public static final Theme TAU = register(base("Tau",
        new Color(0xFF, 0x4D, 0x6D),
        new Color(0xFF, 0x8A, 0xA0),
        new Color(0xC2, 0x24, 0x43)));

    public static final Theme GRAPHITE = register(base("Graphite",
        new Color(0x9A, 0xA7, 0xB8),
        new Color(0xC6, 0xD1, 0xDE),
        new Color(0x66, 0x72, 0x82)));

    /**
     * Every palette shares this neutral chassis. Only the three accent colours vary,
     * which is what keeps the set feeling like one design rather than six.
     */
    private static Theme base(String name, Color accent, Color accentBright, Color accentDeep) {
        Theme theme = new Theme(name);

        theme.background = new Color(0x0A, 0x0E, 0x16, 240);
        theme.surface = new Color(0x12, 0x1A, 0x28, 255);
        theme.surfaceRaised = new Color(0x1A, 0x25, 0x36, 255);
        theme.outline = new Color(0x24, 0x34, 0x4B, 255);

        theme.accent = accent;
        theme.accentBright = accentBright;
        theme.accentDeep = accentDeep;

        theme.text = new Color(0xE8, 0xEF, 0xF9);
        theme.textDim = new Color(0x8F, 0xA0, 0xB8);
        theme.textDisabled = new Color(0x57, 0x65, 0x7A);

        theme.positive = new Color(0x3D, 0xD6, 0x8C);
        theme.negative = new Color(0xF4, 0x58, 0x5C);
        theme.warning = new Color(0xF5, 0xB9, 0x42);

        return theme;
    }

    private static Theme register(Theme theme) {
        BY_NAME.put(theme.name.toLowerCase(), theme);
        return theme;
    }

    public static Theme byName(String name) {
        return BY_NAME.get(name == null ? "" : name.toLowerCase());
    }

    public static Theme[] all() {
        return BY_NAME.values().toArray(new Theme[0]);
    }

    public static String[] names() {
        return BY_NAME.values().stream().map(t -> t.name).toArray(String[]::new);
    }
}
