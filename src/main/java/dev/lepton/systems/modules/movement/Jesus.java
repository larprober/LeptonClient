package dev.lepton.systems.modules.movement;

import dev.lepton.event.EventHandler;
import dev.lepton.event.events.TickEvent;
import dev.lepton.settings.BoolSetting;
import dev.lepton.systems.modules.Categories;
import dev.lepton.systems.modules.Module;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class Jesus extends Module {
    private final BoolSetting lava = sgGeneral.add(new BoolSetting.Builder()
        .name("lava")
        .description("Walk on lava as well as water.")
        .defaultValue(true)
        .build());

    private final BoolSetting sinkOnSneak = sgGeneral.add(new BoolSetting.Builder()
        .name("sink-on-sneak")
        .description("Hold sneak to drop through the surface.")
        .defaultValue(true)
        .build());

    public Jesus() {
        super(Categories.Movement, "Jesus", "Walk on the surface of water and lava.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;
        if (sinkOnSneak.get() && mc.player.isSneaking()) return;

        // Only hold the player up when the fluid is directly beneath them; standing
        // deep inside the fluid should still let them swim out normally.
        Vec3d pos = mc.player.getEntityPos();
        BlockPos below = BlockPos.ofFloored(pos.x, pos.y - 0.1, pos.z);
        BlockPos feet = BlockPos.ofFloored(pos.x, pos.y + 0.2, pos.z);

        boolean fluidBelow = isSupportingFluid(below);
        boolean submerged = isSupportingFluid(feet);

        if (!fluidBelow || submerged) return;

        Vec3d velocity = mc.player.getVelocity();

        if (velocity.y < 0) {
            mc.player.setVelocity(velocity.x, 0, velocity.z);
            mc.player.setOnGround(true);
            mc.player.fallDistance = 0;
        }
    }

    private boolean isSupportingFluid(BlockPos pos) {
        var state = mc.world.getFluidState(pos);
        if (state.isEmpty()) return false;

        return lava.get() || state.isIn(net.minecraft.registry.tag.FluidTags.WATER);
    }
}
