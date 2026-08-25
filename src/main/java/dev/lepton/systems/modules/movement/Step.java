package dev.lepton.systems.modules.movement;

import dev.lepton.event.EventHandler;
import dev.lepton.event.events.TickEvent;
import dev.lepton.settings.BoolSetting;
import dev.lepton.settings.DoubleSetting;
import dev.lepton.systems.modules.Categories;
import dev.lepton.systems.modules.Module;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;

public class Step extends Module {
    private static final double VANILLA_STEP_HEIGHT = 0.6;

    private final DoubleSetting height = sgGeneral.add(new DoubleSetting.Builder()
        .name("height")
        .description("How tall a ledge you can walk straight up.")
        .defaultValue(1.0)
        .range(0.6, 10.0)
        .sliderRange(0.6, 4.0)
        .build());

    private final BoolSetting onlyOnGround = sgGeneral.add(new BoolSetting.Builder()
        .name("only-on-ground")
        .description("Do not step while airborne.")
        .defaultValue(true)
        .build());

    public Step() {
        super(Categories.Movement, "Step", "Walk up full blocks without jumping.");
    }

    @Override
    public void onDeactivate() {
        restore();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null) return;

        EntityAttributeInstance attribute = mc.player.getAttributeInstance(EntityAttributes.STEP_HEIGHT);
        if (attribute == null) return;

        if (onlyOnGround.get() && !mc.player.isOnGround()) {
            attribute.setBaseValue(VANILLA_STEP_HEIGHT);
            return;
        }

        attribute.setBaseValue(height.get());
    }

    private void restore() {
        if (mc.player == null) return;

        EntityAttributeInstance attribute = mc.player.getAttributeInstance(EntityAttributes.STEP_HEIGHT);
        if (attribute != null) attribute.setBaseValue(VANILLA_STEP_HEIGHT);
    }

    @Override
    public String getInfoString() {
        return String.format("%.1f", height.get());
    }
}
