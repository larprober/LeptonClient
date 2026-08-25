package dev.lepton.event;

import dev.lepton.Lepton;
import dev.lepton.event.events.GameJoinedEvent;
import dev.lepton.event.events.GameLeftEvent;
import dev.lepton.event.events.TickEvent;
import dev.lepton.systems.config.Config;
import dev.lepton.systems.modules.Modules;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

/**
 * Bridges Fabric's lifecycle callbacks onto Lepton's own bus.
 *
 * <p>World join/leave is derived from watching {@code mc.world} rather than hooking the
 * network handler -- Lepton deliberately does not touch the network layer at all.
 */
public class LeptonEvents {
    private static boolean wasInWorld;

    public static void register() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            updateWorldState(client);
            Lepton.EVENTS.post(TickEvent.Pre.get());
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> Lepton.EVENTS.post(TickEvent.Post.get()));
    }

    private static void updateWorldState(MinecraftClient client) {
        boolean inWorld = client.world != null && client.player != null;

        if (inWorld == wasInWorld) return;
        wasInWorld = inWorld;

        if (inWorld) {
            Lepton.EVENTS.post(GameJoinedEvent.get());

            Config config = Config.get();
            if (config != null && config.restoreModules.get()) {
                Modules modules = Modules.get();
                if (modules != null) modules.restoreToggledOn();
            }
        } else {
            Lepton.EVENTS.post(GameLeftEvent.get());
        }
    }
}
