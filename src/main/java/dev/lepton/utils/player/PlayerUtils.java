package dev.lepton.utils.player;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class PlayerUtils {
    private static MinecraftClient mc() {
        return MinecraftClient.getInstance();
    }

    public static ClientPlayerEntity player() {
        return mc().player;
    }

    public static Vec3d pos() {
        ClientPlayerEntity player = player();
        return player == null ? Vec3d.ZERO : player.getEntityPos();
    }

    public static double distanceTo(Entity entity) {
        ClientPlayerEntity player = player();
        return player == null || entity == null ? Double.MAX_VALUE : player.distanceTo(entity);
    }

    public static double distanceTo(Vec3d target) {
        return pos().distanceTo(target);
    }

    /** Horizontal speed in blocks per tick. */
    public static double horizontalSpeed() {
        ClientPlayerEntity player = player();
        if (player == null) return 0;

        Vec3d velocity = player.getVelocity();
        return Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
    }

    public static boolean isMoving() {
        ClientPlayerEntity player = player();
        if (player == null) return false;

        PlayerInput input = player.input.playerInput;
        return input.forward() || input.backward() || input.left() || input.right();
    }

    public static boolean isMovingHorizontally() {
        return horizontalSpeed() > 1.0e-4;
    }

    /**
     * The yaw the player is actually travelling along, accounting for strafe.
     * Returns the look yaw when there is no movement input.
     */
    public static float movementYaw() {
        ClientPlayerEntity player = player();
        if (player == null) return 0;

        PlayerInput input = player.input.playerInput;

        float forward = (input.forward() ? 1 : 0) - (input.backward() ? 1 : 0);
        float strafe = (input.left() ? 1 : 0) - (input.right() ? 1 : 0);

        if (forward == 0 && strafe == 0) return player.getYaw();

        float yaw = player.getYaw();

        // Rotate the look yaw by the input direction's angle.
        float offset = (float) Math.toDegrees(Math.atan2(strafe, forward));
        return yaw - offset;
    }

    /** {yaw, pitch} that would point the player at {@code target}. */
    public static float[] rotationTo(Vec3d target) {
        ClientPlayerEntity player = player();
        if (player == null) return new float[] { 0, 0 };

        Vec3d eyes = player.getEyePos();

        double dx = target.x - eyes.x;
        double dy = target.y - eyes.y;
        double dz = target.z - eyes.z;

        double horizontal = Math.sqrt(dx * dx + dz * dz);

        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, horizontal));

        return new float[] { MathHelper.wrapDegrees(yaw), MathHelper.clamp(pitch, -90f, 90f) };
    }

    /** Snaps the player's head to face a point. */
    public static void lookAt(Vec3d target) {
        ClientPlayerEntity player = player();
        if (player == null) return;

        float[] rotation = rotationTo(target);
        player.setYaw(rotation[0]);
        player.setPitch(rotation[1]);
    }

    public static boolean isInLiquid() {
        ClientPlayerEntity player = player();
        return player != null && (player.isTouchingWater() || player.isInLava());
    }

    public static boolean isOnGround() {
        ClientPlayerEntity player = player();
        return player != null && player.isOnGround();
    }

    /** Replaces the player's current input record, preserving fields not overridden. */
    public static void setInput(Boolean forward, Boolean backward, Boolean left, Boolean right,
                                Boolean jump, Boolean sneak, Boolean sprint) {
        ClientPlayerEntity player = player();
        if (player == null) return;

        PlayerInput current = player.input.playerInput;

        player.input.playerInput = new PlayerInput(
            forward != null ? forward : current.forward(),
            backward != null ? backward : current.backward(),
            left != null ? left : current.left(),
            right != null ? right : current.right(),
            jump != null ? jump : current.jump(),
            sneak != null ? sneak : current.sneak(),
            sprint != null ? sprint : current.sprint()
        );
    }
}
