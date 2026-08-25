package dev.lepton.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.function.Consumer;

public class StringSetting extends Setting<String> {
    public StringSetting(String name, String description, String defaultValue, Consumer<String> onChanged, IVisible visible) {
        super(name, description, defaultValue, onChanged, visible);
    }

    @Override
    protected String transformValue(String value) {
        return value == null ? "" : value;
    }

    @Override
    public boolean parse(String input) {
        return set(input);
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(get());
    }

    @Override
    public void fromJson(JsonElement element) {
        set(element.getAsString());
    }

    public static class Builder extends Setting.Builder<Builder, String, StringSetting> {
        public Builder() {
            defaultValue = "";
        }

        @Override
        public StringSetting build() {
            return new StringSetting(name, description, defaultValue, onChanged, visible);
        }
    }
}
