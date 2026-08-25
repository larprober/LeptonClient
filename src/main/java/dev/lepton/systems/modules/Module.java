package dev.lepton.systems.modules;

import com.google.gson.JsonObject;
import dev.lepton.Lepton;
import dev.lepton.SinglePlayerGate;
import dev.lepton.settings.Settings;
import dev.lepton.settings.SettingGroup;
import dev.lepton.utils.misc.ChatUtils;
import dev.lepton.utils.misc.Keybind;
import net.minecraft.client.MinecraftClient;

public abstract class Module implements Comparable<Module> {
    protected final MinecraftClient mc = MinecraftClient.getInstance();

    public final Category category;
    public final String name;
    public final String title;
    public final String description;

    public final Settings settings = new Settings();
    protected final SettingGroup sgGeneral = settings.createGroup("General");

    public final Keybind keybind = Keybind.none();
    public boolean toggleOnBindRelease;
    public boolean chatFeedback = true;
    public boolean favourite;

    private boolean active;

    public Module(Category category, String name, String description) {
        this.category = category;
        this.name = name;
        this.title = name;
        this.description = description;
    }

    // -- lifecycle ------------------------------------------------------------

    public void onActivate() {}

    public void onDeactivate() {}

    /** Called once, after every module has been constructed and configs are loaded. */
    public void onRegistered() {}

    public boolean isActive() {
        return active;
    }

    public void toggle() {
        setActive(!active);
    }

    public void setActive(boolean shouldBeActive) {
        if (shouldBeActive == active) return;

        if (shouldBeActive) {
            String denial = SinglePlayerGate.reason();

            if (denial != null) {
                ChatUtils.error("{} blocked: {}", name, denial);
                return;
            }
        }

        applyActive(shouldBeActive);

        if (chatFeedback) ChatUtils.toggle(title, shouldBeActive);
    }

    /**
     * Flips state without consulting the gate. Only for {@link Modules} shutting everything
     * down when the gate closes -- turning modules OFF must never be blocked.
     */
    void forceOff() {
        if (!active) return;
        applyActive(false);
    }

    private void applyActive(boolean shouldBeActive) {
        active = shouldBeActive;

        if (active) {
            Lepton.EVENTS.subscribe(this);

            try {
                onActivate();
            } catch (Exception e) {
                Lepton.LOG.error("Module {} threw during onActivate, disabling it", name, e);
                active = false;
                Lepton.EVENTS.unsubscribe(this);
            }
        } else {
            Lepton.EVENTS.unsubscribe(this);

            try {
                onDeactivate();
            } catch (Exception e) {
                Lepton.LOG.error("Module {} threw during onDeactivate", name, e);
            }
        }
    }

    // -- display --------------------------------------------------------------

    /** Extra text shown next to the module name in the HUD arraylist, e.g. a mode or a number. */
    public String getInfoString() {
        return null;
    }

    // -- persistence ----------------------------------------------------------

    public JsonObject toJson() {
        JsonObject json = new JsonObject();

        json.addProperty("name", name);
        json.addProperty("active", active);
        json.addProperty("favourite", favourite);
        json.addProperty("chatFeedback", chatFeedback);
        json.addProperty("toggleOnBindRelease", toggleOnBindRelease);
        json.add("keybind", keybind.toJson());
        json.add("settings", settings.toJson());

        return json;
    }

    public void fromJson(JsonObject json) {
        if (json.has("settings")) settings.fromJson(json.getAsJsonObject("settings"));
        if (json.has("keybind")) keybind.set(Keybind.fromJson(json.getAsJsonObject("keybind")));
        if (json.has("favourite")) favourite = json.get("favourite").getAsBoolean();
        if (json.has("chatFeedback")) chatFeedback = json.get("chatFeedback").getAsBoolean();
        if (json.has("toggleOnBindRelease")) toggleOnBindRelease = json.get("toggleOnBindRelease").getAsBoolean();

        // Deliberately NOT restoring "active" here. Modules only come back on once the
        // player is actually in a singleplayer world -- see Modules#restoreToggledOn.
        if (json.has("active")) wasActive = json.get("active").getAsBoolean();
    }

    /** Remembered across sessions, re-applied only once the gate opens. */
    boolean wasActive;

    @Override
    public int compareTo(Module other) {
        return name.compareToIgnoreCase(other.name);
    }

    @Override
    public String toString() {
        return name;
    }
}
