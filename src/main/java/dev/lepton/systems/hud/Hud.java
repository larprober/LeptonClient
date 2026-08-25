package dev.lepton.systems.hud;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.lepton.Lepton;
import dev.lepton.event.EventHandler;
import dev.lepton.event.events.Render2DEvent;
import dev.lepton.gui.theme.Theme;
import dev.lepton.gui.theme.Themes;
import dev.lepton.settings.BoolSetting;
import dev.lepton.settings.SettingGroup;
import dev.lepton.settings.Settings;
import dev.lepton.systems.LeptonSystem;
import dev.lepton.systems.Systems;
import dev.lepton.systems.config.Config;
import dev.lepton.systems.hud.elements.ActiveModules;
import dev.lepton.systems.hud.elements.InfoPanel;
import dev.lepton.systems.hud.elements.Watermark;
import dev.lepton.utils.render.Renderer2D;
import dev.lepton.utils.render.color.RainbowColor;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

public class Hud extends LeptonSystem {
    public final Settings settings = new Settings();
    private final SettingGroup sgGeneral = settings.createGroup("General");

    public final BoolSetting enabled = sgGeneral.add(new BoolSetting.Builder()
        .name("enabled")
        .description("Draw the Lepton HUD.")
        .defaultValue(true)
        .build());

    public final BoolSetting hideInGuis = sgGeneral.add(new BoolSetting.Builder()
        .name("hide-in-guis")
        .description("Hide the HUD while a screen is open.")
        .defaultValue(false)
        .build());

    private final List<HudElement> elements = new ArrayList<>();

    private long lastFrameTime = System.nanoTime();

    public Hud() {
        super("hud");
    }

    public static Hud get() {
        return Systems.get(Hud.class);
    }

    @Override
    public void init() {
        elements.add(new Watermark());
        elements.add(new ActiveModules());
        elements.add(new InfoPanel());

        Lepton.EVENTS.subscribe(this);
    }

    public List<HudElement> elements() {
        return elements;
    }

    @EventHandler
    private void onRender(Render2DEvent event) {
        if (!enabled.get()) return;
        if (hideInGuis.get() && MinecraftClient.getInstance().currentScreen != null) return;

        // Drive the shared rainbow phase from real elapsed time so it runs at the same
        // rate regardless of frame rate.
        long now = System.nanoTime();
        RainbowColor.updateGlobal((now - lastFrameTime) / 1.0e9);
        lastFrameTime = now;

        Renderer2D r = Renderer2D.of(event.context);
        Theme theme = theme();

        for (HudElement element : elements) {
            if (!element.enabled.get()) continue;

            try {
                element.render(r, theme, element.resolvedX(), element.resolvedY());
            } catch (Exception e) {
                Lepton.LOG.error("HUD element {} threw while rendering", element.name, e);
            }
        }
    }

    private Theme theme() {
        Config config = Config.get();
        return config == null ? Themes.LEPTON_BLUE : config.theme();
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = settings.toJson();

        JsonArray array = new JsonArray();
        for (HudElement element : elements) array.add(element.toJson());
        json.add("elements", array);

        return json;
    }

    @Override
    public void fromJson(JsonObject json) {
        settings.fromJson(json);

        if (!json.has("elements")) return;

        for (JsonElement entry : json.getAsJsonArray("elements")) {
            JsonObject object = entry.getAsJsonObject();
            if (!object.has("name")) continue;

            String name = object.get("name").getAsString();

            for (HudElement element : elements) {
                if (element.name.equals(name)) {
                    element.fromJson(object);
                    break;
                }
            }
        }
    }
}
