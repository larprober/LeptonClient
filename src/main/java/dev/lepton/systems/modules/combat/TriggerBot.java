package dev.lepton.systems.modules.combat;

import dev.lepton.event.EventHandler;
import dev.lepton.event.events.TickEvent;
import dev.lepton.settings.BoolSetting;
import dev.lepton.settings.IntSetting;
import dev.lepton.systems.modules.Categories;
import dev.lepton.systems.modules.Module;
import dev.lepton.utils.entity.EntityUtils;
import net.minecraft.entity.Entity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

/** Attacks only what you are already looking at -- no rotation, no target selection. */
public class TriggerBot extends Module {
    private final BoolSetting useCooldown = sgGeneral.add(new BoolSetting.Builder()
        .name("respect-cooldown")
        .description("Wait for the vanilla attack cooldown.")
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

    private final BoolSetting hostiles = sgGeneral.add(new BoolSetting.Builder()
        .name("hostiles").description("Attack hostile mobs.").defaultValue(true).build());

    private final BoolSetting passives = sgGeneral.add(new BoolSetting.Builder()
        .name("passives").description("Attack passive mobs.").defaultValue(false).build());

    private int timer;

    public TriggerBot() {
        super(Categories.Combat, "TriggerBot", "Swings the moment your crosshair lands on a mob.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.crosshairTarget == null || mc.crosshairTarget.getType() != HitResult.Type.ENTITY) return;
        if (mc.player.isUsingItem()) return;

        Entity target = ((EntityHitResult) mc.crosshairTarget).getEntity();

        if (!EntityUtils.isAttackable(target)) return;
        if (!isEnabledFor(target)) return;

        if (useCooldown.get()) {
            if (mc.player.getAttackCooldownProgress(0) < 1.0f) return;
        } else {
            if (timer++ < delay.get()) return;
            timer = 0;
        }

        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    private boolean isEnabledFor(Entity entity) {
        return switch (EntityUtils.groupOf(entity)) {
            case Hostile -> hostiles.get();
            case Passive -> passives.get();
            default -> false;
        };
    }
}
