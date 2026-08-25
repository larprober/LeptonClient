package dev.lepton.systems.modules.render;

import dev.lepton.event.EventHandler;
import dev.lepton.event.events.Render3DEvent;
import dev.lepton.settings.BoolSetting;
import dev.lepton.settings.ColorSetting;
import dev.lepton.settings.DoubleSetting;
import dev.lepton.settings.EnumSetting;
import dev.lepton.settings.SettingGroup;
import dev.lepton.systems.modules.Categories;
import dev.lepton.systems.modules.Module;
import dev.lepton.utils.render.Renderer3D;
import dev.lepton.utils.render.ShapeMode;
import dev.lepton.utils.render.color.Color;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.util.math.Box;

public class StorageESP extends Module {
    private final SettingGroup sgColors = settings.createGroup("Colours");

    private final EnumSetting<ShapeMode> shapeMode = sgGeneral.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("Outline, filled sides, or both.")
        .defaultValue(ShapeMode.Both)
        .build());

    private final DoubleSetting range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("Only draw containers within this many blocks.")
        .defaultValue(64.0)
        .range(8.0, 256.0)
        .sliderRange(16.0, 128.0)
        .build());

    private final DoubleSetting fillOpacity = sgGeneral.add(new DoubleSetting.Builder()
        .name("fill-opacity")
        .description("Alpha of the filled sides.")
        .defaultValue(0.25)
        .range(0.0, 1.0)
        .sliderRange(0.0, 1.0)
        .visible(() -> shapeMode.get().sides())
        .build());

    private final BoolSetting chests = sgGeneral.add(new BoolSetting.Builder()
        .name("chests").description("Chests and trapped chests.").defaultValue(true).build());

    private final BoolSetting barrels = sgGeneral.add(new BoolSetting.Builder()
        .name("barrels").description("Barrels.").defaultValue(true).build());

    private final BoolSetting shulkers = sgGeneral.add(new BoolSetting.Builder()
        .name("shulkers").description("Shulker boxes.").defaultValue(true).build());

    private final BoolSetting enderChests = sgGeneral.add(new BoolSetting.Builder()
        .name("ender-chests").description("Ender chests.").defaultValue(true).build());

    private final BoolSetting furnaces = sgGeneral.add(new BoolSetting.Builder()
        .name("furnaces").description("Furnaces, smokers and blast furnaces.").defaultValue(false).build());

    private final BoolSetting hoppers = sgGeneral.add(new BoolSetting.Builder()
        .name("hoppers").description("Hoppers.").defaultValue(false).build());

    private final ColorSetting chestColor = sgColors.add(new ColorSetting.Builder()
        .name("chest-colour").defaultValue(0xF5, 0xB9, 0x42, 255).build());

    private final ColorSetting barrelColor = sgColors.add(new ColorSetting.Builder()
        .name("barrel-colour").defaultValue(0xC8, 0x8B, 0x4A, 255).build());

    private final ColorSetting shulkerColor = sgColors.add(new ColorSetting.Builder()
        .name("shulker-colour").defaultValue(0xA8, 0x92, 0xFF, 255).build());

    private final ColorSetting enderChestColor = sgColors.add(new ColorSetting.Builder()
        .name("ender-chest-colour").defaultValue(0x17, 0xC9, 0x8D, 255).build());

    private final ColorSetting furnaceColor = sgColors.add(new ColorSetting.Builder()
        .name("furnace-colour").defaultValue(0x9A, 0xA7, 0xB8, 255).build());

    private final ColorSetting hopperColor = sgColors.add(new ColorSetting.Builder()
        .name("hopper-colour").defaultValue(0x8F, 0xA0, 0xB8, 255).build());

    public StorageESP() {
        super(Categories.Render, "StorageESP", "Highlights containers through walls.");
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (mc.world == null || mc.player == null) return;

        Renderer3D renderer = Renderer3D.of(event);
        double maxRangeSq = range.get() * range.get();
        boolean drewAnything = false;

        for (BlockEntity blockEntity : mc.world.getWorldChunk(mc.player.getBlockPos()).getBlockEntities().values()) {
            if (blockEntity.getPos().getSquaredDistance(mc.player.getEntityPos()) > maxRangeSq) continue;

            Color color = colorFor(blockEntity);
            if (color == null) continue;

            Box box = new Box(blockEntity.getPos());
            renderer.box(box, color, color.withAlpha(fillOpacity.get()), shapeMode.get());
            drewAnything = true;
        }

        if (drewAnything) renderer.flush();
    }

    private Color colorFor(BlockEntity blockEntity) {
        if (blockEntity instanceof ShulkerBoxBlockEntity) return shulkers.get() ? shulkerColor.get().resolve() : null;
        if (blockEntity instanceof EnderChestBlockEntity) return enderChests.get() ? enderChestColor.get().resolve() : null;
        if (blockEntity instanceof ChestBlockEntity) return chests.get() ? chestColor.get().resolve() : null;
        if (blockEntity instanceof BarrelBlockEntity) return barrels.get() ? barrelColor.get().resolve() : null;
        if (blockEntity instanceof FurnaceBlockEntity) return furnaces.get() ? furnaceColor.get().resolve() : null;
        if (blockEntity instanceof HopperBlockEntity) return hoppers.get() ? hopperColor.get().resolve() : null;

        return null;
    }
}
