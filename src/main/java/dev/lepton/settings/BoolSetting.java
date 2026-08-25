package dev.lepton.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.function.Consumer;

public class BoolSetting extends Setting<Boolean> {
    public BoolSetting(String name, String description, Boolean defaultValue, Consumer<Boolean> onChanged, IVisible visible) {
        super(name, description, defaultValue, onChanged, visible);
    }

    @Override
    public boolean parse(String input) {
        String s = input.trim().toLowerCase();

        if (s.equals("true") || s.equals("on") || s.equals("yes") || s.equals("1")) return set(true);
        if (s.equals("false") || s.equals("off") || s.equals("no") || s.equals("0")) return set(false);

        return false;
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(get());
    }

    @Override
    public void fromJson(JsonElement element) {
        set(element.getAsBoolean());
    }

    public static class Builder extends Setting.Builder<Builder, Boolean, BoolSetting> {
        public Builder() {
            defaultValue = false;
        }

        @Override
        public BoolSetting build() {
            return new BoolSetting(name, description, defaultValue, onChanged, visible);
        }
    }
}
