package dev.lepton.settings;

import com.google.gson.JsonElement;
import dev.lepton.utils.misc.Keybind;

import java.util.function.Consumer;

public class KeybindSetting extends Setting<Keybind> {
    public KeybindSetting(String name, String description, Keybind defaultValue, Consumer<Keybind> onChanged, IVisible visible) {
        super(name, description, defaultValue, onChanged, visible);
    }

    @Override
    protected Keybind transformValue(Keybind value) {
        return value == null ? Keybind.none() : value;
    }

    @Override
    public boolean parse(String input) {
        if (input.trim().equalsIgnoreCase("none")) {
            get().clear();
            onChanged();
            return true;
        }

        return false;
    }

    @Override
    public JsonElement toJson() {
        return get().toJson();
    }

    @Override
    public void fromJson(JsonElement element) {
        set(Keybind.fromJson(element.getAsJsonObject()));
    }

    public static class Builder extends Setting.Builder<Builder, Keybind, KeybindSetting> {
        public Builder() {
            defaultValue = Keybind.none();
        }

        @Override
        public KeybindSetting build() {
            return new KeybindSetting(name, description, defaultValue, onChanged, visible);
        }
    }
}
