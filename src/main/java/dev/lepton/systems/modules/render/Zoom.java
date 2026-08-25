package dev.lepton.systems.modules.render;

import dev.lepton.event.EventHandler;
import dev.lepton.event.events.MouseScrollEvent;
import dev.lepton.event.events.TickEvent;
import dev.lepton.settings.BoolSetting;
import dev.lepton.settings.DoubleSetting;
import dev.lepton.systems.modules.Categories;
import dev.lepton.systems.modules.Module;

public class Zoom extends Module {
    private final DoubleSetting factor = sgGeneral.add(new DoubleSetting.Builder()
        .name("factor")
        .description("How far to zoom in. Higher is closer.")
        .defaultValue(4.0)
        .range(1.1, 50.0)
        .sliderRange(1.5, 20.0)
        .build());

    private final BoolSetting scrollToAdjust = sgGeneral.add(new BoolSetting.Builder()
        .name("scroll-to-adjust")
        .description("Change the zoom factor with the scroll wheel while zoomed.")
        .defaultValue(true)
        .build());

    private Integer previousFov;

    public Zoom() {
        super(Categories.Render, "Zoom", "Narrows your field of view like a spyglass.");
        chatFeedback = false;
    }

    @Override
    public void onActivate() {
        if (previousFov == null) previousFov = mc.options.getFov().getValue();
    }

    @Override
    public void onDeactivate() {
        if (previousFov != null) {
            mc.options.getFov().setValue(previousFov);
            previousFov = null;
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (previousFov == null) return;

        int zoomed = (int) Math.max(1, Math.round(previousFov / factor.get()));
        mc.options.getFov().setValue(zoomed);
    }

    @EventHandler
    private void onScroll(MouseScrollEvent event) {
        if (!scrollToAdjust.get()) return;

        factor.set(factor.get() + event.value * 0.5);
        event.cancel();
    }

    @Override
    public String getInfoString() {
        return String.format("%.1fx", factor.get());
    }
}
