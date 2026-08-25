package dev.lepton.systems.modules.movement;

import dev.lepton.event.EventHandler;
import dev.lepton.event.events.TickEvent;
import dev.lepton.settings.BoolSetting;
import dev.lepton.settings.DoubleSetting;
import dev.lepton.systems.modules.Categories;
import dev.lepton.systems.modules.Module;
import dev.lepton.utils.player.PlayerUtils;
import net.minecraft.util.math.Vec3d;

public class Spider extends Module {
    private final DoubleSetting climbSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("climb-speed")
        .description("How fast you climb a wall.")
        .defaultValue(0.2)
        .range(0.05, 1.0)
        .sliderRange(0.05, 0.6)
        .build());

    private final BoolSetting requireMovement = sgGeneral.add(new BoolSetting.Builder()
        .name("require-movement")
        .description("Only climb while pressing a movement key.")
        .defaultValue(true)
        .build());

    public Spider() {
        super(Categories.Movement, "Spider", "Climb any wall like a spider.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null) return;
        if (!mc.player.horizontalCollision) return;
        if (requireMovement.get() && !PlayerUtils.isMoving()) return;

        Vec3d velocity = mc.player.getVelocity();
        mc.player.setVelocity(velocity.x, climbSpeed.get(), velocity.z);
        mc.player.fallDistance = 0;
    }
}
