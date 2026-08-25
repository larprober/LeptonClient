package dev.lepton.settings;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Settings implements Iterable<SettingGroup> {
    private final List<SettingGroup> groups = new ArrayList<>();

    public SettingGroup createGroup(String name) {
        return createGroup(name, true);
    }

    public SettingGroup createGroup(String name, boolean expanded) {
        SettingGroup group = new SettingGroup(name, expanded);
        groups.add(group);
        return group;
    }

    public SettingGroup getGroup(String name) {
        for (SettingGroup group : groups) {
            if (group.name.equalsIgnoreCase(name)) return group;
        }
        return null;
    }

    public Setting<?> get(String name) {
        for (SettingGroup group : groups) {
            Setting<?> setting = group.get(name);
            if (setting != null) return setting;
        }
        return null;
    }

    public List<Setting<?>> getAll() {
        List<Setting<?>> all = new ArrayList<>();
        for (SettingGroup group : groups) all.addAll(group.getAll());
        return all;
    }

    public void reset() {
        for (SettingGroup group : groups) {
            for (Setting<?> setting : group) setting.reset();
        }
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();

        for (SettingGroup group : groups) {
            for (Setting<?> setting : group) json.add(setting.name, setting.toJson());
        }

        return json;
    }

    public void fromJson(JsonObject json) {
        if (json == null) return;

        for (SettingGroup group : groups) {
            for (Setting<?> setting : group) {
                if (json.has(setting.name)) {
                    try {
                        setting.fromJson(json.get(setting.name));
                    } catch (Exception ignored) {
                        setting.reset();
                    }
                }
            }
        }
    }

    @Override
    public Iterator<SettingGroup> iterator() {
        return groups.iterator();
    }
}
