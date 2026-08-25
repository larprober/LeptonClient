package dev.lepton.systems.hud.elements;

import dev.lepton.gui.theme.Theme;
import dev.lepton.settings.BoolSetting;
import dev.lepton.systems.hud.HudElement;
import dev.lepton.utils.render.Renderer2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

/** Coordinates, FPS, and other at-a-glance readouts in one stacked block. */
public class InfoPanel extends HudElement {
    private final BoolSetting coords = sgGeneral.add(new BoolSetting.Builder()
        .name("coordinates").description("Your XYZ position.").defaultValue(true).build());

    private final BoolSetting netherCoords = sgGeneral.add(new BoolSetting.Builder()
        .name("nether-coordinates").description("The matching coordinates in the other dimension.").defaultValue(true).build());

    private final BoolSetting fps = sgGeneral.add(new BoolSetting.Builder()
        .name("fps").description("Frames per second.").defaultValue(true).build());

    private final BoolSetting biome = sgGeneral.add(new BoolSetting.Builder()
        .name("biome").description("Current biome.").defaultValue(false).build());

    private final BoolSetting facing = sgGeneral.add(new BoolSetting.Builder()
        .name("facing").description("Which way you are looking.").defaultValue(true).build());

    public InfoPanel() {
        super("InfoPanel", Anchor.BottomLeft, 4, 4);
    }

    private List<String[]> lines() {
        List<String[]> lines = new ArrayList<>();
        MinecraftClient mc = MinecraftClient.getInstance();

        if (mc.player == null) return lines;

        BlockPos pos = mc.player.getBlockPos();

        if (coords.get()) {
            lines.add(new String[] { "XYZ", pos.getX() + " " + pos.getY() + " " + pos.getZ() });
        }

        if (netherCoords.get()) {
            boolean inNether = mc.world != null
                && mc.world.getRegistryKey() == net.minecraft.world.World.NETHER;

            double factor = inNether ? 8.0 : 0.125;
            String label = inNether ? "Overworld" : "Nether";

            lines.add(new String[] {
                label,
                (int) (pos.getX() * factor) + " " + pos.getY() + " " + (int) (pos.getZ() * factor)
            });
        }

        if (facing.get()) {
            lines.add(new String[] { "Facing", mc.player.getHorizontalFacing().asString() });
        }

        if (fps.get()) {
            lines.add(new String[] { "FPS", String.valueOf(mc.getCurrentFps()) });
        }

        if (biome.get() && mc.world != null) {
            var entry = mc.world.getBiome(mc.player.getBlockPos());
            String name = entry.getKey().map(key -> key.getValue().getPath()).orElse("unknown");
            lines.add(new String[] { "Biome", name });
        }

        return lines;
    }

    @Override
    public double width() {
        double widest = 0;

        for (String[] line : lines()) {
            widest = Math.max(widest, Renderer2D.textWidth(line[0] + " " + line[1]));
        }

        return widest + 4;
    }

    @Override
    public double height() {
        return Math.max(1, lines().size()) * (Renderer2D.textHeight() + 2);
    }

    @Override
    public void render(Renderer2D r, Theme theme, double x, double y) {
        List<String[]> lines = lines();
        if (lines.isEmpty()) return;

        double rowHeight = Renderer2D.textHeight() + 2;

        for (int i = 0; i < lines.size(); i++) {
            String[] line = lines.get(i);
            double rowY = y + i * rowHeight;

            r.text(line[0], x, rowY, theme.textDim);
            r.text(line[1], x + Renderer2D.textWidth(line[0] + " "), rowY, theme.text);
        }
    }
}
