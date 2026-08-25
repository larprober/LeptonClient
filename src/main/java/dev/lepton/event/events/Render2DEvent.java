package dev.lepton.event.events;

import net.minecraft.client.gui.DrawContext;

public class Render2DEvent {
    private static final Render2DEvent INSTANCE = new Render2DEvent();

    public DrawContext context;
    public float tickDelta;

    public static Render2DEvent get(DrawContext context, float tickDelta) {
        INSTANCE.context = context;
        INSTANCE.tickDelta = tickDelta;
        return INSTANCE;
    }
}
