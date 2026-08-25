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
import dev.lepton.utils.render.color.Color;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class Tracers extends Module {
    public enum Target {
        Feet,
        Center,
        Head
    }

    private final SettingGroup sgColors = settings.createGroup("Colours");

    private final EnumSetting<Target> target = sgGeneral.add(new EnumSetting.Builder<Target>()
        .name("aim-point")
        .description("Which part of the entity the line points at.")
        .defaultValue(Target.Center)
        .build());

    private final DoubleSetting range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("Only trace entities within this many blocks.")
        .defaultValue(96.0)
        .range(8.0, 512.0)
        .sliderRange(16.0, 256.0)
        .build());

    private final BoolSetting hostiles = sgGeneral.add(new BoolSetting.Builder()
        .name("hostiles").description("Trace hostile mobs.").defaultValue(true).build());

    private final BoolSetting passives = sgGeneral.add(new BoolSetting.Builder()
        .name("passives").description("Trace passive mobs.").defaultValue(false).build());

    private final BoolSetting players = sgGeneral.add(new BoolSetting.Builder()
        .name("players").description("Trace players.").defaultValue(true).build());

    private final BoolSetting items = sgGeneral.add(new BoolSetting.Builder()
        .name("items").description("Trace dropped items.").defaultValue(false).build());

    private final ColorSetting hostileColor = sgColors.add(new ColorSetting.Builder()
        .name("hostile-colour").defaultValue(0xFF, 0x4D, 0x6D, 200).build());

    private final ColorSetting passiveColor = sgColors.add(new ColorSetting.Builder()
        .name("passive-colour").defaultValue(0x3D, 0xD6, 0x8C, 200).build());

    private final ColorSetting playerColor = sgColors.add(new ColorSetting.Builder()
        .name("player-colour").defaultValue(0x5F, 0xB0, 0xFF, 200).build());

    private final ColorSetting itemColor = sgColors.add(new ColorSetting.Builder()
        .name("item-colour").defaultValue(0xF5, 0xB9, 0x42, 200).build());

    public Tracers() {
        super(Categories.Render, "Tracers", "Draws a line from you to nearby entities.");
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

            renderer.tracer(aimPoint(entity, event.tickDelta), color);
            drewAnything = true;
        }

        if (drewAnything) renderer.flush();
    }

    private Vec3d aimPoint(Entity entity, float tickDelta) {
        Box box = EntityUtils.interpolatedBox(entity, tickDelta);

        return switch (target.get()) {
            case Feet -> new Vec3d(box.getCenter().x, box.minY, box.getCenter().z);
            case Head -> new Vec3d(box.getCenter().x, box.maxY, box.getCenter().z);
            case Center -> box.getCenter();
        };
    }

    private Color colorFor(Entity entity) {
        return switch (EntityUtils.groupOf(entity)) {
            case Hostile -> hostiles.get() ? hostileColor.get().resolve() : null;
            case Passive -> passives.get() ? passiveColor.get().resolve() : null;
            case Player -> players.get() ? playerColor.get().resolve() : null;
            case Item -> items.get() ? itemColor.get().resolve() : null;
            default -> null;
        };
    }
}
