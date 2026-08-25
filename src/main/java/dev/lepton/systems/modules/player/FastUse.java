package dev.lepton.systems.modules.player;

import dev.lepton.event.EventHandler;
import dev.lepton.event.events.TickEvent;
import dev.lepton.settings.IntSetting;
import dev.lepton.systems.modules.Categories;
import dev.lepton.systems.modules.Module;
import net.minecraft.util.Hand;

/**
 * Removes the vanilla four-tick gap between right-click uses.
 *
 * <p>Rather than editing the cooldown field, this simply issues extra use calls per tick,
 * which is enough in singleplayer because the integrated server processes them in order.
 */
public class FastUse extends Module {
    private final IntSetting usesPerTick = sgGeneral.add(new IntSetting.Builder()
        .name("uses-per-tick")
        .description("How many extra use actions to fire each tick.")
        .defaultValue(4)
        .range(1, 20)
        .sliderRange(1, 10)
        .build());

    public FastUse() {
        super(Categories.Player, "FastUse", "Use items with no cooldown.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (!mc.options.useKey.isPressed()) return;
        if (mc.player.getMainHandStack().isEmpty()) return;

        for (int i = 0; i < usesPerTick.get(); i++) {
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        }
    }

    @Override
    public String getInfoString() {
        return String.valueOf(usesPerTick.get());
    }
}
