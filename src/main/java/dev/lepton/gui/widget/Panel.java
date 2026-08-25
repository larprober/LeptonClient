package dev.lepton.gui.widget;

import dev.lepton.gui.Icons;
import dev.lepton.gui.theme.Theme;
import dev.lepton.systems.modules.Category;
import dev.lepton.systems.modules.Module;
import dev.lepton.utils.render.Renderer2D;

import java.util.ArrayList;
import java.util.List;

/** A draggable, collapsible column of modules for one category. */
public class Panel {
    public static final double WIDTH = 118;
    public static final double HEADER_HEIGHT = 18;

    public final Category category;
    public double x, y;
    public boolean collapsed;

    private boolean dragging;
    private double dragOffsetX, dragOffsetY;

    private final List<ModuleButton> buttons = new ArrayList<>();

    public Panel(Category category, List<Module> modules, double x, double y) {
        this.category = category;
        this.x = x;
        this.y = y;

        for (Module module : modules) buttons.add(new ModuleButton(module));
    }

    public List<ModuleButton> buttons() {
        return buttons;
    }

    public double height() {
        double height = HEADER_HEIGHT;
        if (collapsed) return height;

        for (ModuleButton button : buttons) height += button.height();
        return height;
    }

    // -- rendering ------------------------------------------------------------

    public void render(Renderer2D r, Theme theme, double mouseX, double mouseY, String filter) {
        boolean headerHovered = SettingRow.contains(mouseX, mouseY, x, y, WIDTH, HEADER_HEIGHT);

        // Header: accent-tinted so each column reads as one unit.
        r.rect(x, y, WIDTH, HEADER_HEIGHT, headerHovered ? theme.accentDeep : theme.surfaceRaised);
        r.rect(x, y + HEADER_HEIGHT - 1, WIDTH, 1, theme.accent);

        Icons.draw(r, Icons.forCategory(category.name), x + 6, y + (HEADER_HEIGHT - 9) / 2.0, 1, theme.accent);

        double textY = y + (HEADER_HEIGHT - Renderer2D.textHeight()) / 2.0 + 1;
        r.text(category.name, x + 20, textY, theme.text);

        String[] chevron = collapsed ? Icons.CHEVRON_RIGHT : Icons.CHEVRON_DOWN;
        Icons.draw(r, chevron, x + WIDTH - 13, y + (HEADER_HEIGHT - 7) / 2.0, 1, theme.textDim);

        if (collapsed) return;

        double rowY = y + HEADER_HEIGHT;

        for (ModuleButton button : buttons) {
            if (!matches(button, filter)) continue;

            button.x = x;
            button.y = rowY;
            button.width = WIDTH;
            button.render(r, theme, mouseX, mouseY);
            rowY += button.height();
        }

        r.outline(x, y, WIDTH, rowY - y, theme.outline);
    }

    private boolean matches(ModuleButton button, String filter) {
        if (filter == null || filter.isEmpty()) return true;
        return button.module.name.toLowerCase().contains(filter.toLowerCase());
    }

    // -- input ----------------------------------------------------------------

    public boolean mouseClicked(double mouseX, double mouseY, int button, String filter) {
        if (SettingRow.contains(mouseX, mouseY, x, y, WIDTH, HEADER_HEIGHT)) {
            if (button == 0) {
                dragging = true;
                dragOffsetX = mouseX - x;
                dragOffsetY = mouseY - y;
            } else if (button == 1) {
                collapsed = !collapsed;
            }

            return true;
        }

        if (collapsed) return false;

        for (ModuleButton moduleButton : buttons) {
            if (!matches(moduleButton, filter)) continue;
            if (moduleButton.mouseClicked(mouseX, mouseY, button)) return true;
        }

        return false;
    }

    public void mouseReleased() {
        dragging = false;
        for (ModuleButton button : buttons) button.mouseReleased();
    }

    public void mouseDragged(double mouseX, double mouseY) {
        if (dragging) {
            x = mouseX - dragOffsetX;
            y = mouseY - dragOffsetY;
            return;
        }

        for (ModuleButton button : buttons) button.mouseDragged(mouseX);
    }

    public boolean keyPressed(int key, int modifiers) {
        for (ModuleButton button : buttons) {
            if (button.keyPressed(key, modifiers)) return true;
        }
        return false;
    }

    public boolean charTyped(char c) {
        for (ModuleButton button : buttons) {
            if (button.charTyped(c)) return true;
        }
        return false;
    }

    public boolean mouseButtonBind(int button) {
        for (ModuleButton moduleButton : buttons) {
            if (moduleButton.mouseButtonBind(button)) return true;
        }
        return false;
    }

    public boolean isCapturing() {
        for (ModuleButton button : buttons) if (button.isCapturing()) return true;
        return false;
    }
}
