package dev.lepton.systems;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import dev.lepton.Lepton;
import net.minecraft.client.MinecraftClient;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Systems {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Map<Class<?>, LeptonSystem> BY_CLASS = new LinkedHashMap<>();
    private static final List<LeptonSystem> ORDER = new ArrayList<>();

    private static Path folder;

    public static Path folder() {
        if (folder == null) {
            folder = MinecraftClient.getInstance().runDirectory.toPath().resolve(Lepton.MOD_ID);

            try {
                Files.createDirectories(folder);
            } catch (IOException e) {
                Lepton.LOG.error("Could not create the Lepton config folder", e);
            }
        }

        return folder;
    }

    public static <T extends LeptonSystem> T add(T system) {
        BY_CLASS.put(system.getClass(), system);
        ORDER.add(system);
        return system;
    }

    @SuppressWarnings("unchecked")
    public static <T extends LeptonSystem> T get(Class<T> type) {
        return (T) BY_CLASS.get(type);
    }

    public static List<LeptonSystem> all() {
        return ORDER;
    }

    public static void init() {
        for (LeptonSystem system : ORDER) {
            try {
                system.init();
            } catch (Exception e) {
                Lepton.LOG.error("System {} failed to initialise", system.name, e);
            }
        }
    }

    public static void ready() {
        for (LeptonSystem system : ORDER) {
            try {
                system.ready();
            } catch (Exception e) {
                Lepton.LOG.error("System {} failed during ready()", system.name, e);
            }
        }
    }

    public static void load() {
        for (LeptonSystem system : ORDER) {
            Path file = folder().resolve(system.name + ".json");
            if (!Files.exists(file)) continue;

            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                JsonObject json = GSON.fromJson(reader, JsonObject.class);
                if (json != null) system.fromJson(json);
            } catch (Exception e) {
                Lepton.LOG.error("Could not load {}.json, keeping defaults", system.name, e);
            }
        }
    }

    public static void save() {
        for (LeptonSystem system : ORDER) {
            Path file = folder().resolve(system.name + ".json");

            try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                GSON.toJson(system.toJson(), writer);
            } catch (Exception e) {
                Lepton.LOG.error("Could not save {}.json", system.name, e);
            }
        }
    }
}
