package dev.lepton.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.lepton.utils.render.color.RainbowColor;

import java.util.function.Consumer;

public class ColorSetting extends Setting<RainbowColor> {
    public ColorSetting(String name, String description, RainbowColor defaultValue, Consumer<RainbowColor> onChanged, IVisible visible) {
        super(name, description, defaultValue, onChanged, visible);
    }

    @Override
    protected RainbowColor transformValue(RainbowColor value) {
        // Settings own their colour instance -- copy so the caller's default is never mutated.
        return value == null ? new RainbowColor() : value.copyRainbow();
    }

    @Override
    public boolean parse(String input) {
        try {
            return set(new RainbowColor(dev.lepton.utils.render.color.Color.fromHex(input.trim())));
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public JsonElement toJson() {
        RainbowColor c = get();

        JsonObject json = new JsonObject();
        json.addProperty("r", c.r);
        json.addProperty("g", c.g);
        json.addProperty("b", c.b);
        json.addProperty("a", c.a);
        json.addProperty("rainbow", c.rainbow);
        json.addProperty("rainbowSpeed", c.rainbowSpeed);
        json.addProperty("rainbowSpread", c.rainbowSpread);
        return json;
    }

    @Override
    public void fromJson(JsonElement element) {
        JsonObject json = element.getAsJsonObject();

        RainbowColor c = new RainbowColor(
            json.get("r").getAsInt(),
            json.get("g").getAsInt(),
            json.get("b").getAsInt(),
            json.get("a").getAsInt()
        );

        if (json.has("rainbow")) c.rainbow = json.get("rainbow").getAsBoolean();
        if (json.has("rainbowSpeed")) c.rainbowSpeed = json.get("rainbowSpeed").getAsDouble();
        if (json.has("rainbowSpread")) c.rainbowSpread = json.get("rainbowSpread").getAsDouble();

        set(c);
    }

    public static class Builder extends Setting.Builder<Builder, RainbowColor, ColorSetting> {
        public Builder() {
            defaultValue = new RainbowColor(255, 255, 255, 255);
        }

        public Builder defaultValue(int r, int g, int b, int a) {
            this.defaultValue = new RainbowColor(r, g, b, a);
            return this;
        }

        @Override
        public ColorSetting build() {
            return new ColorSetting(name, description, defaultValue, onChanged, visible);
        }
    }
}
