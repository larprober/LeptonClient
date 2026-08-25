package dev.lepton.systems.modules.movement;

import dev.lepton.event.EventHandler;
import dev.lepton.event.events.TickEvent;
import dev.lepton.settings.BoolSetting;
import dev.lepton.systems.modules.Categories;
import dev.lepton.systems.modules.Module;
import dev.lepton.utils.player.PlayerUtils;

public class AutoJump extends Module {
    private final BoolSetting onlyWhenMoving = sgGeneral.add(new BoolSetting.Builder()
        .name("only-when-moving")
        .description("Only jump while a movement key is held.")
        .defaultValue(true)
        .build());

    private final BoolSetting inLiquids = sgGeneral.add(new BoolSetting.Builder()
        .name("in-liquids")
        .description("Keep jumping while in water or lava.")
        .defaultValue(false)
        .build());

    public AutoJump() {
        super(Categories.Movement, "AutoJump", "Jumps continuously.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null) return;
        if (!mc.player.isOnGround()) return;
        if (onlyWhenMoving.get() && !PlayerUtils.isMoving()) return;
        if (!inLiquids.get() && PlayerUtils.isInLiquid()) return;

        mc.player.jump();
    }
}
