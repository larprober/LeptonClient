package dev.lepton.systems.modules.movement;

import dev.lepton.event.EventHandler;
import dev.lepton.event.events.TickEvent;
import dev.lepton.settings.DoubleSetting;
import dev.lepton.systems.modules.Categories;
import dev.lepton.systems.modules.Module;
import net.minecraft.util.math.Vec3d;

public class HighJump extends Module {
    private final DoubleSetting multiplier = sgGeneral.add(new DoubleSetting.Builder()
        .name("multiplier")
        .description("How much higher than a vanilla jump.")
        .defaultValue(2.0)
        .range(1.0, 10.0)
        .sliderRange(1.0, 6.0)
        .build());

    private boolean jumpedLastTick;

    public HighJump() {
        super(Categories.Movement, "HighJump", "Jump much higher than normal.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null) return;

        boolean jumping = mc.player.input.playerInput.jump();

        // Boost only on the frame the jump actually leaves the ground, otherwise the
        // multiplier compounds every tick and launches you into orbit.
        if (jumping && !jumpedLastTick && mc.player.isOnGround()) {
            Vec3d velocity = mc.player.getVelocity();
            mc.player.setVelocity(velocity.x, 0.42 * multiplier.get(), velocity.z);
        }

        jumpedLastTick = jumping;
    }

    @Override
    public String getInfoString() {
        return String.format("%.1fx", multiplier.get());
    }
}
