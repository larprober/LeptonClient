package dev.lepton.settings;

import net.minecraft.block.Block;
import net.minecraft.registry.Registries;

import java.util.List;
import java.util.function.Consumer;

public class BlockListSetting extends RegistryListSetting<Block> {
    public BlockListSetting(String name, String description, List<Block> defaultValue, Consumer<List<Block>> onChanged, IVisible visible) {
        super(name, description, defaultValue, onChanged, visible, Registries.BLOCK);
    }

    public static class Builder extends RegistryListSetting.Builder<Builder, Block, BlockListSetting> {
        @Override
        public BlockListSetting build() {
            return new BlockListSetting(name, description, defaultValue, onChanged, visible);
        }
    }
}
