package dev.lepton;

import net.minecraft.client.MinecraftClient;
import net.minecraft.server.integrated.IntegratedServer;

/**
 * The single point of truth for whether Lepton is allowed to do anything at all.
 *
 * <p>Lepton is a singleplayer-only client. Every module routes through {@link #isAllowed()}
 * before it can activate, and {@link dev.lepton.systems.modules.Modules} re-checks it every
 * tick and force-disables everything the moment the answer changes to {@code false}.
 *
 * <p>Three independent conditions must all hold. They overlap on purpose -- if a future
 * Minecraft version changes the semantics of one, the others still close the gate:
 * <ul>
 *   <li>the client reports it is in singleplayer,</li>
 *   <li>there is no remote server entry (we did not join anything),</li>
 *   <li>the integrated server has not been opened to LAN.</li>
 * </ul>
 *
 * <p>A world opened to LAN counts as multiplayer. Other people are in it, which is exactly
 * the situation this client refuses to operate in.
 */
public final class SinglePlayerGate {
    private SinglePlayerGate() {}

    public static boolean isAllowed() {
        return reason() == null;
    }

    /**
     * Returns {@code null} when Lepton may run, otherwise a human-readable explanation
     * of why it may not. Used for chat feedback and the ClickGUI banner.
     */
    public static String reason() {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (mc == null) return "Client is not ready.";
        if (mc.world == null || mc.player == null) return "Not in a world.";

        if (!mc.isInSingleplayer()) return "Lepton only runs in singleplayer.";
        if (mc.getCurrentServerEntry() != null) return "Lepton only runs in singleplayer.";

        IntegratedServer server = mc.getServer();
        if (server == null) return "No integrated server -- this is not a singleplayer world.";
        if (isPublishedToLan(server)) return "This world is open to LAN, which counts as multiplayer.";

        return null;
    }

    /** Whether the integrated server has been shared over LAN. */
    public static boolean isPublishedToLan(IntegratedServer server) {
        return server.isRemote();
    }

    /**
     * Convenience for module tick handlers. A module should never assume it is allowed to
     * act just because it is active -- the world can change underneath it mid-tick.
     */
    public static boolean canAct() {
        return isAllowed();
    }
}
