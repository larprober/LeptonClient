package dev.lepton.settings;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;

import java.util.List;
import java.util.function.Consumer;

public class ItemListSetting extends RegistryListSetting<Item> {
    public ItemListSetting(String name, String description, List<Item> defaultValue, Consumer<List<Item>> onChanged, IVisible visible) {
        super(name, description, defaultValue, onChanged, visible, Registries.ITEM);
    }

    public static class Builder extends RegistryListSetting.Builder<Builder, Item, ItemListSetting> {
        @Override
        public ItemListSetting build() {
            return new ItemListSetting(name, description, defaultValue, onChanged, visible);
        }
    }
}
