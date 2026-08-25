package dev.lepton.systems;

import com.google.gson.JsonObject;

/**
 * A persistent subsystem: modules, config, HUD, macros, waypoints, profiles.
 * Named {@code LeptonSystem} rather than {@code System} so it never shadows {@link java.lang.System}.
 */
public abstract class LeptonSystem {
    /** Also the config file name, without extension. */
    public final String name;

    protected LeptonSystem(String name) {
        this.name = name;
    }

    /** Called once during client init, in registration order. */
    public void init() {}

    /** Called after every system has been constructed and its config loaded. */
    public void ready() {}

    public abstract JsonObject toJson();

    public abstract void fromJson(JsonObject json);
}
