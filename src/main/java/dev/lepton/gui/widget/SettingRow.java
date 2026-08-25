package dev.lepton.gui.widget;

import dev.lepton.gui.Icons;
import dev.lepton.gui.theme.Theme;
import dev.lepton.settings.BoolSetting;
import dev.lepton.settings.ColorSetting;
import dev.lepton.settings.DoubleSetting;
import dev.lepton.settings.EnumSetting;
import dev.lepton.settings.IntSetting;
import dev.lepton.settings.KeybindSetting;
import dev.lepton.settings.RegistryListSetting;
import dev.lepton.settings.Setting;
import dev.lepton.settings.StringSetting;
import dev.lepton.utils.misc.Keybind;
import dev.lepton.utils.render.Renderer2D;
import dev.lepton.utils.render.color.Color;
import dev.lepton.utils.render.color.RainbowColor;

/**
 * One row in a module's settings list. Renders and drives a single {@link Setting},
 * dispatching on its concrete type.
 */
public class SettingRow {
    public static final double ROW_HEIGHT = 13;

    public final Setting<?> setting;

    public double x, y, width;

    private boolean draggingSlider;
    private boolean editingText;
    private boolean capturingBind;
    private boolean colorExpanded;
    private StringBuilder textBuffer;

    /** Which channel of a colour setting is being dragged: 0=R 1=G 2=B 3=A, -1 = none. */
    private int colorChannel = -1;

    public SettingRow(Setting<?> setting) {
        this.setting = setting;
    }

    public double height() {
        if (setting instanceof ColorSetting && colorExpanded) return ROW_HEIGHT + ROW_HEIGHT * 5;
        return ROW_HEIGHT;
    }

    // -- rendering ------------------------------------------------------------

    public void render(Renderer2D r, Theme theme, double mouseX, double mouseY) {
        boolean hovered = contains(mouseX, mouseY, x, y, width, ROW_HEIGHT);
        double textY = y + (ROW_HEIGHT - Renderer2D.textHeight()) / 2.0 + 1;

        if (hovered) r.rect(x, y, width, ROW_HEIGHT, theme.surfaceRaised);

        if (setting instanceof BoolSetting bool) {
            renderLabel(r, theme, textY);
            renderCheckbox(r, theme, bool.get());
        } else if (setting instanceof IntSetting || setting instanceof DoubleSetting) {
            renderSlider(r, theme, textY);
        } else if (setting instanceof EnumSetting<?> enumSetting) {
            renderLabel(r, theme, textY);
            r.textRight(enumSetting.get().name(), x + width - 4, textY, theme.accent);
        } else if (setting instanceof StringSetting stringSetting) {
            renderLabel(r, theme, textY);
            String shown = editingText ? textBuffer + "_" : stringSetting.get();
            r.textRight(shown, x + width - 4, textY, editingText ? theme.accentBright : theme.text);
        } else if (setting instanceof KeybindSetting keybindSetting) {
            renderLabel(r, theme, textY);
            String shown = capturingBind ? "press a key..." : keybindSetting.get().toString();
            r.textRight(shown, x + width - 4, textY, capturingBind ? theme.warning : theme.accent);
        } else if (setting instanceof ColorSetting colorSetting) {
            renderLabel(r, theme, textY);
            renderColorSwatch(r, theme, colorSetting);
            if (colorExpanded) renderColorChannels(r, theme, colorSetting, mouseX, mouseY);
        } else if (setting instanceof RegistryListSetting<?> listSetting) {
            renderLabel(r, theme, textY);
            r.textRight(listSetting.get().size() + " selected", x + width - 4, textY, theme.accent);
        } else {
            renderLabel(r, theme, textY);
        }
    }

    private void renderLabel(Renderer2D r, Theme theme, double textY) {
        r.text(setting.title, x + 6, textY, theme.textDim);
    }

    private void renderCheckbox(Renderer2D r, Theme theme, boolean on) {
        double size = 8;
        double bx = x + width - size - 5;
        double by = y + (ROW_HEIGHT - size) / 2.0;

        r.rect(bx, by, size, size, on ? theme.accent : theme.background);
        r.outline(bx, by, size, size, on ? theme.accentBright : theme.outline);

        if (on) Icons.draw(r, Icons.CHECK, bx + 1, by + 1, 1, theme.text);
    }

    private void renderSlider(Renderer2D r, Theme theme, double textY) {
        double progress;
        String valueText;

        if (setting instanceof IntSetting intSetting) {
            progress = normalise(intSetting.get(), intSetting.sliderMin, intSetting.sliderMax);
            valueText = String.valueOf(intSetting.get());
        } else {
            DoubleSetting doubleSetting = (DoubleSetting) setting;
            progress = normalise(doubleSetting.get(), doubleSetting.sliderMin, doubleSetting.sliderMax);
            valueText = trim(doubleSetting.get(), doubleSetting.decimalPlaces);
        }

        r.text(setting.title, x + 6, textY - 2, theme.textDim);
        r.textRight(valueText, x + width - 5, textY - 2, theme.text);

        double trackY = y + ROW_HEIGHT - 4;
        double trackX = x + 6;
        double trackW = width - 12;

        r.bar(trackX, trackY, trackW, 2, progress, theme.background, theme.accent);

        double knobX = trackX + trackW * progress;
        r.rect(knobX - 1.5, trackY - 2, 3, 6, theme.accentBright);
    }

    private void renderColorSwatch(Renderer2D r, Theme theme, ColorSetting colorSetting) {
        RainbowColor value = colorSetting.get();
        Color shown = value.resolve();

        double size = 9;
        double bx = x + width - size - 5;
        double by = y + (ROW_HEIGHT - size) / 2.0;

        r.rect(bx, by, size, size, shown);
        r.outline(bx, by, size, size, theme.outline);
    }

    private void renderColorChannels(Renderer2D r, Theme theme, ColorSetting colorSetting, double mouseX, double mouseY) {
        RainbowColor value = colorSetting.get();
        String[] labels = { "R", "G", "B", "A" };
        int[] values = { value.r, value.g, value.b, value.a };

        for (int i = 0; i < 4; i++) {
            double rowY = y + ROW_HEIGHT + i * ROW_HEIGHT;
            double textY = rowY + (ROW_HEIGHT - Renderer2D.textHeight()) / 2.0;

            r.text(labels[i], x + 10, textY - 2, theme.textDim);
            r.textRight(String.valueOf(values[i]), x + width - 5, textY - 2, theme.text);

            double trackX = x + 20;
            double trackW = width - 26;
            r.bar(trackX, rowY + ROW_HEIGHT - 4, trackW, 2, values[i] / 255.0, theme.background, channelColor(theme, i));
        }

        double rainbowY = y + ROW_HEIGHT + 4 * ROW_HEIGHT;
        double textY = rainbowY + (ROW_HEIGHT - Renderer2D.textHeight()) / 2.0 + 1;

        r.text("Rainbow", x + 10, textY, theme.textDim);

        double size = 8;
        double bx = x + width - size - 5;
        double by = rainbowY + (ROW_HEIGHT - size) / 2.0;

        r.rect(bx, by, size, size, value.rainbow ? theme.accent : theme.background);
        r.outline(bx, by, size, size, theme.outline);
        if (value.rainbow) Icons.draw(r, Icons.CHECK, bx + 1, by + 1, 1, theme.text);
    }

    private Color channelColor(Theme theme, int channel) {
        return switch (channel) {
            case 0 -> new Color(240, 80, 80);
            case 1 -> new Color(80, 220, 120);
            case 2 -> new Color(90, 140, 255);
            default -> theme.textDim;
        };
    }

    // -- input ----------------------------------------------------------------

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (setting instanceof ColorSetting colorSetting && colorExpanded) {
            for (int i = 0; i < 4; i++) {
                double rowY = y + ROW_HEIGHT + i * ROW_HEIGHT;

                if (contains(mouseX, mouseY, x, rowY, width, ROW_HEIGHT)) {
                    colorChannel = i;
                    applyColorChannel(colorSetting, mouseX);
                    return true;
                }
            }

            double rainbowY = y + ROW_HEIGHT + 4 * ROW_HEIGHT;
            if (contains(mouseX, mouseY, x, rainbowY, width, ROW_HEIGHT)) {
                RainbowColor value = colorSetting.get();
                value.rainbow = !value.rainbow;
                return true;
            }
        }

        if (!contains(mouseX, mouseY, x, y, width, ROW_HEIGHT)) return false;

        // Right-click resets any setting to its default.
        if (button == 1 && !(setting instanceof ColorSetting)) {
            setting.reset();
            return true;
        }

        if (setting instanceof BoolSetting bool) {
            bool.set(!bool.get());
            return true;
        }

        if (setting instanceof IntSetting || setting instanceof DoubleSetting) {
            draggingSlider = true;
            applySlider(mouseX);
            return true;
        }

        if (setting instanceof EnumSetting<?> enumSetting) {
            enumSetting.cycle();
            return true;
        }

        if (setting instanceof StringSetting stringSetting) {
            editingText = true;
            textBuffer = new StringBuilder(stringSetting.get());
            return true;
        }

        if (setting instanceof KeybindSetting) {
            capturingBind = true;
            return true;
        }

        if (setting instanceof ColorSetting) {
            if (button == 1) colorExpanded = !colorExpanded;
            else colorExpanded = !colorExpanded;
            return true;
        }

        return false;
    }

    public void mouseReleased() {
        draggingSlider = false;
        colorChannel = -1;
    }

    public void mouseDragged(double mouseX) {
        if (draggingSlider) applySlider(mouseX);

        if (colorChannel >= 0 && setting instanceof ColorSetting colorSetting) {
            applyColorChannel(colorSetting, mouseX);
        }
    }

    public boolean keyPressed(int key, int modifiers) {
        if (capturingBind && setting instanceof KeybindSetting keybindSetting) {
            // Escape clears the bind rather than assigning Escape to it.
            if (key == 256) keybindSetting.get().clear();
            else keybindSetting.get().set(true, key);

            capturingBind = false;
            return true;
        }

        if (editingText && setting instanceof StringSetting stringSetting) {
            if (key == 256 || key == 257) { // escape or enter
                if (key == 257) stringSetting.set(textBuffer.toString());
                editingText = false;
                return true;
            }

            if (key == 259 && textBuffer.length() > 0) { // backspace
                textBuffer.deleteCharAt(textBuffer.length() - 1);
                return true;
            }

            return true;
        }

        return false;
    }

    public boolean mouseButtonBind(int button) {
        if (!capturingBind || !(setting instanceof KeybindSetting keybindSetting)) return false;

        keybindSetting.get().set(false, button);
        capturingBind = false;
        return true;
    }

    public boolean charTyped(char c) {
        if (!editingText) return false;

        textBuffer.append(c);
        return true;
    }

    public boolean isCapturing() {
        return capturingBind || editingText;
    }

    // -- helpers --------------------------------------------------------------

    private void applySlider(double mouseX) {
        double trackX = x + 6;
        double trackW = width - 12;
        double progress = clamp((mouseX - trackX) / trackW);

        if (setting instanceof IntSetting intSetting) {
            int value = (int) Math.round(intSetting.sliderMin + (intSetting.sliderMax - intSetting.sliderMin) * progress);
            intSetting.set(value);
        } else if (setting instanceof DoubleSetting doubleSetting) {
            double value = doubleSetting.sliderMin + (doubleSetting.sliderMax - doubleSetting.sliderMin) * progress;
            doubleSetting.set(round(value, doubleSetting.decimalPlaces));
        }
    }

    private void applyColorChannel(ColorSetting colorSetting, double mouseX) {
        double trackX = x + 20;
        double trackW = width - 26;
        int value = (int) Math.round(clamp((mouseX - trackX) / trackW) * 255);

        RainbowColor color = colorSetting.get();

        switch (colorChannel) {
            case 0 -> color.r = value;
            case 1 -> color.g = value;
            case 2 -> color.b = value;
            case 3 -> color.a = value;
        }
    }

    private static double normalise(double value, double min, double max) {
        if (max - min == 0) return 0;
        return clamp((value - min) / (max - min));
    }

    private static double clamp(double v) {
        return v < 0 ? 0 : Math.min(v, 1);
    }

    private static double round(double value, int places) {
        double factor = Math.pow(10, places);
        return Math.round(value * factor) / factor;
    }

    private static String trim(double value, int places) {
        return String.format("%." + places + "f", value);
    }

    public static boolean contains(double mouseX, double mouseY, double x, double y, double w, double h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }
}
