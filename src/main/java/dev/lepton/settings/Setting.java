package dev.lepton.settings;

import com.google.gson.JsonElement;

import java.util.function.Consumer;

public abstract class Setting<T> {
    public final String name;
    public final String title;
    public final String description;
    public final T defaultValue;

    protected T value;

    private final Consumer<T> onChanged;
    private final IVisible visible;

    public SettingGroup group;

    public Setting(String name, String description, T defaultValue, Consumer<T> onChanged, IVisible visible) {
        this.name = name;
        this.title = titleify(name);
        this.description = description;
        this.defaultValue = defaultValue;
        this.onChanged = onChanged;
        this.visible = visible;

        resetImpl();
    }

    public T get() {
        return value;
    }

    public boolean set(T newValue) {
        if (!isValueValid(newValue)) return false;

        value = transformValue(newValue);
        onChanged();
        return true;
    }

    public void reset() {
        resetImpl();
        onChanged();
    }

    protected void resetImpl() {
        value = transformValue(defaultValue);
    }

    protected void onChanged() {
        if (onChanged != null) onChanged.accept(value);
    }

    public boolean isVisible() {
        return visible == null || visible.isVisible();
    }

    protected boolean isValueValid(T value) {
        return true;
    }

    protected T transformValue(T value) {
        return value;
    }

    /** Parses a value from a command argument. Returns false if the input was not understood. */
    public abstract boolean parse(String input);

    public abstract JsonElement toJson();

    public abstract void fromJson(JsonElement element);

    /** "renderMode" -> "Render Mode" */
    private static String titleify(String name) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);

            if (i == 0) {
                sb.append(Character.toUpperCase(c));
            } else if (Character.isUpperCase(c) && !Character.isUpperCase(name.charAt(i - 1))) {
                sb.append(' ').append(c);
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    public abstract static class Builder<B extends Builder<B, V, S>, V, S extends Setting<V>> {
        protected String name = "undefined";
        protected String description = "";
        protected V defaultValue;
        protected Consumer<V> onChanged;
        protected IVisible visible;

        @SuppressWarnings("unchecked")
        protected B self() {
            return (B) this;
        }

        public B name(String name) {
            this.name = name;
            return self();
        }

        public B description(String description) {
            this.description = description;
            return self();
        }

        public B defaultValue(V defaultValue) {
            this.defaultValue = defaultValue;
            return self();
        }

        public B onChanged(Consumer<V> onChanged) {
            this.onChanged = onChanged;
            return self();
        }

        public B visible(IVisible visible) {
            this.visible = visible;
            return self();
        }

        public abstract S build();
    }
}
