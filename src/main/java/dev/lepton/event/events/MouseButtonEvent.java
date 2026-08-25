package dev.lepton.event.events;

import dev.lepton.event.Cancellable;
import dev.lepton.event.events.KeyEvent.KeyAction;

public class MouseButtonEvent extends Cancellable {
    private static final MouseButtonEvent INSTANCE = new MouseButtonEvent();

    public int button;
    public KeyAction action;

    public static MouseButtonEvent get(int button, KeyAction action) {
        INSTANCE.reset();
        INSTANCE.button = button;
        INSTANCE.action = action;
        return INSTANCE;
    }
}
