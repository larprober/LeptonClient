package dev.lepton.systems.modules.player;

import dev.lepton.event.EventHandler;
import dev.lepton.event.events.TickEvent;
import dev.lepton.settings.BoolSetting;
import dev.lepton.settings.IntSetting;
import dev.lepton.systems.modules.Categories;
import dev.lepton.systems.modules.Module;
import dev.lepton.utils.player.PlayerUtils;

import java.util.Random;

public class AntiAFK extends Module {
    private final BoolSetting spin = sgGeneral.add(new BoolSetting.Builder()
        .name("spin")
        .description("Slowly rotate on the spot.")
        .defaultValue(true)
        .build());

    private final BoolSetting jump = sgGeneral.add(new BoolSetting.Builder()
        .name("jump")
        .description("Jump occasionally.")
        .defaultValue(true)
        .build());

    private final BoolSetting walk = sgGeneral.add(new BoolSetting.Builder()
        .name("walk")
        .description("Take a few steps in a random direction now and then.")
        .defaultValue(false)
        .build());

    private final IntSetting interval = sgGeneral.add(new IntSetting.Builder()
        .name("interval")
        .description("Ticks between actions.")
        .defaultValue(80)
        .range(10, 600)
        .sliderRange(20, 300)
        .build());

    private final Random random = new Random();
    private int timer;
    private int walkTicksLeft;

    public AntiAFK() {
        super(Categories.Player, "AntiAFK", "Keeps you moving so you never idle out.");
    }

    @Override
    public void onDeactivate() {
        walkTicksLeft = 0;
        PlayerUtils.setInput(false, false, false, false, null, null, null);
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null) return;

        if (spin.get()) mc.player.setYaw(mc.player.getYaw() + 2f);

        if (walkTicksLeft > 0) {
            walkTicksLeft--;
            PlayerUtils.setInput(true, false, false, false, null, null, null);
            if (walkTicksLeft == 0) PlayerUtils.setInput(false, false, false, false, null, null, null);
        }

        if (timer++ < interval.get()) return;
        timer = 0;

        if (jump.get() && mc.player.isOnGround()) mc.player.jump();

        if (walk.get()) {
            mc.player.setYaw(random.nextFloat() * 360f);
            walkTicksLeft = 20 + random.nextInt(20);
        }
    }
}
