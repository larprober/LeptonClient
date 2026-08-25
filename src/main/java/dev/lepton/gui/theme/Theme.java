package dev.lepton.gui.theme;

import dev.lepton.utils.render.color.Color;

/**
 * A named palette. Lepton ships several; the user can pick one in the GUI settings
 * or hand-edit the accent to anything they like.
 */
public class Theme {
    public final String name;

    /** Window and panel fills, darkest to lightest. */
    public Color background;
    public Color surface;
    public Color surfaceRaised;
    public Color outline;

    /** The identity colour -- title bars, active toggles, slider fills, the logo. */
    public Color accent;
    /** A lighter partner to {@link #accent}, used for gradients and hover states. */
    public Color accentBright;
    /** A darker partner, used for pressed states and shadows. */
    public Color accentDeep;

    public Color text;
    public Color textDim;
    public Color textDisabled;

    public Color positive;
    public Color negative;
    public Color warning;

    public Theme(String name) {
        this.name = name;
    }

    /** Fill used behind a module button, blending toward the accent when active. */
    public Color moduleFill(boolean active, boolean hovered) {
        if (active) return hovered ? accentBright : accent;
        if (hovered) return surfaceRaised;
        return surface;
    }

    public Color moduleText(boolean active) {
        return active ? text : textDim;
    }

    @Override
    public String toString() {
        return name;
    }
}
