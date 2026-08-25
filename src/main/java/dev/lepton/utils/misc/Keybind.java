package dev.lepton.utils.misc;

import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.Window;
import org.lwjgl.glfw.GLFW;

/** A key or mouse button binding. {@code value == -1} means unbound. */
public class Keybind {
    private boolean isKey;
    private int value;

    private Keybind(boolean isKey, int value) {
        this.isKey = isKey;
        this.value = value;
    }

    public static Keybind none() {
        return new Keybind(true, -1);
    }

    public static Keybind fromKey(int key) {
        return new Keybind(true, key);
    }

    public static Keybind fromButton(int button) {
        return new Keybind(false, button);
    }

    public boolean isSet() {
        return value != -1;
    }

    public boolean isKey() {
        return isKey;
    }

    public int getValue() {
        return value;
    }

    public void set(boolean isKey, int value) {
        this.isKey = isKey;
        this.value = value;
    }

    public void set(Keybind other) {
        set(other.isKey, other.value);
    }

    public void clear() {
        set(true, -1);
    }

    public boolean matches(boolean isKey, int value) {
        return isSet() && this.isKey == isKey && this.value == value;
    }

    /** Whether the bound key/button is currently held down. */
    public boolean isPressed() {
        if (!isSet()) return false;

        Window window = MinecraftClient.getInstance().getWindow();

        return isKey
            ? InputUtil.isKeyPressed(window, value)
            : GLFW.glfwGetMouseButton(window.getHandle(), value) == GLFW.GLFW_PRESS;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("isKey", isKey);
        json.addProperty("value", value);
        return json;
    }

    public static Keybind fromJson(JsonObject json) {
        if (json == null || !json.has("value")) return none();

        boolean isKey = !json.has("isKey") || json.get("isKey").getAsBoolean();
        return new Keybind(isKey, json.get("value").getAsInt());
    }

    @Override
    public String toString() {
        if (!isSet()) return "None";

        if (isKey) {
            String name = InputUtil.Type.KEYSYM.createFromCode(value).getLocalizedText().getString();
            return name == null || name.isEmpty() ? "Key " + value : name;
        }

        return switch (value) {
            case 0 -> "Left Click";
            case 1 -> "Right Click";
            case 2 -> "Middle Click";
            default -> "Mouse " + (value + 1);
        };
    }
}
