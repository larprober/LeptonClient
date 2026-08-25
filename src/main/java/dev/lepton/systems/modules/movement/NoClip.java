package dev.lepton.systems.modules.movement;

import dev.lepton.event.EventHandler;
import dev.lepton.event.events.TickEvent;
import dev.lepton.settings.BoolSetting;
import dev.lepton.settings.DoubleSetting;
import dev.lepton.systems.modules.Categories;
import dev.lepton.systems.modules.Module;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * Walks through terrain by disabling the player's collision.
 *
 * <p>This only behaves in singleplayer, where the integrated server accepts the client's
 * position without a strict movement check. It is one of the clearest examples of why
 * Lepton is singleplayer-only.
 */
public class NoClip extends Module {
    private final DoubleSetting speed = sgGeneral.add(new DoubleSetting.Builder()
        .name("speed")
        .description("Movement speed while clipping.")
        .defaultValue(0.3)
        .range(0.05, 2.0)
        .sliderRange(0.05, 1.0)
        .build());

    private final BoolSetting noGravity = sgGeneral.add(new BoolSetting.Builder()
        .name("no-gravity")
        .description("Hover instead of sinking through the floor.")
        .defaultValue(true)
        .build());

    public NoClip() {
        super(Categories.Movement, "NoClip", "Move through blocks.");
    }

    @Override
    public void onDeactivate() {
        if (mc.player == null) return;

        mc.player.noClip = false;
        mc.player.setNoGravity(false);

        // Leaving noclip inside a wall would suffocate; zero the velocity so the
        // player is at least not driven deeper in.
        mc.player.setVelocity(Vec3d.ZERO);
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null) return;

        mc.player.noClip = true;
        mc.player.setNoGravity(noGravity.get());
        mc.player.fallDistance = 0;

        PlayerInput input = mc.player.input.playerInput;

        double forward = (input.forward() ? 1 : 0) - (input.backward() ? 1 : 0);
        double strafe = (input.right() ? 1 : 0) - (input.left() ? 1 : 0);

        double vertical = 0;
        if (input.jump()) vertical += speed.get();
        if (input.sneak()) vertical -= speed.get();

        if (forward == 0 && strafe == 0) {
            mc.player.setVelocity(0, vertical, 0);
            return;
        }

        double length = Math.sqrt(forward * forward + strafe * strafe);
        forward /= length;
        strafe /= length;

        float yaw = mc.player.getYaw() * MathHelper.RADIANS_PER_DEGREE;
        double sin = MathHelper.sin(yaw);
        double cos = MathHelper.cos(yaw);

        mc.player.setVelocity(
            (forward * -sin + strafe * cos) * speed.get(),
            vertical,
            (forward * cos + strafe * sin) * speed.get()
        );
    }
}
