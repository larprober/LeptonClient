package dev.lepton.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.function.Consumer;

public class EnumSetting<T extends Enum<T>> extends Setting<T> {
    private final T[] values;

    public EnumSetting(String name, String description, T defaultValue, Consumer<T> onChanged, IVisible visible) {
        super(name, description, defaultValue, onChanged, visible);

        this.values = defaultValue.getDeclaringClass().getEnumConstants();
    }

    public T[] getValues() {
        return values;
    }

    /** Advances to the next constant, wrapping around. */
    public void cycle() {
        set(values[(get().ordinal() + 1) % values.length]);
    }

    @Override
    public boolean parse(String input) {
        for (T value : values) {
            if (value.name().equalsIgnoreCase(input.trim())) return set(value);
        }
        return false;
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(get().name());
    }

    @Override
    public void fromJson(JsonElement element) {
        String name = element.getAsString();

        for (T value : values) {
            if (value.name().equals(name)) {
                set(value);
                return;
            }
        }

        reset();
    }

    public static class Builder<T extends Enum<T>> extends Setting.Builder<Builder<T>, T, EnumSetting<T>> {
        @Override
        public EnumSetting<T> build() {
            return new EnumSetting<>(name, description, defaultValue, onChanged, visible);
        }
    }
}
