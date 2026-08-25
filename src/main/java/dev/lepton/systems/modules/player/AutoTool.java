package dev.lepton.systems.modules.player;

import dev.lepton.event.EventHandler;
import dev.lepton.event.events.TickEvent;
import dev.lepton.settings.BoolSetting;
import dev.lepton.settings.IntSetting;
import dev.lepton.systems.modules.Categories;
import dev.lepton.systems.modules.Module;
import dev.lepton.utils.player.InvUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

public class AutoTool extends Module {
    private final BoolSetting switchBack = sgGeneral.add(new BoolSetting.Builder()
        .name("switch-back")
        .description("Return to your previous slot when you stop mining.")
        .defaultValue(true)
        .build());

    private final IntSetting durabilityFloor = sgGeneral.add(new IntSetting.Builder()
        .name("durability-floor")
        .description("Never pick a tool with less than this much durability left.")
        .defaultValue(10)
        .range(0, 100)
        .sliderRange(0, 50)
        .build());

    private final BoolSetting antiBreak = sgGeneral.add(new BoolSetting.Builder()
        .name("anti-break")
        .description("Swap off a tool that is about to break rather than finishing the block.")
        .defaultValue(true)
        .build());

    private int previousSlot = -1;

    public AutoTool() {
        super(Categories.Player, "AutoTool", "Picks the fastest tool for whatever you are mining.");
    }

    @Override
    public void onDeactivate() {
        restore();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;

        if (!mc.options.attackKey.isPressed() || mc.crosshairTarget == null
            || mc.crosshairTarget.getType() != HitResult.Type.BLOCK) {
            restore();
            return;
        }

        BlockHitResult hit = (BlockHitResult) mc.crosshairTarget;
        BlockState state = mc.world.getBlockState(hit.getBlockPos());

        if (state.isAir()) {
            restore();
            return;
        }

        int best = findBestTool(state);
        if (best == -1) return;

        if (previousSlot == -1) previousSlot = InvUtils.selectedSlot();
        InvUtils.select(best);
    }

    private int findBestTool(BlockState state) {
        int bestSlot = -1;
        double bestScore = -1;

        for (int slot = 0; slot < PlayerInventory.getHotbarSize(); slot++) {
            ItemStack stack = mc.player.getInventory().getStack(slot);
            if (stack.isEmpty()) continue;

            if (antiBreak.get() && stack.isDamageable()) {
                int remaining = stack.getMaxDamage() - stack.getDamage();
                if (remaining <= durabilityFloor.get()) continue;
            }

            double score = stack.getMiningSpeedMultiplier(state);
            if (score <= bestScore) continue;

            bestScore = score;
            bestSlot = slot;
        }

        // A score of 1 means nothing in the hotbar is actually a tool for this block.
        return bestScore > 1 ? bestSlot : -1;
    }

    private void restore() {
        if (previousSlot == -1) return;

        if (switchBack.get()) InvUtils.select(previousSlot);
        previousSlot = -1;
    }
}
