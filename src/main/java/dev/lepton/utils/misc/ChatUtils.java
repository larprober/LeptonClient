package dev.lepton.utils.misc;

import dev.lepton.Lepton;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** Client-side chat output. Nothing here ever reaches a server -- these messages are local only. */
public class ChatUtils {
    private static MutableText prefix() {
        return Text.literal("")
            .append(Text.literal("[").formatted(Formatting.DARK_GRAY))
            .append(Text.literal(Lepton.NAME_SHORT).formatted(Formatting.AQUA, Formatting.BOLD))
            .append(Text.literal("] ").formatted(Formatting.DARK_GRAY));
    }

    private static void send(MutableText body) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.inGameHud == null) return;

        mc.inGameHud.getChatHud().addMessage(prefix().append(body));
    }

    public static void info(String message, Object... args) {
        send(Text.literal(format(message, args)).formatted(Formatting.GRAY));
    }

    public static void warning(String message, Object... args) {
        send(Text.literal(format(message, args)).formatted(Formatting.YELLOW));
    }

    public static void error(String message, Object... args) {
        send(Text.literal(format(message, args)).formatted(Formatting.RED));
    }

    /** Used for module on/off feedback so the colour reads at a glance. */
    public static void toggle(String moduleName, boolean on) {
        send(Text.literal("")
            .append(Text.literal(moduleName).formatted(Formatting.WHITE))
            .append(Text.literal(" ").formatted(Formatting.GRAY))
            .append(Text.literal(on ? "on" : "off").formatted(on ? Formatting.GREEN : Formatting.RED)));
    }

    private static String format(String message, Object... args) {
        if (args == null || args.length == 0) return message;

        StringBuilder sb = new StringBuilder();
        int argIndex = 0;

        for (int i = 0; i < message.length(); i++) {
            if (i + 1 < message.length() && message.charAt(i) == '{' && message.charAt(i + 1) == '}' && argIndex < args.length) {
                sb.append(args[argIndex++]);
                i++;
            } else {
                sb.append(message.charAt(i));
            }
        }

        return sb.toString();
    }
}
