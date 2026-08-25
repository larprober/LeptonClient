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
import dev.lepton.utils.entity.EntityUtils;
import dev.lepton.utils.render.Renderer3D;
import dev.lepton.utils.render.ShapeMode;
import dev.lepton.utils.render.color.Color;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;

public class ESP extends Module {
    private final SettingGroup sgColors = settings.createGroup("Colours");

    private final EnumSetting<ShapeMode> shapeMode = sgGeneral.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("Outline, filled sides, or both.")
        .defaultValue(ShapeMode.Both)
        .build());

    private final DoubleSetting range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("Only draw entities within this many blocks.")
        .defaultValue(96.0)
        .range(8.0, 512.0)
        .sliderRange(16.0, 256.0)
        .build());

    private final DoubleSetting fillOpacity = sgGeneral.add(new DoubleSetting.Builder()
        .name("fill-opacity")
        .description("Alpha of the filled sides, as a fraction of the line colour.")
        .defaultValue(0.25)
        .range(0.0, 1.0)
        .sliderRange(0.0, 1.0)
        .visible(() -> shapeMode.get().sides())
        .build());

    private final BoolSetting hostiles = sgGeneral.add(new BoolSetting.Builder()
        .name("hostiles").description("Draw hostile mobs.").defaultValue(true).build());

    private final BoolSetting passives = sgGeneral.add(new BoolSetting.Builder()
        .name("passives").description("Draw passive mobs.").defaultValue(true).build());

    private final BoolSetting players = sgGeneral.add(new BoolSetting.Builder()
        .name("players").description("Draw players.").defaultValue(true).build());

    private final BoolSetting items = sgGeneral.add(new BoolSetting.Builder()
        .name("items").description("Draw dropped items.").defaultValue(false).build());

    private final BoolSetting projectiles = sgGeneral.add(new BoolSetting.Builder()
        .name("projectiles").description("Draw arrows and thrown items.").defaultValue(false).build());

    private final ColorSetting hostileColor = sgColors.add(new ColorSetting.Builder()
        .name("hostile-colour").defaultValue(0xFF, 0x4D, 0x6D, 255).build());

    private final ColorSetting passiveColor = sgColors.add(new ColorSetting.Builder()
        .name("passive-colour").defaultValue(0x3D, 0xD6, 0x8C, 255).build());

    private final ColorSetting playerColor = sgColors.add(new ColorSetting.Builder()
        .name("player-colour").defaultValue(0x5F, 0xB0, 0xFF, 255).build());

    private final ColorSetting itemColor = sgColors.add(new ColorSetting.Builder()
        .name("item-colour").defaultValue(0xF5, 0xB9, 0x42, 255).build());

    private final ColorSetting projectileColor = sgColors.add(new ColorSetting.Builder()
        .name("projectile-colour").defaultValue(0xA8, 0x92, 0xFF, 255).build());

    public ESP() {
        super(Categories.Render, "ESP", "Draws boxes around entities through walls.");
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (mc.world == null || mc.player == null) return;

        Renderer3D renderer = Renderer3D.of(event);
        double maxRange = range.get();
        boolean drewAnything = false;

        for (Entity entity : mc.world.getEntities()) {
            if (EntityUtils.isSelf(entity)) continue;
            if (mc.player.distanceTo(entity) > maxRange) continue;

            Color color = colorFor(entity);
            if (color == null) continue;

            Box box = EntityUtils.interpolatedBox(entity, event.tickDelta);
            Color fill = color.withAlpha(fillOpacity.get());

            renderer.box(box, color, fill, shapeMode.get());
            drewAnything = true;
        }

        if (drewAnything) renderer.flush();
    }

    /** Null means "do not draw this entity". */
    private Color colorFor(Entity entity) {
        return switch (EntityUtils.groupOf(entity)) {
            case Hostile -> hostiles.get() ? hostileColor.get().resolve() : null;
            case Passive -> passives.get() ? passiveColor.get().resolve() : null;
            case Player -> players.get() ? playerColor.get().resolve() : null;
            case Item -> items.get() ? itemColor.get().resolve() : null;
            case Projectile -> projectiles.get() ? projectileColor.get().resolve() : null;
            case Other -> null;
        };
    }
}
