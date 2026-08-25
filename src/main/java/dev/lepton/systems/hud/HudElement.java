package dev.lepton.systems.hud;

import com.google.gson.JsonObject;
import dev.lepton.gui.theme.Theme;
import dev.lepton.settings.BoolSetting;
import dev.lepton.settings.EnumSetting;
import dev.lepton.settings.IntSetting;
import dev.lepton.settings.SettingGroup;
import dev.lepton.settings.Settings;
import dev.lepton.utils.render.Renderer2D;
import net.minecraft.client.MinecraftClient;

public abstract class HudElement {
    public enum Anchor {
        TopLeft,
        TopRight,
        BottomLeft,
        BottomRight
    }

    public final String name;
    public final Settings settings = new Settings();
    protected final SettingGroup sgGeneral = settings.createGroup("General");

    public final BoolSetting enabled = sgGeneral.add(new BoolSetting.Builder()
        .name("enabled")
        .description("Show this element.")
        .defaultValue(true)
        .build());

    public final EnumSetting<Anchor> anchor = sgGeneral.add(new EnumSetting.Builder<Anchor>()
        .name("anchor")
        .description("Which corner of the screen to attach to.")
        .defaultValue(Anchor.TopLeft)
        .build());

    public final IntSetting offsetX = sgGeneral.add(new IntSetting.Builder()
        .name("offset-x")
        .description("Horizontal distance from the anchor corner.")
        .defaultValue(4)
        .range(0, 2000)
        .sliderRange(0, 400)
        .build());

    public final IntSetting offsetY = sgGeneral.add(new IntSetting.Builder()
        .name("offset-y")
        .description("Vertical distance from the anchor corner.")
        .defaultValue(4)
        .range(0, 2000)
        .sliderRange(0, 400)
        .build());

    protected HudElement(String name, Anchor defaultAnchor, int defaultX, int defaultY) {
        this.name = name;

        anchor.set(defaultAnchor);
        offsetX.set(defaultX);
        offsetY.set(defaultY);
    }

    /** Rendered width, used to right-align against the screen edge. */
    public abstract double width();

    public abstract double height();

    public abstract void render(Renderer2D r, Theme theme, double x, double y);

    /** Resolves the anchor into an actual top-left screen position. */
    public double resolvedX() {
        int screenWidth = MinecraftClient.getInstance().getWindow().getScaledWidth();

        return switch (anchor.get()) {
            case TopLeft, BottomLeft -> offsetX.get();
            case TopRight, BottomRight -> screenWidth - width() - offsetX.get();
        };
    }

    public double resolvedY() {
        int screenHeight = MinecraftClient.getInstance().getWindow().getScaledHeight();

        return switch (anchor.get()) {
            case TopLeft, TopRight -> offsetY.get();
            case BottomLeft, BottomRight -> screenHeight - height() - offsetY.get();
        };
    }

    public boolean rightAligned() {
        return anchor.get() == Anchor.TopRight || anchor.get() == Anchor.BottomRight;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        json.add("settings", settings.toJson());
        return json;
    }

    public void fromJson(JsonObject json) {
        if (json.has("settings")) settings.fromJson(json.getAsJsonObject("settings"));
    }
}
