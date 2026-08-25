package dev.lepton.systems.modules.world;

import dev.lepton.event.EventHandler;
import dev.lepton.event.events.Render3DEvent;
import dev.lepton.event.events.TickEvent;
import dev.lepton.settings.BlockListSetting;
import dev.lepton.settings.BoolSetting;
import dev.lepton.settings.ColorSetting;
import dev.lepton.settings.DoubleSetting;
import dev.lepton.settings.EnumSetting;
import dev.lepton.settings.IntSetting;
import dev.lepton.systems.modules.Categories;
import dev.lepton.systems.modules.Module;
import dev.lepton.utils.render.Renderer3D;
import dev.lepton.utils.render.ShapeMode;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Nuker extends Module {
    public enum Filter {
        /** Break anything breakable. */
        All,
        /** Break only blocks on the list. */
        Whitelist,
        /** Break everything except blocks on the list. */
        Blacklist
    }

    private final DoubleSetting radius = sgGeneral.add(new DoubleSetting.Builder()
        .name("radius")
        .description("How far around you to break blocks.")
        .defaultValue(4.0)
        .range(1.0, 6.0)
        .sliderRange(1.0, 6.0)
        .build());

    private final IntSetting blocksPerTick = sgGeneral.add(new IntSetting.Builder()
        .name("blocks-per-tick")
        .description("How many blocks to break each tick.")
        .defaultValue(1)
        .range(1, 64)
        .sliderRange(1, 16)
        .build());

    private final EnumSetting<Filter> filter = sgGeneral.add(new EnumSetting.Builder<Filter>()
        .name("filter")
        .description("How the block list is applied.")
        .defaultValue(Filter.All)
        .build());

    private final BlockListSetting blocks = sgGeneral.add(new BlockListSetting.Builder()
        .name("blocks")
        .description("Blocks the filter applies to.")
        .visible(() -> filter.get() != Filter.All)
        .build());

    private final BoolSetting render = sgGeneral.add(new BoolSetting.Builder()
        .name("render")
        .description("Outline the block being broken.")
        .defaultValue(true)
        .build());

    private final ColorSetting lineColor = sgGeneral.add(new ColorSetting.Builder()
        .name("line-colour")
        .defaultValue(0x2E, 0x7D, 0xF6, 255)
        .visible(render::get)
        .build());

    private final ColorSetting sideColor = sgGeneral.add(new ColorSetting.Builder()
        .name("side-colour")
        .defaultValue(0x2E, 0x7D, 0xF6, 60)
        .visible(render::get)
        .build());

    private final List<BlockPos> breaking = new ArrayList<>();

    public Nuker() {
        super(Categories.World, "Nuker", "Breaks every block around you.");
    }

    @Override
    public void onDeactivate() {
        breaking.clear();
        if (mc.interactionManager != null) mc.interactionManager.cancelBlockBreaking();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        breaking.clear();

        List<BlockPos> candidates = findCandidates();
        int budget = blocksPerTick.get();

        for (BlockPos pos : candidates) {
            if (budget-- <= 0) break;

            mc.interactionManager.updateBlockBreakingProgress(pos, Direction.UP);
            mc.player.swingHand(net.minecraft.util.Hand.MAIN_HAND);
            breaking.add(pos);
        }
    }

    private List<BlockPos> findCandidates() {
        List<BlockPos> found = new ArrayList<>();

        Vec3d eyes = mc.player.getEyePos();
        int r = (int) Math.ceil(radius.get());
        BlockPos origin = mc.player.getBlockPos();
        double radiusSq = radius.get() * radius.get();

        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos pos = origin.add(x, y, z);

                    if (eyes.squaredDistanceTo(Vec3d.ofCenter(pos)) > radiusSq) continue;

                    BlockState state = mc.world.getBlockState(pos);
                    if (state.isAir()) continue;
                    if (state.getHardness(mc.world, pos) < 0) continue;
                    if (!passesFilter(state)) continue;

                    found.add(pos);
                }
            }
        }

        found.sort(Comparator.comparingDouble(pos -> eyes.squaredDistanceTo(Vec3d.ofCenter(pos))));
        return found;
    }

    private boolean passesFilter(BlockState state) {
        return switch (filter.get()) {
            case All -> true;
            case Whitelist -> blocks.get().contains(state.getBlock());
            case Blacklist -> !blocks.get().contains(state.getBlock());
        };
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (!render.get() || breaking.isEmpty()) return;

        Renderer3D renderer = Renderer3D.of(event);

        for (BlockPos pos : breaking) {
            renderer.box(new Box(pos), lineColor.get().resolve(), sideColor.get().resolve(), ShapeMode.Both);
        }

        renderer.flush();
    }

    @Override
    public String getInfoString() {
        return String.format("%.0f", radius.get());
    }
}
