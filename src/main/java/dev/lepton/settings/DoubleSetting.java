package dev.lepton.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.function.Consumer;

public class DoubleSetting extends Setting<Double> {
    public final double min, max;
    public final double sliderMin, sliderMax;
    public final boolean noSlider;
    public final int decimalPlaces;

    public DoubleSetting(String name, String description, Double defaultValue, Consumer<Double> onChanged, IVisible visible,
                         double min, double max, double sliderMin, double sliderMax, boolean noSlider, int decimalPlaces) {
        super(name, description, defaultValue, onChanged, visible);

        this.min = min;
        this.max = max;
        this.sliderMin = sliderMin;
        this.sliderMax = sliderMax;
        this.noSlider = noSlider;
        this.decimalPlaces = decimalPlaces;

        // Re-run now that the bounds actually exist; the super constructor clamped against zeroes.
        resetImpl();
    }

    @Override
    protected Double transformValue(Double value) {
        if (value == null) return min;
        return Math.max(min, Math.min(max, value));
    }

    @Override
    public boolean parse(String input) {
        try {
            return set(Double.parseDouble(input.trim()));
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
        set(element.getAsDouble());
    }

    public static class Builder extends Setting.Builder<Builder, Double, DoubleSetting> {
        private double min = -Double.MAX_VALUE, max = Double.MAX_VALUE;
        private double sliderMin = 0, sliderMax = 10;
        private boolean noSlider;
        private int decimalPlaces = 2;

        public Builder() {
            defaultValue = 0.0;
        }

        public Builder min(double min) {
            this.min = min;
            return this;
        }

        public Builder max(double max) {
            this.max = max;
            return this;
        }

        public Builder range(double min, double max) {
            this.min = min;
            this.max = max;
            return this;
        }

        public Builder sliderRange(double min, double max) {
            this.sliderMin = min;
            this.sliderMax = max;
            return this;
        }

        public Builder sliderMax(double max) {
            this.sliderMax = max;
            return this;
        }

        public Builder noSlider() {
            this.noSlider = true;
            return this;
        }

        public Builder decimalPlaces(int decimalPlaces) {
            this.decimalPlaces = decimalPlaces;
            return this;
        }

        @Override
        public DoubleSetting build() {
            return new DoubleSetting(name, description, defaultValue, onChanged, visible, min, max, sliderMin, sliderMax, noSlider, decimalPlaces);
        }
    }
}
