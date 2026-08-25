package dev.lepton.systems.modules.render;

import dev.lepton.settings.BlockListSetting;
import dev.lepton.settings.BoolSetting;
import dev.lepton.systems.modules.Categories;
import dev.lepton.systems.modules.Module;
import dev.lepton.systems.modules.Modules;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

/**
 * Hides every block except a chosen set.
 *
 * <p>Works by answering "should this face be drawn?" with false for anything not on the
 * list -- see {@code BlockMixin}. Toggling forces a chunk rebuild so the change is visible
 * immediately rather than when chunks happen to reload.
 */
public class Xray extends Module {
    private final BlockListSetting blocks = sgGeneral.add(new BlockListSetting.Builder()
        .name("blocks")
        .description("Blocks that stay visible.")
        .defaultValue(
            Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE,
            Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE,
            Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE,
            Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE,
            Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE,
            Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE,
            Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE,
            Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE,
            Blocks.NETHER_GOLD_ORE, Blocks.NETHER_QUARTZ_ORE,
            Blocks.ANCIENT_DEBRIS,
            Blocks.SPAWNER, Blocks.TRIAL_SPAWNER, Blocks.VAULT,
            Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.BARREL,
            Blocks.LAVA, Blocks.WATER
        )
        .build());

    private final BoolSetting fullbright = sgGeneral.add(new BoolSetting.Builder()
        .name("fullbright")
        .description("Turn Fullbright on while Xray is active.")
        .defaultValue(true)
        .build());

    private boolean enabledFullbright;

    public Xray() {
        super(Categories.Render, "Xray", "See through terrain to the blocks that matter.");
    }

    @Override
    public void onActivate() {
        reloadChunks();

        if (!fullbright.get()) return;

        Fullbright fb = Modules.get().get(Fullbright.class);

        if (fb != null && !fb.isActive()) {
            fb.setActive(true);
            enabledFullbright = true;
        }
    }

    @Override
    public void onDeactivate() {
        reloadChunks();

        if (!enabledFullbright) return;

        Fullbright fb = Modules.get().get(Fullbright.class);
        if (fb != null && fb.isActive()) fb.setActive(false);

        enabledFullbright = false;
    }

    /** Called from the block mixin for every face the chunk builder considers. */
    public boolean isVisible(BlockState state) {
        return blocks.get().contains(state.getBlock());
    }

    public boolean contains(Block block) {
        return blocks.get().contains(block);
    }

    private void reloadChunks() {
        if (mc.worldRenderer != null) mc.worldRenderer.reload();
    }

    @Override
    public String getInfoString() {
        return blocks.get().size() + " blocks";
    }
}
