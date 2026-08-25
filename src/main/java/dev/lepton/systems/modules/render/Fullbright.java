package dev.lepton.systems.modules.render;

import dev.lepton.event.EventHandler;
import dev.lepton.event.events.TickEvent;
import dev.lepton.settings.DoubleSetting;
import dev.lepton.systems.modules.Categories;
import dev.lepton.systems.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.SimpleOption;

/**
 * Raises the effective gamma well past the vanilla ceiling.
 *
 * <p>Writing the value is not an option: {@code SimpleOption} validates on set and rejects
 * anything above 1.0, logging "Illegal option value" and keeping the old value. Instead
 * {@code SimpleOptionMixin} intercepts the <em>read</em> of the brightness option and
 * substitutes our value, leaving the stored setting untouched -- so nothing needs
 * restoring when the module is switched off, and the user's own brightness slider is
 * never overwritten.
 */
public class Fullbright extends Module {
    private static volatile boolean active;
    private static volatile double level = 15.0;

    private final DoubleSetting brightness = sgGeneral.add(new DoubleSetting.Builder()
        .name("brightness")
        .description("Effective gamma. Vanilla caps out at 1.")
        .defaultValue(15.0)
        .range(1.0, 30.0)
        .sliderRange(1.0, 20.0)
        .onChanged(v -> level = v)
        .build());

    public Fullbright() {
        super(Categories.Render, "Fullbright", "Lights the world up as if you had night vision.");
    }

    @Override
    public void onActivate() {
        level = brightness.get();
        active = true;
    }

    @Override
    public void onDeactivate() {
        active = false;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        level = brightness.get();
        active = true;
    }

    /**
     * Called from the mixin on every option read, so it must stay cheap. Returns null
     * for "leave this option alone".
     */
    public static Double brightnessOverride(Object option) {
        if (!active) return null;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.options == null) return null;

        SimpleOption<Double> gamma = mc.options.getGamma();
        return option == gamma ? level : null;
    }

    @Override
    public String getInfoString() {
        return String.format("%.0f", brightness.get());
    }
}
