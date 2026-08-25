package dev.lepton.systems.modules.player;

import dev.lepton.event.EventHandler;
import dev.lepton.event.events.TickEvent;
import dev.lepton.settings.IntSetting;
import dev.lepton.systems.modules.Categories;
import dev.lepton.systems.modules.Module;
import net.minecraft.client.gui.screen.DeathScreen;

public class AutoRespawn extends Module {
    private final IntSetting delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("Ticks to wait on the death screen before respawning.")
        .defaultValue(5)
        .range(0, 100)
        .sliderRange(0, 40)
        .build());

    private int ticksWaiting;

    public AutoRespawn() {
        super(Categories.Player, "AutoRespawn", "Clicks respawn for you when you die.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null) return;

        if (!(mc.currentScreen instanceof DeathScreen)) {
            ticksWaiting = 0;
            return;
        }

        if (ticksWaiting++ < delay.get()) return;

        ticksWaiting = 0;
        mc.player.requestRespawn();
        mc.setScreen(null);
    }
}
