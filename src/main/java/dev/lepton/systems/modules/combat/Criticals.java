package dev.lepton.systems.modules.combat;

import dev.lepton.event.EventHandler;
import dev.lepton.event.events.TickEvent;
import dev.lepton.settings.EnumSetting;
import dev.lepton.systems.modules.Categories;
import dev.lepton.systems.modules.Module;
import net.minecraft.util.math.Vec3d;

/**
 * Nudges the player upward the instant before a swing so the hit counts as a critical.
 */
public class Criticals extends Module {
    public enum Mode {
        /** A tiny hop, exactly enough to register the crit. */
        Hop,
        /** A downward twitch while already airborne. */
        Packet
    }

    private final EnumSetting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode")
        .description("How the critical condition is produced.")
        .defaultValue(Mode.Hop)
        .build());

    public Criticals() {
        super(Categories.Combat, "Criticals", "Makes your attacks land as critical hits.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null) return;
        if (!mc.options.attackKey.isPressed()) return;
        if (mc.player.getAttackCooldownProgress(0) < 1.0f) return;

        if (mode.get() == Mode.Hop) {
            if (!mc.player.isOnGround()) return;
            if (mc.player.isTouchingWater() || mc.player.isInLava()) return;

            Vec3d velocity = mc.player.getVelocity();
            mc.player.setVelocity(velocity.x, 0.1, velocity.z);
            return;
        }

        if (mc.player.isOnGround()) return;

        Vec3d velocity = mc.player.getVelocity();
        mc.player.setVelocity(velocity.x, Math.min(velocity.y, -0.05), velocity.z);
    }

    @Override
    public String getInfoString() {
        return mode.get().name();
    }
}
