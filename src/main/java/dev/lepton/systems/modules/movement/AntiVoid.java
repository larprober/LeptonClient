package dev.lepton.systems.modules.movement;

import dev.lepton.event.EventHandler;
import dev.lepton.event.events.TickEvent;
import dev.lepton.settings.DoubleSetting;
import dev.lepton.settings.IntSetting;
import dev.lepton.systems.modules.Categories;
import dev.lepton.systems.modules.Module;
import dev.lepton.utils.misc.ChatUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class AntiVoid extends Module {
    private final IntSetting triggerHeight = sgGeneral.add(new IntSetting.Builder()
        .name("trigger-height")
        .description("Y level below which the void catch engages.")
        .defaultValue(-40)
        .range(-128, 64)
        .sliderRange(-128, 0)
        .build());

    private final DoubleSetting scanDepth = sgGeneral.add(new DoubleSetting.Builder()
        .name("scan-depth")
        .description("How far below you to look for solid ground before deciding it is void.")
        .defaultValue(12.0)
        .range(2.0, 64.0)
        .sliderRange(2.0, 32.0)
        .build());

    private boolean caught;

    public AntiVoid() {
        super(Categories.Movement, "AntiVoid", "Stops you falling into the void.");
    }

    @Override
    public void onActivate() {
        caught = false;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;

        Vec3d pos = mc.player.getEntityPos();

        if (pos.y > triggerHeight.get() || mc.player.getVelocity().y >= 0) {
            caught = false;
            return;
        }

        if (hasGroundBelow(pos)) {
            caught = false;
            return;
        }

        Vec3d velocity = mc.player.getVelocity();
        mc.player.setVelocity(velocity.x, 0, velocity.z);
        mc.player.setNoGravity(true);
        mc.player.fallDistance = 0;

        if (!caught) {
            caught = true;
            ChatUtils.warning("Void catch engaged at Y {}.", (int) pos.y);
        }
    }

    @Override
    public void onDeactivate() {
        if (mc.player != null) mc.player.setNoGravity(false);
        caught = false;
    }

    private boolean hasGroundBelow(Vec3d pos) {
        for (double offset = 1; offset <= scanDepth.get(); offset += 1) {
            BlockPos below = BlockPos.ofFloored(pos.x, pos.y - offset, pos.z);
            if (below.getY() < mc.world.getBottomY()) break;
            if (!mc.world.getBlockState(below).isAir()) return true;
        }

        return false;
    }
}
