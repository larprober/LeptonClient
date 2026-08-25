package dev.lepton.utils.player;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.function.Predicate;

public class InvUtils {
    private static MinecraftClient mc() {
        return MinecraftClient.getInstance();
    }

    private static PlayerInventory inventory() {
        ClientPlayerEntity player = mc().player;
        return player == null ? null : player.getInventory();
    }

    public static ItemStack held() {
        ClientPlayerEntity player = mc().player;
        return player == null ? ItemStack.EMPTY : player.getMainHandStack();
    }

    public static int selectedSlot() {
        PlayerInventory inv = inventory();
        return inv == null ? 0 : inv.getSelectedSlot();
    }

    public static void select(int slot) {
        PlayerInventory inv = inventory();
        if (inv == null || slot < 0 || slot >= PlayerInventory.getHotbarSize()) return;

        inv.setSelectedSlot(slot);
    }

    /** Hotbar slot index holding a matching stack, or -1. */
    public static int findHotbar(Predicate<ItemStack> filter) {
        PlayerInventory inv = inventory();
        if (inv == null) return -1;

        for (int i = 0; i < PlayerInventory.getHotbarSize(); i++) {
            if (filter.test(inv.getStack(i))) return i;
        }

        return -1;
    }

    public static int findHotbar(Item item) {
        return findHotbar(stack -> stack.getItem() == item);
    }

    /** Any inventory slot holding a matching stack, or -1. */
    public static int findInventory(Predicate<ItemStack> filter) {
        PlayerInventory inv = inventory();
        if (inv == null) return -1;

        for (int i = 0; i < inv.size(); i++) {
            if (filter.test(inv.getStack(i))) return i;
        }

        return -1;
    }

    public static boolean has(Predicate<ItemStack> filter) {
        return findInventory(filter) != -1;
    }

    public static int count(Predicate<ItemStack> filter) {
        PlayerInventory inv = inventory();
        if (inv == null) return 0;

        int total = 0;

        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
            if (filter.test(stack)) total += stack.getCount();
        }

        return total;
    }

    /**
     * Selects a hotbar slot matching the filter if one exists.
     * Returns the previously selected slot, or -1 if nothing matched.
     */
    public static int selectMatching(Predicate<ItemStack> filter) {
        int slot = findHotbar(filter);
        if (slot == -1) return -1;

        int previous = selectedSlot();
        select(slot);
        return previous;
    }
}
