package dev.lepton.systems.modules.movement;

import dev.lepton.event.EventHandler;
import dev.lepton.event.events.TickEvent;
import dev.lepton.settings.BoolSetting;
import dev.lepton.settings.DoubleSetting;
import dev.lepton.settings.EnumSetting;
import dev.lepton.systems.modules.Categories;
import dev.lepton.systems.modules.Module;
import dev.lepton.utils.player.PlayerUtils;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class Flight extends Module {
    public enum Mode {
        /** Toggles the vanilla creative-flight ability. Smoothest, and behaves like the real thing. */
        Abilities,
        /** Drives velocity directly. Works even when abilities are locked. */
        Velocity
    }

    private final EnumSetting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode")
        .description("How flight is applied.")
        .defaultValue(Mode.Abilities)
        .build());

    private final DoubleSetting speed = sgGeneral.add(new DoubleSetting.Builder()
        .name("speed")
        .description("Horizontal flight speed.")
        .defaultValue(1.0)
        .range(0.05, 10.0)
        .sliderRange(0.05, 5.0)
        .build());

    private final DoubleSetting verticalSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("vertical-speed")
        .description("Speed when holding jump or sneak.")
        .defaultValue(1.0)
        .range(0.05, 10.0)
        .sliderRange(0.05, 5.0)
        .visible(() -> true)
        .build());

    private final BoolSetting antiKick = sgGeneral.add(new BoolSetting.Builder()
        .name("smooth-descent")
        .description("Drift down very slightly instead of hovering perfectly still.")
        .defaultValue(false)
        .build());

    private float previousFlySpeed;
    private boolean previousAllowFlying;
    private boolean previousFlying;

    public Flight() {
        super(Categories.Movement, "Flight", "Lets you fly around your world.");
    }

    @Override
    public void onActivate() {
        if (mc.player == null) return;

        previousFlySpeed = mc.player.getAbilities().getFlySpeed();
        previousAllowFlying = mc.player.getAbilities().allowFlying;
        previousFlying = mc.player.getAbilities().flying;
    }

    @Override
    public void onDeactivate() {
        if (mc.player == null) return;

        mc.player.getAbilities().setFlySpeed(previousFlySpeed);
        mc.player.getAbilities().allowFlying = previousAllowFlying;
        mc.player.getAbilities().flying = previousFlying && previousAllowFlying;

        // Zero out any leftover upward momentum so releasing flight does not launch you.
        if (mode.get() == Mode.Velocity) {
            Vec3d velocity = mc.player.getVelocity();
            mc.player.setVelocity(velocity.x, 0, velocity.z);
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null) return;

        if (mode.get() == Mode.Abilities) {
            mc.player.getAbilities().allowFlying = true;
            mc.player.getAbilities().flying = true;
            mc.player.getAbilities().setFlySpeed((float) (speed.get() * 0.05));

            if (antiKick.get() && !PlayerUtils.isMoving()) {
                Vec3d velocity = mc.player.getVelocity();
                mc.player.setVelocity(velocity.x, -0.04, velocity.z);
            }

            return;
        }

        applyVelocityFlight();
    }

    private void applyVelocityFlight() {
        PlayerInput input = mc.player.input.playerInput;

        double horizontal = speed.get() * 0.5;
        double forward = (input.forward() ? 1 : 0) - (input.backward() ? 1 : 0);
        double strafe = (input.right() ? 1 : 0) - (input.left() ? 1 : 0);

        double vertical = 0;
        if (input.jump()) vertical += verticalSpeed.get() * 0.5;
        if (input.sneak()) vertical -= verticalSpeed.get() * 0.5;

        if (forward == 0 && strafe == 0) {
            Vec3d velocity = mc.player.getVelocity();
            mc.player.setVelocity(0, vertical, 0);
            mc.player.setVelocity(mc.player.getVelocity().multiply(1, 1, 1));
            if (vertical == 0) mc.player.setVelocity(0, antiKick.get() ? -0.04 : 0, 0);
            return;
        }

        // Normalise so diagonal movement is not faster than straight.
        double length = Math.sqrt(forward * forward + strafe * strafe);
        forward /= length;
        strafe /= length;

        float yaw = mc.player.getYaw() * MathHelper.RADIANS_PER_DEGREE;
        double sin = MathHelper.sin(yaw);
        double cos = MathHelper.cos(yaw);

        double vx = (forward * -sin + strafe * cos) * horizontal;
        double vz = (forward * cos + strafe * sin) * horizontal;

        mc.player.setVelocity(vx, vertical, vz);
    }

    @Override
    public String getInfoString() {
        return mode.get().name();
    }
}
