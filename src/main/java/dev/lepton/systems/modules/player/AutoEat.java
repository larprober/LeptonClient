package dev.lepton.systems.modules.player;

import dev.lepton.event.EventHandler;
import dev.lepton.event.events.TickEvent;
import dev.lepton.settings.BoolSetting;
import dev.lepton.settings.IntSetting;
import dev.lepton.settings.ItemListSetting;
import dev.lepton.systems.modules.Categories;
import dev.lepton.systems.modules.Module;
import dev.lepton.utils.player.InvUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class AutoEat extends Module {
    private final IntSetting threshold = sgGeneral.add(new IntSetting.Builder()
        .name("hunger-threshold")
        .description("Start eating when hunger drops to this or below.")
        .defaultValue(16)
        .range(1, 19)
        .sliderRange(1, 19)
        .build());

    private final IntSetting healthThreshold = sgGeneral.add(new IntSetting.Builder()
        .name("health-threshold")
        .description("Also eat when health drops to this or below.")
        .defaultValue(10)
        .range(1, 20)
        .sliderRange(1, 20)
        .build());

    private final ItemListSetting blacklist = sgGeneral.add(new ItemListSetting.Builder()
        .name("never-eat")
        .description("Food the module will not touch.")
        .defaultValue(Items.ROTTEN_FLESH, Items.SPIDER_EYE, Items.POISONOUS_POTATO,
            Items.PUFFERFISH, Items.CHICKEN, Items.SUSPICIOUS_STEW, Items.GOLDEN_APPLE,
            Items.ENCHANTED_GOLDEN_APPLE)
        .build());

    private final BoolSetting pauseMovement = sgGeneral.add(new BoolSetting.Builder()
        .name("pause-actions")
        .description("Stop mining and attacking while eating.")
        .defaultValue(true)
        .build());

    private boolean eating;
    private int previousSlot = -1;

    public AutoEat() {
        super(Categories.Player, "AutoEat", "Eats for you when you get hungry.");
    }

    @Override
    public void onDeactivate() {
        stopEating();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.interactionManager == null) return;

        if (!shouldEat()) {
            if (eating) stopEating();
            return;
        }

        int slot = InvUtils.findHotbar(this::isEdible);

        if (slot == -1) {
            if (eating) stopEating();
            return;
        }

        if (!eating) {
            previousSlot = InvUtils.selectedSlot();
            eating = true;
        }

        InvUtils.select(slot);

        if (pauseMovement.get()) mc.options.attackKey.setPressed(false);

        mc.options.useKey.setPressed(true);
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
    }

    private boolean shouldEat() {
        if (mc.player == null) return false;

        int hunger = mc.player.getHungerManager().getFoodLevel();
        if (hunger <= threshold.get()) return true;

        return mc.player.getHealth() <= healthThreshold.get() && hunger < 20;
    }

    private boolean isEdible(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (!stack.contains(DataComponentTypes.FOOD)) return false;

        return !blacklist.get().contains(stack.getItem());
    }

    private void stopEating() {
        if (!eating) return;

        eating = false;
        mc.options.useKey.setPressed(false);

        if (mc.player != null) mc.player.stopUsingItem();
        if (previousSlot != -1) InvUtils.select(previousSlot);

        previousSlot = -1;
    }

    @Override
    public String getInfoString() {
        return eating ? "eating" : null;
    }
}
