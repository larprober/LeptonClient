package dev.lepton.systems.modules.movement;

import dev.lepton.event.EventHandler;
import dev.lepton.event.events.TickEvent;
import dev.lepton.settings.BoolSetting;
import dev.lepton.settings.EnumSetting;
import dev.lepton.systems.modules.Categories;
import dev.lepton.systems.modules.Module;
import dev.lepton.utils.player.PlayerUtils;

public class AutoWalk extends Module {
    public enum Direction {
        Forward,
        Backward,
        Left,
        Right
    }

    private final EnumSetting<Direction> direction = sgGeneral.add(new EnumSetting.Builder<Direction>()
        .name("direction")
        .description("Which way to walk.")
        .defaultValue(Direction.Forward)
        .build());

    private final BoolSetting pauseInScreens = sgGeneral.add(new BoolSetting.Builder()
        .name("pause-in-screens")
        .description("Stop walking while a GUI is open.")
        .defaultValue(true)
        .build());

    public AutoWalk() {
        super(Categories.Movement, "AutoWalk", "Holds a movement key for you.");
    }

    @Override
    public void onDeactivate() {
        PlayerUtils.setInput(false, false, false, false, null, null, null);
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null) return;
        if (pauseInScreens.get() && mc.currentScreen != null) return;

        switch (direction.get()) {
            case Forward -> PlayerUtils.setInput(true, false, null, null, null, null, null);
            case Backward -> PlayerUtils.setInput(false, true, null, null, null, null, null);
            case Left -> PlayerUtils.setInput(null, null, true, false, null, null, null);
            case Right -> PlayerUtils.setInput(null, null, false, true, null, null, null);
        }
    }

    @Override
    public String getInfoString() {
        return direction.get().name();
    }
}
