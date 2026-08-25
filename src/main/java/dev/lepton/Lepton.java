package dev.lepton;

import dev.lepton.event.EventBus;
import dev.lepton.event.LeptonEvents;
import dev.lepton.gui.ClickGui;
import dev.lepton.systems.config.Config;
import dev.lepton.systems.hud.Hud;
import dev.lepton.systems.Systems;
import dev.lepton.systems.modules.Modules;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lepton Client -- a singleplayer-only utility client.
 *
 * <p>Nothing in this mod touches the network layer. Every module is gated behind
 * {@link SinglePlayerGate}, which refuses to open on servers or LAN-shared worlds.
 */
public class Lepton implements ClientModInitializer {
    public static final String MOD_ID = "lepton";
    public static final String NAME = "Lepton Client";
    public static final String NAME_SHORT = "Lepton";
    public static final String VERSION = "1.0.0";

    public static final Logger LOG = LoggerFactory.getLogger(NAME);
    public static final EventBus EVENTS = new EventBus();

    private static boolean initialised;

    @Override
    public void onInitializeClient() {
        LOG.info("Initialising {} v{} (singleplayer only)", NAME, VERSION);

        Systems.add(new Config());
        Systems.add(new Modules());
        Systems.add(new Hud());

        Systems.init();
        Systems.load();
        Systems.ready();

        LeptonEvents.register();
        ClickGui.register();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                Systems.save();
            } catch (Exception e) {
                LOG.error("Failed to save config on shutdown", e);
            }
        }, "Lepton-Save"));

        initialised = true;
        LOG.info("{} ready", NAME);
    }

    public static boolean isInitialised() {
        return initialised;
    }
}
