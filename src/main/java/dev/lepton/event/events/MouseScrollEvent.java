package dev.lepton.event.events;

import dev.lepton.event.Cancellable;

public class MouseScrollEvent extends Cancellable {
    private static final MouseScrollEvent INSTANCE = new MouseScrollEvent();

    public double value;

    public static MouseScrollEvent get(double value) {
        INSTANCE.reset();
        INSTANCE.value = value;
        return INSTANCE;
    }
}
