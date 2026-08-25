package dev.lepton.systems.modules.movement;

import dev.lepton.event.EventHandler;
import dev.lepton.event.events.TickEvent;
import dev.lepton.settings.BoolSetting;
import dev.lepton.settings.DoubleSetting;
import dev.lepton.settings.EnumSetting;
import dev.lepton.systems.modules.Categories;
import dev.lepton.systems.modules.Module;
import dev.lepton.utils.player.PlayerUtils;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class Speed extends Module {
    public enum Mode {
        /** Constant horizontal velocity while moving. */
        Strafe,
        /** Hops along the ground, building speed like a bunnyhop. */
        Bhop
    }

    private final EnumSetting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode")
        .description("How the extra speed is applied.")
        .defaultValue(Mode.Strafe)
        .build());

    private final DoubleSetting speed = sgGeneral.add(new DoubleSetting.Builder()
        .name("speed")
        .description("Blocks per tick while moving.")
        .defaultValue(0.35)
        .range(0.1, 2.0)
        .sliderRange(0.1, 1.0)
        .build());

    private final BoolSetting onlyOnGround = sgGeneral.add(new BoolSetting.Builder()
        .name("only-on-ground")
        .description("Do not apply speed while airborne.")
        .defaultValue(false)
        .visible(() -> mode.get() == Mode.Strafe)
        .build());

    private final BoolSetting inLiquids = sgGeneral.add(new BoolSetting.Builder()
        .name("in-liquids")
        .description("Keep applying speed while swimming.")
        .defaultValue(true)
        .build());

    public Speed() {
        super(Categories.Movement, "Speed", "Moves you faster than vanilla allows.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null) return;
        if (!PlayerUtils.isMoving()) return;
        if (!inLiquids.get() && PlayerUtils.isInLiquid()) return;

        if (mode.get() == Mode.Bhop) {
            applyBhop();
            return;
        }

        if (onlyOnGround.get() && !mc.player.isOnGround()) return;

        applyStrafe(speed.get());
    }

    private void applyBhop() {
        if (mc.player.isOnGround()) {
            // Hop, then carry speed through the air.
            Vec3d velocity = mc.player.getVelocity();
            mc.player.setVelocity(velocity.x, 0.42, velocity.z);
        }

        applyStrafe(speed.get());
    }

    private void applyStrafe(double target) {
        float yaw = PlayerUtils.movementYaw() * MathHelper.RADIANS_PER_DEGREE;

        double vx = -MathHelper.sin(yaw) * target;
        double vz = MathHelper.cos(yaw) * target;

        Vec3d velocity = mc.player.getVelocity();
        mc.player.setVelocity(vx, velocity.y, vz);
    }

    @Override
    public String getInfoString() {
        return mode.get().name();
    }
}
