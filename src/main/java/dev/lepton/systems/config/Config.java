package dev.lepton.systems.config;

import com.google.gson.JsonObject;
import dev.lepton.gui.theme.Theme;
import dev.lepton.gui.theme.Themes;
import dev.lepton.settings.BoolSetting;
import dev.lepton.settings.ColorSetting;
import dev.lepton.settings.DoubleSetting;
import dev.lepton.settings.IntSetting;
import dev.lepton.settings.KeybindSetting;
import dev.lepton.settings.SettingGroup;
import dev.lepton.settings.Settings;
import dev.lepton.settings.StringSetting;
import dev.lepton.systems.LeptonSystem;
import dev.lepton.systems.Systems;
import dev.lepton.utils.misc.Keybind;
import dev.lepton.utils.render.color.RainbowColor;
import org.lwjgl.glfw.GLFW;

/** Client-wide settings: appearance, keybinds, chat behaviour. */
public class Config extends LeptonSystem {
    public final Settings settings = new Settings();

    private final SettingGroup sgGeneral = settings.createGroup("General");
    private final SettingGroup sgAppearance = settings.createGroup("Appearance");
    private final SettingGroup sgChat = settings.createGroup("Chat");

    // -- general --

    public final KeybindSetting guiKeybind = sgGeneral.add(new KeybindSetting.Builder()
        .name("gui-keybind")
        .description("Opens the Lepton ClickGUI.")
        .defaultValue(Keybind.fromKey(GLFW.GLFW_KEY_RIGHT_SHIFT))
        .build());

    public final StringSetting prefix = sgGeneral.add(new StringSetting.Builder()
        .name("command-prefix")
        .description("Character that starts a Lepton chat command.")
        .defaultValue(".")
        .build());

    public final BoolSetting restoreModules = sgGeneral.add(new BoolSetting.Builder()
        .name("restore-modules")
        .description("Re-enable whatever was on last session, once you load a singleplayer world.")
        .defaultValue(true)
        .build());

    // -- appearance --

    public final StringSetting themeName = sgAppearance.add(new StringSetting.Builder()
        .name("theme")
        .description("Which shipped palette to use.")
        .defaultValue(Themes.LEPTON_BLUE.name)
        .onChanged(v -> resolved = null)
        .build());

    public final BoolSetting customAccent = sgAppearance.add(new BoolSetting.Builder()
        .name("custom-accent")
        .description("Override the theme's accent colour with one of your own.")
        .defaultValue(false)
        .onChanged(v -> resolved = null)
        .build());

    public final ColorSetting accentColor = sgAppearance.add(new ColorSetting.Builder()
        .name("accent-colour")
        .description("Your own accent colour.")
        .defaultValue(0x2E, 0x7D, 0xF6, 255)
        .visible(() -> customAccent.get())
        .onChanged(v -> resolved = null)
        .build());

    public final DoubleSetting guiScale = sgAppearance.add(new DoubleSetting.Builder()
        .name("gui-scale")
        .description("Size of the ClickGUI.")
        .defaultValue(1.0)
        .range(0.5, 3.0)
        .sliderRange(0.5, 2.5)
        .build());

    public final IntSetting backgroundBlur = sgAppearance.add(new IntSetting.Builder()
        .name("background-dim")
        .description("How much to darken the world behind the ClickGUI.")
        .defaultValue(96)
        .range(0, 255)
        .sliderRange(0, 255)
        .build());

    public final BoolSetting rainbowAccent = sgAppearance.add(new BoolSetting.Builder()
        .name("rainbow-accent")
        .description("Cycle the accent colour through the spectrum.")
        .defaultValue(false)
        .onChanged(v -> resolved = null)
        .build());

    public final DoubleSetting rainbowSpeed = sgAppearance.add(new DoubleSetting.Builder()
        .name("rainbow-speed")
        .description("How fast the accent cycles.")
        .defaultValue(0.5)
        .range(0.05, 5.0)
        .sliderRange(0.05, 2.0)
        .visible(() -> rainbowAccent.get())
        .build());

    // -- chat --

    public final BoolSetting moduleToggleMessages = sgChat.add(new BoolSetting.Builder()
        .name("module-toggle-messages")
        .description("Print a line in chat when a module turns on or off.")
        .defaultValue(true)
        .build());

    /** Cached resolved theme; invalidated whenever an appearance setting changes. */
    private Theme resolved;

    public Config() {
        super("config");
    }

    public static Config get() {
        return Systems.get(Config.class);
    }

    /**
     * The palette to draw with right now, including any accent override.
     * Cached because this is called many times per frame.
     */
    public Theme theme() {
        if (resolved != null) return resolved;

        Theme base = Themes.byName(themeName.get());
        if (base == null) base = Themes.LEPTON_BLUE;

        if (!customAccent.get() && !rainbowAccent.get()) {
            resolved = base;
            return resolved;
        }

        Theme copy = new Theme(base.name);

        copy.background = base.background;
        copy.surface = base.surface;
        copy.surfaceRaised = base.surfaceRaised;
        copy.outline = base.outline;
        copy.text = base.text;
        copy.textDim = base.textDim;
        copy.textDisabled = base.textDisabled;
        copy.positive = base.positive;
        copy.negative = base.negative;
        copy.warning = base.warning;

        RainbowColor accent = accentColor.get();
        copy.accent = customAccent.get() ? accent.copy() : base.accent;
        copy.accentBright = copy.accent.lerp(copy.accent.copy().set(255, 255, 255, copy.accent.a), 0.35);
        copy.accentDeep = copy.accent.lerp(copy.accent.copy().set(0, 0, 0, copy.accent.a), 0.35);

        // Rainbow re-resolves every frame, so do not cache it.
        if (rainbowAccent.get()) return copy;

        resolved = copy;
        return resolved;
    }

    public void invalidateTheme() {
        resolved = null;
    }

    @Override
    public JsonObject toJson() {
        return settings.toJson();
    }

    @Override
    public void fromJson(JsonObject json) {
        settings.fromJson(json);
        resolved = null;
    }
}
