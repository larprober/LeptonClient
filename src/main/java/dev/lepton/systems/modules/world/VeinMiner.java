package dev.lepton.systems.modules.world;

import dev.lepton.event.EventHandler;
import dev.lepton.event.events.TickEvent;
import dev.lepton.settings.BoolSetting;
import dev.lepton.settings.IntSetting;
import dev.lepton.systems.modules.Categories;
import dev.lepton.systems.modules.Module;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Breaking one block of a vein breaks the whole connected group of the same block. */
public class VeinMiner extends Module {
    private final IntSetting maxBlocks = sgGeneral.add(new IntSetting.Builder()
        .name("max-blocks")
        .description("Largest vein to follow.")
        .defaultValue(64)
        .range(1, 512)
        .sliderRange(8, 256)
        .build());

    private final IntSetting blocksPerTick = sgGeneral.add(new IntSetting.Builder()
        .name("blocks-per-tick")
        .description("How fast to work through the queue.")
        .defaultValue(1)
        .range(1, 32)
        .sliderRange(1, 8)
        .build());

    private final BoolSetting diagonals = sgGeneral.add(new BoolSetting.Builder()
        .name("diagonals")
        .description("Follow the vein through diagonal contact, not just faces.")
        .defaultValue(true)
        .build());

    private final List<BlockPos> queue = new ArrayList<>();
    private Block targetBlock;

    public VeinMiner() {
        super(Categories.World, "VeinMiner", "Break one ore, break the whole vein.");
    }

    @Override
    public void onDeactivate() {
        queue.clear();
        targetBlock = null;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        if (queue.isEmpty()) {
            if (!mc.options.attackKey.isPressed()) return;
            if (mc.crosshairTarget == null || mc.crosshairTarget.getType() != HitResult.Type.BLOCK) return;

            BlockPos origin = ((BlockHitResult) mc.crosshairTarget).getBlockPos();
            BlockState state = mc.world.getBlockState(origin);
            if (state.isAir()) return;

            targetBlock = state.getBlock();
            collectVein(origin);
        }

        int budget = blocksPerTick.get();

        while (budget-- > 0 && !queue.isEmpty()) {
            BlockPos pos = queue.get(0);

            if (mc.world.getBlockState(pos).getBlock() != targetBlock) {
                queue.remove(0);
                continue;
            }

            mc.interactionManager.updateBlockBreakingProgress(pos, Direction.UP);
            mc.player.swingHand(Hand.MAIN_HAND);

            if (mc.world.getBlockState(pos).isAir()) queue.remove(0);
            break;
        }
    }

    /** Flood fill outward from the first block, following only blocks of the same type. */
    private void collectVein(BlockPos origin) {
        Set<BlockPos> seen = new HashSet<>();
        Deque<BlockPos> pending = new ArrayDeque<>();

        pending.add(origin);
        seen.add(origin);

        while (!pending.isEmpty() && queue.size() < maxBlocks.get()) {
            BlockPos current = pending.poll();
            queue.add(current);

            int spread = diagonals.get() ? 1 : 0;

            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && y == 0 && z == 0) continue;

                        // Without diagonals, only face-adjacent neighbours count.
                        if (spread == 0 && Math.abs(x) + Math.abs(y) + Math.abs(z) != 1) continue;

                        BlockPos neighbour = current.add(x, y, z);
                        if (!seen.add(neighbour)) continue;
                        if (mc.world.getBlockState(neighbour).getBlock() != targetBlock) continue;

                        pending.add(neighbour);
                    }
                }
            }
        }
    }

    @Override
    public String getInfoString() {
        return queue.isEmpty() ? null : String.valueOf(queue.size());
    }
}
