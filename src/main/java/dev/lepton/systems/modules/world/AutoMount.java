package dev.lepton.systems.modules.world;

import dev.lepton.event.EventHandler;
import dev.lepton.event.events.TickEvent;
import dev.lepton.settings.BoolSetting;
import dev.lepton.settings.DoubleSetting;
import dev.lepton.systems.modules.Categories;
import dev.lepton.systems.modules.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.util.Hand;

public class AutoMount extends Module {
    private final DoubleSetting range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("How far away to look for something to ride.")
        .defaultValue(4.0)
        .range(1.0, 6.0)
        .sliderRange(1.0, 6.0)
        .build());

    private final BoolSetting horses = sgGeneral.add(new BoolSetting.Builder()
        .name("horses").description("Mount horses, donkeys and llamas.").defaultValue(true).build());

    private final BoolSetting boats = sgGeneral.add(new BoolSetting.Builder()
        .name("boats").description("Board boats.").defaultValue(true).build());

    private final BoolSetting minecarts = sgGeneral.add(new BoolSetting.Builder()
        .name("minecarts").description("Board minecarts.").defaultValue(true).build());

    public AutoMount() {
        super(Categories.World, "AutoMount", "Automatically rides the nearest vehicle.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.player.hasVehicle()) return;

        Entity best = null;
        double bestDistance = range.get();

        for (Entity entity : mc.world.getEntities()) {
            if (!isRideable(entity)) continue;
            if (entity.hasPassengers()) continue;

            double distance = mc.player.distanceTo(entity);
            if (distance > bestDistance) continue;

            best = entity;
            bestDistance = distance;
        }

        if (best == null) return;

        mc.interactionManager.interactEntity(mc.player, best, Hand.MAIN_HAND);
    }

    private boolean isRideable(Entity entity) {
        if (entity instanceof AbstractHorseEntity) return horses.get();
        if (entity instanceof AbstractBoatEntity) return boats.get();
        if (entity instanceof AbstractMinecartEntity) return minecarts.get();

        return false;
    }
}
