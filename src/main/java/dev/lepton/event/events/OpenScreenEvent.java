package dev.lepton.event.events;

import dev.lepton.event.Cancellable;
import net.minecraft.client.gui.screen.Screen;

public class OpenScreenEvent extends Cancellable {
    private static final OpenScreenEvent INSTANCE = new OpenScreenEvent();

    public Screen screen;

    public static OpenScreenEvent get(Screen screen) {
        INSTANCE.reset();
        INSTANCE.screen = screen;
        return INSTANCE;
    }
}
