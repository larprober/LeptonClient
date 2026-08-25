package dev.lepton.settings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SettingGroup implements Iterable<Setting<?>> {
    public final String name;
    public boolean expanded;

    private final List<Setting<?>> settings = new ArrayList<>();

    public SettingGroup(String name, boolean expanded) {
        this.name = name;
        this.expanded = expanded;
    }

    public <T extends Setting<?>> T add(T setting) {
        setting.group = this;
        settings.add(setting);
        return setting;
    }

    public Setting<?> get(String name) {
        for (Setting<?> setting : settings) {
            if (setting.name.equalsIgnoreCase(name)) return setting;
        }
        return null;
    }

    public List<Setting<?>> getAll() {
        return settings;
    }

    public boolean isEmpty() {
        return settings.isEmpty();
    }

    /** True when at least one setting in this group is currently visible. */
    public boolean anyVisible() {
        for (Setting<?> setting : settings) {
            if (setting.isVisible()) return true;
        }
        return false;
    }

    @Override
    public Iterator<Setting<?>> iterator() {
        return settings.iterator();
    }
}
