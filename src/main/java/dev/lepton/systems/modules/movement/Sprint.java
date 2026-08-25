package dev.lepton.systems.modules.movement;

import dev.lepton.event.EventHandler;
import dev.lepton.event.events.TickEvent;
import dev.lepton.settings.BoolSetting;
import dev.lepton.systems.modules.Categories;
import dev.lepton.systems.modules.Module;
import dev.lepton.utils.player.PlayerUtils;

public class Sprint extends Module {
    private final BoolSetting keepSprint = sgGeneral.add(new BoolSetting.Builder()
        .name("keep-sprint")
        .description("Stay sprinting after attacking instead of dropping out of it.")
        .defaultValue(true)
        .build());

    private final BoolSetting omnidirectional = sgGeneral.add(new BoolSetting.Builder()
        .name("omnidirectional")
        .description("Sprint sideways and backwards too, not just forwards.")
        .defaultValue(false)
        .build());

    private final BoolSetting whenHungry = sgGeneral.add(new BoolSetting.Builder()
        .name("when-hungry")
        .description("Keep sprinting even below the vanilla hunger threshold.")
        .defaultValue(false)
        .build());

    public Sprint() {
        super(Categories.Movement, "Sprint", "Sprints automatically whenever you move.");
    }

    @Override
    public void onDeactivate() {
        if (mc.player != null) mc.player.setSprinting(false);
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null) return;

        boolean moving = omnidirectional.get()
            ? PlayerUtils.isMoving()
            : mc.player.input.playerInput.forward();

        if (!moving) return;
        if (mc.player.isSneaking()) return;
        if (!whenHungry.get() && mc.player.getHungerManager().getFoodLevel() <= 6) return;
        if (mc.player.horizontalCollision) return;

        mc.player.setSprinting(true);

        if (keepSprint.get()) PlayerUtils.setInput(null, null, null, null, null, null, true);
    }
}
