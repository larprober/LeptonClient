package dev.lepton.gui;

import dev.lepton.Lepton;
import dev.lepton.event.EventHandler;
import dev.lepton.event.events.KeyEvent;
import dev.lepton.event.events.MouseButtonEvent;
import dev.lepton.event.events.TickEvent;
import dev.lepton.gui.screen.ClickGuiScreen;
import dev.lepton.systems.config.Config;
import net.minecraft.client.MinecraftClient;

/**
 * Owns the ClickGUI keybind.
 *
 * <p>The bind is detected by polling GLFW every tick rather than by reacting to a key
 * event. Polling is immune to the event being delivered on the wrong thread or a mixin
 * silently not applying, and it costs one GLFW call per tick. The event path is kept as a
 * secondary trigger so a mouse-button bind also works.
 */
public class ClickGui {
    private static final ClickGui INSTANCE = new ClickGui();

    private boolean wasPressed;
    private boolean warnedNoWorld;

    public static void register() {
        Lepton.EVENTS.subscribe(INSTANCE);
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        Config config = Config.get();
        if (config == null) return;

        boolean pressed = config.guiKeybind.get().isPressed();

        // Edge-triggered: fire once when the key goes down, not every tick it is held.
        if (pressed && !wasPressed) open();
        wasPressed = pressed;
    }

    @EventHandler
    private void onMouseButton(MouseButtonEvent event) {
        if (event.action != KeyEvent.KeyAction.Press) return;

        Config config = Config.get();
        if (config == null || !config.guiKeybind.get().matches(false, event.button)) return;

        open();
    }

    public static void open() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) return;

        // Already showing something -- the screen handles closing itself.
        if (mc.currentScreen != null) return;

        if (mc.player == null || mc.world == null) {
            if (!INSTANCE.warnedNoWorld) {
                INSTANCE.warnedNoWorld = true;
                Lepton.LOG.info("ClickGUI bind pressed but no world is loaded -- load a singleplayer world first.");
            }
            return;
        }

        INSTANCE.warnedNoWorld = false;
        mc.setScreen(new ClickGuiScreen());
    }
}
