package dev.lepton.gui.widget;

import dev.lepton.gui.Icons;
import dev.lepton.gui.theme.Theme;
import dev.lepton.settings.Setting;
import dev.lepton.settings.SettingGroup;
import dev.lepton.systems.modules.Module;
import dev.lepton.utils.render.Renderer2D;
import dev.lepton.utils.render.color.Color;

import java.util.ArrayList;
import java.util.List;

/** A module row inside a category panel, plus its expandable settings list. */
public class ModuleButton {
    public static final double ROW_HEIGHT = 15;

    public final Module module;
    public double x, y, width;
    public boolean expanded;

    private final List<SettingRow> rows = new ArrayList<>();

    public ModuleButton(Module module) {
        this.module = module;

        for (SettingGroup group : module.settings) {
            for (Setting<?> setting : group) rows.add(new SettingRow(setting));
        }
    }

    public double height() {
        double height = ROW_HEIGHT;
        if (!expanded) return height;

        for (SettingRow row : visibleRows()) height += row.height();
        return height;
    }

    private List<SettingRow> visibleRows() {
        List<SettingRow> visible = new ArrayList<>();
        for (SettingRow row : rows) if (row.setting.isVisible()) visible.add(row);
        return visible;
    }

    public boolean hasSettings() {
        return !rows.isEmpty();
    }

    // -- rendering ------------------------------------------------------------

    public void render(Renderer2D r, Theme theme, double mouseX, double mouseY) {
        boolean active = module.isActive();
        boolean hovered = SettingRow.contains(mouseX, mouseY, x, y, width, ROW_HEIGHT);

        r.rect(x, y, width, ROW_HEIGHT, theme.moduleFill(active, hovered));

        // Active modules get an accent bar down the left edge -- readable at a glance
        // even when the fill is subtle.
        if (active) r.rect(x, y, 2, ROW_HEIGHT, theme.accentBright);

        double textY = y + (ROW_HEIGHT - Renderer2D.textHeight()) / 2.0 + 1;
        Color textColor = active ? theme.text : theme.textDim;

        r.text(module.title, x + 7, textY, textColor);

        if (module.favourite) {
            Icons.draw(r, Icons.STAR, x + width - 24, y + (ROW_HEIGHT - 7) / 2.0, 1, theme.warning);
        }

        if (hasSettings()) {
            String[] chevron = expanded ? Icons.CHEVRON_DOWN : Icons.CHEVRON_RIGHT;
            Icons.draw(r, chevron, x + width - 12, y + (ROW_HEIGHT - 7) / 2.0, 1, active ? theme.text : theme.textDisabled);
        }

        if (!expanded) return;

        double rowY = y + ROW_HEIGHT;

        for (SettingRow row : visibleRows()) {
            row.x = x;
            row.y = rowY;
            row.width = width;
            row.render(r, theme, mouseX, mouseY);
            rowY += row.height();
        }

        // A hairline under the settings block separates it from the next module.
        r.rect(x, rowY - 1, width, 1, theme.outline);
    }

    // -- input ----------------------------------------------------------------

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (expanded) {
            for (SettingRow row : visibleRows()) {
                if (row.mouseClicked(mouseX, mouseY, button)) return true;
            }
        }

        if (!SettingRow.contains(mouseX, mouseY, x, y, width, ROW_HEIGHT)) return false;

        if (button == 0) {
            module.toggle();
            return true;
        }

        if (button == 1 && hasSettings()) {
            expanded = !expanded;
            return true;
        }

        if (button == 2) {
            module.favourite = !module.favourite;
            return true;
        }

        return false;
    }

    public void mouseReleased() {
        for (SettingRow row : rows) row.mouseReleased();
    }

    public void mouseDragged(double mouseX) {
        if (!expanded) return;
        for (SettingRow row : visibleRows()) row.mouseDragged(mouseX);
    }

    public boolean keyPressed(int key, int modifiers) {
        if (!expanded) return false;

        for (SettingRow row : visibleRows()) {
            if (row.keyPressed(key, modifiers)) return true;
        }

        return false;
    }

    public boolean charTyped(char c) {
        if (!expanded) return false;

        for (SettingRow row : visibleRows()) {
            if (row.charTyped(c)) return true;
        }

        return false;
    }

    public boolean mouseButtonBind(int button) {
        if (!expanded) return false;

        for (SettingRow row : visibleRows()) {
            if (row.mouseButtonBind(button)) return true;
        }

        return false;
    }

    public boolean isCapturing() {
        for (SettingRow row : rows) if (row.isCapturing()) return true;
        return false;
    }
}
