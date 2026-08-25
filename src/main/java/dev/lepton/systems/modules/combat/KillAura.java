package dev.lepton.systems.modules.combat;

import dev.lepton.event.EventHandler;
import dev.lepton.event.events.TickEvent;
import dev.lepton.settings.BoolSetting;
import dev.lepton.settings.DoubleSetting;
import dev.lepton.settings.EnumSetting;
import dev.lepton.settings.IntSetting;
import dev.lepton.systems.modules.Categories;
import dev.lepton.systems.modules.Module;
import dev.lepton.utils.entity.EntityUtils;
import dev.lepton.utils.player.PlayerUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class KillAura extends Module {
    public enum Priority {
        Closest,
        LowestHealth,
        HighestHealth
    }

    private final DoubleSetting range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("How far away you can hit.")
        .defaultValue(4.5)
        .range(1.0, 8.0)
        .sliderRange(1.0, 6.0)
        .build());

    private final EnumSetting<Priority> priority = sgGeneral.add(new EnumSetting.Builder<Priority>()
        .name("priority")
        .description("Which target to pick when several are in range.")
        .defaultValue(Priority.Closest)
        .build());

    private final IntSetting maxTargets = sgGeneral.add(new IntSetting.Builder()
        .name("max-targets")
        .description("How many entities to hit each swing.")
        .defaultValue(1)
        .range(1, 10)
        .sliderRange(1, 5)
        .build());

    private final BoolSetting useCooldown = sgGeneral.add(new BoolSetting.Builder()
        .name("respect-cooldown")
        .description("Wait for the vanilla attack cooldown so every hit is a full-damage one.")
        .defaultValue(true)
        .build());

    private final IntSetting delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("Extra ticks between swings.")
        .defaultValue(0)
        .range(0, 40)
        .sliderRange(0, 20)
        .visible(() -> !useCooldown.get())
        .build());

    private final BoolSetting rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate")
        .description("Face the target when attacking.")
        .defaultValue(true)
        .build());

    private final BoolSetting wallCheck = sgGeneral.add(new BoolSetting.Builder()
        .name("wall-check")
        .description("Do not hit through blocks.")
        .defaultValue(true)
        .build());

    private final BoolSetting hostiles = sgGeneral.add(new BoolSetting.Builder()
        .name("hostiles").description("Attack hostile mobs.").defaultValue(true).build());

    private final BoolSetting passives = sgGeneral.add(new BoolSetting.Builder()
        .name("passives").description("Attack passive mobs.").defaultValue(false).build());

    private final BoolSetting players = sgGeneral.add(new BoolSetting.Builder()
        .name("players").description("Attack players. Only ever relevant to a second local player.").defaultValue(false).build());

    private final BoolSetting pauseWhileEating = sgGeneral.add(new BoolSetting.Builder()
        .name("pause-while-eating")
        .description("Hold fire while you are using an item.")
        .defaultValue(true)
        .build());

    private int timer;
    private Entity currentTarget;

    public KillAura() {
        super(Categories.Combat, "KillAura", "Attacks nearby mobs automatically.");
    }

    @Override
    public void onDeactivate() {
        currentTarget = null;
        timer = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (pauseWhileEating.get() && mc.player.isUsingItem()) return;

        List<Entity> targets = findTargets();

        if (targets.isEmpty()) {
            currentTarget = null;
            return;
        }

        currentTarget = targets.get(0);

        if (useCooldown.get()) {
            if (mc.player.getAttackCooldownProgress(0) < 1.0f) return;
        } else {
            if (timer++ < delay.get()) return;
            timer = 0;
        }

        int hit = 0;

        for (Entity target : targets) {
            if (hit++ >= maxTargets.get()) break;

            if (rotate.get()) PlayerUtils.lookAt(target.getBoundingBox().getCenter());

            mc.interactionManager.attackEntity(mc.player, target);
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }

    private List<Entity> findTargets() {
        List<Entity> targets = new ArrayList<>();
        double maxRange = range.get();

        for (Entity entity : mc.world.getEntities()) {
            if (EntityUtils.isSelf(entity)) continue;
            if (!EntityUtils.isAttackable(entity)) continue;
            if (mc.player.distanceTo(entity) > maxRange) continue;
            if (!isEnabledFor(entity)) continue;
            if (wallCheck.get() && !mc.player.canSee(entity)) continue;

            targets.add(entity);
        }

        targets.sort(comparator());
        return targets;
    }

    private Comparator<Entity> comparator() {
        return switch (priority.get()) {
            case Closest -> Comparator.comparingDouble(e -> mc.player.distanceTo(e));
            case LowestHealth -> Comparator.comparingDouble(this::healthOf);
            case HighestHealth -> Comparator.comparingDouble(this::healthOf).reversed();
        };
    }

    private double healthOf(Entity entity) {
        return entity instanceof LivingEntity living ? living.getHealth() : Double.MAX_VALUE;
    }

    private boolean isEnabledFor(Entity entity) {
        return switch (EntityUtils.groupOf(entity)) {
            case Hostile -> hostiles.get();
            case Passive -> passives.get();
            case Player -> players.get();
            default -> false;
        };
    }

    @Override
    public String getInfoString() {
        return currentTarget == null ? null : currentTarget.getType().getName().getString();
    }
}
