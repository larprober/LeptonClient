package dev.lepton.settings;

import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;

import java.util.List;
import java.util.function.Consumer;

public class EntityTypeListSetting extends RegistryListSetting<EntityType<?>> {
    public EntityTypeListSetting(String name, String description, List<EntityType<?>> defaultValue,
                                 Consumer<List<EntityType<?>>> onChanged, IVisible visible) {
        // Registries.ENTITY_TYPE is Registry<EntityType<?>>; the cast keeps the generic plumbing quiet.
        super(name, description, defaultValue, onChanged, visible, Registries.ENTITY_TYPE);
    }

    public static class Builder extends RegistryListSetting.Builder<Builder, EntityType<?>, EntityTypeListSetting> {
        @Override
        public EntityTypeListSetting build() {
            return new EntityTypeListSetting(name, description, defaultValue, onChanged, visible);
        }
    }
}
