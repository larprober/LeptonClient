package dev.lepton.systems.modules;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.lepton.Lepton;
import dev.lepton.SinglePlayerGate;
import dev.lepton.event.EventHandler;
import dev.lepton.event.events.GameLeftEvent;
import dev.lepton.event.events.TickEvent;
import dev.lepton.systems.LeptonSystem;
import dev.lepton.systems.Systems;
import dev.lepton.utils.misc.ChatUtils;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Modules extends LeptonSystem {
    private final Map<Class<? extends Module>, Module> byClass = new LinkedHashMap<>();
    private final Map<String, Module> byName = new LinkedHashMap<>();
    private final Map<Category, List<Module>> byCategory = new LinkedHashMap<>();
    private final List<Module> all = new ArrayList<>();

    private final Map<Module, Boolean> bindStates = new HashMap<>();

    /** Set while the gate is shut so we only nag the player once per transition. */
    private boolean gateWarned;

    public Modules() {
        super("modules");

        for (Category category : Categories.all()) byCategory.put(category, new ArrayList<>());
    }

    public static Modules get() {
        return Systems.get(Modules.class);
    }

    @Override
    public void init() {
        Lepton.EVENTS.subscribe(this);
        ModuleRegistry.registerAll(this);

        for (List<Module> list : byCategory.values()) Collections.sort(list);
        Collections.sort(all);

        Lepton.LOG.info("Registered {} modules", all.size());
    }

    @Override
    public void ready() {
        for (Module module : all) module.onRegistered();
    }

    // -- registry -------------------------------------------------------------

    public void add(Module module) {
        if (byName.containsKey(module.name.toLowerCase(Locale.ROOT))) {
            Lepton.LOG.warn("Duplicate module name '{}', skipping", module.name);
            return;
        }

        byClass.put(module.getClass(), module);
        byName.put(module.name.toLowerCase(Locale.ROOT), module);
        byCategory.get(module.category).add(module);
        all.add(module);
    }

    @SuppressWarnings("unchecked")
    public <T extends Module> T get(Class<T> type) {
        return (T) byClass.get(type);
    }

    public Module get(String name) {
        return byName.get(name.toLowerCase(Locale.ROOT));
    }

    public List<Module> getAll() {
        return all;
    }

    public List<Module> getCategory(Category category) {
        return byCategory.getOrDefault(category, List.of());
    }

    public List<Module> getActive() {
        List<Module> active = new ArrayList<>();
        for (Module module : all) if (module.isActive()) active.add(module);
        return active;
    }

    public List<Module> search(String query) {
        String q = query.toLowerCase(Locale.ROOT).trim();
        List<Module> results = new ArrayList<>();

        if (q.isEmpty()) return results;

        for (Module module : all) {
            if (module.name.toLowerCase(Locale.ROOT).contains(q)) results.add(module);
        }

        for (Module module : all) {
            if (!results.contains(module) && module.description.toLowerCase(Locale.ROOT).contains(q)) results.add(module);
        }

        return results;
    }

    // -- the gate -------------------------------------------------------------

    /**
     * Runs every tick. If the player is no longer in a plain singleplayer world -- they joined
     * a server, or opened this world to LAN -- every module is switched off immediately.
     */
    @EventHandler
    private void onTick(TickEvent.Pre event) {
        pollKeybinds();

        if (SinglePlayerGate.isAllowed()) {
            gateWarned = false;
            return;
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.world == null) return;

        List<Module> active = getActive();
        if (active.isEmpty()) return;

        for (Module module : active) module.forceOff();

        if (!gateWarned) {
            gateWarned = true;
            ChatUtils.error("Disabled {} module(s): {}", active.size(), SinglePlayerGate.reason());
        }
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        disableAll();
        gateWarned = false;
    }

    public void disableAll() {
        for (Module module : all) module.forceOff();
    }

    // -- keybinds -------------------------------------------------------------

    /**
     * Keybinds are polled from GLFW each tick rather than driven by key events.
     * Polling cannot miss an edge because of thread hand-off, and it treats keyboard
     * and mouse binds identically.
     */
    private void pollKeybinds() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) return;

        boolean screenOpen = mc.currentScreen != null;

        for (Module module : all) {
            if (!module.keybind.isSet()) {
                bindStates.remove(module);
                continue;
            }

            // While a screen is open we still track the key so releasing it there does
            // not fire a toggle the moment the screen closes.
            boolean pressed = !screenOpen && module.keybind.isPressed();
            boolean was = bindStates.getOrDefault(module, false);

            if (pressed != was) {
                bindStates.put(module, pressed);

                boolean shouldToggle = module.toggleOnBindRelease ? (!pressed && was) : (pressed && !was);
                if (shouldToggle) module.toggle();
            }
        }
    }

    // -- persistence ----------------------------------------------------------

    @Override
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        JsonArray array = new JsonArray();

        for (Module module : all) array.add(module.toJson());

        json.add("modules", array);
        return json;
    }

    @Override
    public void fromJson(JsonObject json) {
        if (!json.has("modules")) return;

        for (JsonElement element : json.getAsJsonArray("modules")) {
            JsonObject entry = element.getAsJsonObject();
            if (!entry.has("name")) continue;

            Module module = get(entry.get("name").getAsString());
            if (module == null) continue;

            try {
                module.fromJson(entry);
            } catch (Exception e) {
                Lepton.LOG.warn("Could not restore settings for module {}", module.name, e);
            }
        }
    }

    /**
     * Re-enables whatever was on when the game last closed, but only once the player is
     * actually standing in a singleplayer world. Called on world join.
     */
    public void restoreToggledOn() {
        if (!SinglePlayerGate.isAllowed()) return;

        for (Module module : all) {
            if (module.wasActive && !module.isActive()) {
                boolean feedback = module.chatFeedback;
                module.chatFeedback = false;
                module.setActive(true);
                module.chatFeedback = feedback;
            }
        }
    }
}
