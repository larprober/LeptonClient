package dev.lepton.event.events;

import dev.lepton.event.Cancellable;

public class KeyEvent extends Cancellable {
    private static final KeyEvent INSTANCE = new KeyEvent();

    public int key;
    public int modifiers;
    public KeyAction action;

    public static KeyEvent get(int key, int modifiers, KeyAction action) {
        INSTANCE.reset();
        INSTANCE.key = key;
        INSTANCE.modifiers = modifiers;
        INSTANCE.action = action;
        return INSTANCE;
    }

    public enum KeyAction {
        Press,
        Repeat,
        Release
    }
}
