package dev.lepton.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.function.Consumer;

public class IntSetting extends Setting<Integer> {
    public final int min, max;
    public final int sliderMin, sliderMax;
    public final boolean noSlider;

    public IntSetting(String name, String description, Integer defaultValue, Consumer<Integer> onChanged, IVisible visible,
                      int min, int max, int sliderMin, int sliderMax, boolean noSlider) {
        super(name, description, defaultValue, onChanged, visible);

        this.min = min;
        this.max = max;
        this.sliderMin = sliderMin;
        this.sliderMax = sliderMax;
        this.noSlider = noSlider;

        // Re-run now that the bounds actually exist; the super constructor clamped against zeroes.
        resetImpl();
    }

    @Override
    protected Integer transformValue(Integer value) {
        if (value == null) return min;
        return Math.max(min, Math.min(max, value));
    }

    @Override
    public boolean parse(String input) {
        try {
            return set(Integer.parseInt(input.trim()));
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(get());
    }

    @Override
    public void fromJson(JsonElement element) {
        set(element.getAsInt());
    }

    public static class Builder extends Setting.Builder<Builder, Integer, IntSetting> {
        private int min = Integer.MIN_VALUE, max = Integer.MAX_VALUE;
        private int sliderMin = 0, sliderMax = 10;
        private boolean noSlider;

        public Builder() {
            defaultValue = 0;
        }

        public Builder min(int min) {
            this.min = min;
            return this;
        }

        public Builder max(int max) {
            this.max = max;
            return this;
        }

        public Builder range(int min, int max) {
            this.min = min;
            this.max = max;
            return this;
        }

        public Builder sliderRange(int min, int max) {
            this.sliderMin = min;
            this.sliderMax = max;
            return this;
        }

        public Builder sliderMax(int max) {
            this.sliderMax = max;
            return this;
        }

        public Builder noSlider() {
            this.noSlider = true;
            return this;
        }

        @Override
        public IntSetting build() {
            return new IntSetting(name, description, defaultValue, onChanged, visible, min, max, sliderMin, sliderMax, noSlider);
        }
    }
}
