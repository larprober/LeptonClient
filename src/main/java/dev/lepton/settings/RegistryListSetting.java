package dev.lepton.settings;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A multi-select over any Minecraft registry -- blocks, items, entity types.
 * Serialises to an array of namespaced ids so configs survive registry changes.
 */
public abstract class RegistryListSetting<T> extends Setting<List<T>> {
    protected final Registry<T> registry;

    public RegistryListSetting(String name, String description, List<T> defaultValue, Consumer<List<T>> onChanged,
                               IVisible visible, Registry<T> registry) {
        super(name, description, defaultValue, onChanged, visible);

        this.registry = registry;
    }

    @Override
    protected List<T> transformValue(List<T> value) {
        return value == null ? new ArrayList<>() : new ArrayList<>(value);
    }

    public boolean contains(T value) {
        return get().contains(value);
    }

    public void toggle(T value) {
        List<T> list = get();

        if (list.contains(value)) list.remove(value);
        else list.add(value);

        onChanged();
    }

    public void clear() {
        get().clear();
        onChanged();
    }

    @Override
    public boolean parse(String input) {
        List<T> parsed = new ArrayList<>();

        for (String part : input.split(",")) {
            part = part.trim();
            if (part.isEmpty()) continue;

            Identifier id = Identifier.tryParse(part);
            if (id == null) return false;

            T value = registry.get(id);
            if (value == null) return false;

            parsed.add(value);
        }

        return set(parsed);
    }

    @Override
    public JsonElement toJson() {
        JsonArray array = new JsonArray();

        for (T value : get()) {
            Identifier id = registry.getId(value);
            if (id != null) array.add(id.toString());
        }

        return array;
    }

    @Override
    public void fromJson(JsonElement element) {
        List<T> parsed = new ArrayList<>();

        for (JsonElement entry : element.getAsJsonArray()) {
            Identifier id = Identifier.tryParse(entry.getAsString());
            if (id == null) continue;

            T value = registry.get(id);
            if (value != null) parsed.add(value);
        }

        set(parsed);
    }

    public abstract static class Builder<B extends Builder<B, T, S>, T, S extends RegistryListSetting<T>>
        extends Setting.Builder<B, List<T>, S> {

        public Builder() {
            defaultValue = new ArrayList<>();
        }

        @SafeVarargs
        public final B defaultValue(T... values) {
            this.defaultValue = new ArrayList<>(List.of(values));
            return self();
        }
    }
}
