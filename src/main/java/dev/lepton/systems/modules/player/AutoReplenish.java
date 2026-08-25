package dev.lepton.systems.modules.player;

import dev.lepton.event.EventHandler;
import dev.lepton.event.events.TickEvent;
import dev.lepton.settings.BoolSetting;
import dev.lepton.settings.IntSetting;
import dev.lepton.systems.modules.Categories;
import dev.lepton.systems.modules.Module;
import dev.lepton.utils.player.InvUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

/**
 * Refills the held stack from the inventory before it runs out, so a bridging or
 * building run is never interrupted by an empty hand.
 */
public class AutoReplenish extends Module {
    private final IntSetting threshold = sgGeneral.add(new IntSetting.Builder()
        .name("threshold")
        .description("Refill when the held stack drops to this many items.")
        .defaultValue(8)
        .range(1, 63)
        .sliderRange(1, 32)
        .build());

    private final BoolSetting offhand = sgGeneral.add(new BoolSetting.Builder()
        .name("include-offhand")
        .description("Also keep the offhand stack topped up.")
        .defaultValue(true)
        .build());

    private final IntSetting delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("Ticks between refill attempts.")
        .defaultValue(2)
        .range(0, 20)
        .sliderRange(0, 10)
        .build());

    private int timer;

    public AutoReplenish() {
        super(Categories.Player, "AutoReplenish", "Tops up your held stack before it runs out.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;

        if (timer++ < delay.get()) return;
        timer = 0;

        ItemStack held = mc.player.getMainHandStack();
        if (held.isEmpty() || held.getCount() > threshold.get() || held.getMaxCount() == 1) return;

        int source = findMatchingStack(held);
        if (source == -1) return;

        // Pick up the spare stack and drop it onto the held slot; the game merges them.
        int targetSlot = InvUtils.selectedSlot() + 36;

        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, source, 0, SlotActionType.PICKUP, mc.player);
        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, targetSlot, 0, SlotActionType.PICKUP, mc.player);
        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, source, 0, SlotActionType.PICKUP, mc.player);
    }

    private int findMatchingStack(ItemStack held) {
        for (int slot = 9; slot < 36; slot++) {
            ItemStack stack = mc.player.getInventory().getStack(slot);
            if (stack.isEmpty()) continue;
            if (!ItemStack.areItemsAndComponentsEqual(stack, held)) continue;

            // Convert inventory index to screen handler slot index.
            return slot;
        }

        return -1;
    }

    @Override
    public String getInfoString() {
        return String.valueOf(threshold.get());
    }
}
