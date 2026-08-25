package dev.lepton.event.events;

import dev.lepton.event.Cancellable;

public class CharTypedEvent extends Cancellable {
    private static final CharTypedEvent INSTANCE = new CharTypedEvent();

    public char c;

    public static CharTypedEvent get(char c) {
        INSTANCE.reset();
        INSTANCE.c = c;
        return INSTANCE;
    }
}
