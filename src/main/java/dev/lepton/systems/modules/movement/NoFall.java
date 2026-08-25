package dev.lepton.systems.modules.movement;

import dev.lepton.SinglePlayerGate;
import dev.lepton.event.EventHandler;
import dev.lepton.event.events.TickEvent;
import dev.lepton.settings.DoubleSetting;
import dev.lepton.settings.EnumSetting;
import dev.lepton.systems.modules.Categories;
import dev.lepton.systems.modules.Module;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

/**
 * Stops fall damage.
 *
 * <p>Zeroing the client's {@code fallDistance} does nothing on its own: in singleplayer the
 * integrated server owns the player's fall distance and applies the damage. But the
 * integrated server runs inside this same JVM, so a mixin on
 * {@link PlayerEntity#handleFallDamage} reaches the server-side player object too. That
 * is how {@link Mode#Cancel} works, and it is why this cannot function on a real server:
 * there the damage is computed on a machine this mod is not running on.
 *
 * <p>The mixin reads {@link #armed} and {@link #localPlayerId}, which are volatile and
 * written only from the client tick. The mixin itself may run on the integrated server
 * thread, so it must never touch MinecraftClient state directly.
 */
public class NoFall extends Module {
    public enum Mode {
        /** Cancels the damage outright. Reliable. */
        Cancel,
        /** Arrests downward velocity just above the ground. Leaves damage logic alone. */
        Catch
    }

    private static volatile boolean armed;
    private static volatile UUID localPlayerId;

    private final EnumSetting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode")
        .description("How falling damage is avoided.")
        .defaultValue(Mode.Cancel)
        .build());

    private final DoubleSetting catchHeight = sgGeneral.add(new DoubleSetting.Builder()
        .name("catch-height")
        .description("Blocks above the ground at which to arrest the fall.")
        .defaultValue(2.0)
        .range(0.5, 10.0)
        .sliderRange(0.5, 6.0)
        .visible(() -> mode.get() == Mode.Catch)
        .build());

    public NoFall() {
        super(Categories.Movement, "NoFall", "Stops you taking fall damage.");
    }

    @Override
    public void onDeactivate() {
        armed = false;
    }

    /**
     * Called from {@code PlayerEntityMixin}, possibly on the integrated server thread.
     * Deliberately reads only volatile fields.
     */
    public static boolean shouldCancelFallDamage(PlayerEntity player) {
        if (!armed) return false;

        UUID id = localPlayerId;
        if (id == null) return false;

        // The server-side player is a different object from mc.player, so match on identity
        // by UUID rather than by reference.
        return id.equals(player.getUuid());
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) {
            armed = false;
            return;
        }

        localPlayerId = mc.player.getUuid();
        armed = mode.get() == Mode.Cancel && SinglePlayerGate.isAllowed();

        // Keeps the client's own prediction quiet so there is no damage tilt or particles.
        mc.player.fallDistance = 0;

        if (mode.get() != Mode.Catch) return;

        if (mc.player.getVelocity().y >= 0) return;

        Vec3d pos = mc.player.getEntityPos();

        for (double offset = 0.1; offset <= catchHeight.get(); offset += 0.25) {
            BlockPos below = BlockPos.ofFloored(pos.x, pos.y - offset, pos.z);

            if (!mc.world.getBlockState(below).isAir()) {
                Vec3d velocity = mc.player.getVelocity();
                mc.player.setVelocity(velocity.x, 0, velocity.z);
                return;
            }
        }
    }

    @Override
    public String getInfoString() {
        return mode.get().name();
    }
}
