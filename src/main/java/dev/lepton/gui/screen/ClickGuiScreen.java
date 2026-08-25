package dev.lepton.gui.screen;

import dev.lepton.SinglePlayerGate;
import dev.lepton.gui.Icons;
import dev.lepton.gui.theme.Theme;
import dev.lepton.gui.widget.Panel;
import dev.lepton.gui.widget.SettingRow;
import dev.lepton.systems.config.Config;
import dev.lepton.systems.modules.Categories;
import dev.lepton.systems.modules.Category;
import dev.lepton.systems.modules.Modules;
import dev.lepton.utils.render.Renderer2D;
import dev.lepton.utils.render.color.Color;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class ClickGuiScreen extends Screen {
    /** Panels live longer than the screen so drag positions survive closing it. */
    private static final List<Panel> PANELS = new ArrayList<>();
    private static boolean laidOut;

    private static final double HEADER_HEIGHT = 22;

    private String search = "";
    private boolean searchFocused;

    public ClickGuiScreen() {
        super(Text.literal("Lepton"));
    }

    @Override
    protected void init() {
        if (!laidOut) {
            layout();
            laidOut = true;
        }
    }

    /** Rebuilds panels from scratch. Called once, and again if modules are re-registered. */
    public static void layout() {
        PANELS.clear();

        Modules modules = Modules.get();
        if (modules == null) return;

        double x = 12;

        for (Category category : Categories.all()) {
            PANELS.add(new Panel(category, modules.getCategory(category), x, HEADER_HEIGHT + 14));
            x += Panel.WIDTH + 8;
        }
    }

    public static void invalidate() {
        laidOut = false;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Deliberately not calling super -- we want a flat configurable dim, not the
        // vanilla blur, so the world stays readable behind the panels.
        Config config = Config.get();
        int dim = config == null ? 96 : config.backgroundBlur.get();

        context.fill(0, 0, this.width, this.height, new Color(0, 0, 0, dim).packed());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);

        Renderer2D r = Renderer2D.of(context);
        Theme theme = theme();

        renderHeader(r, theme, mouseX, mouseY);

        for (Panel panel : PANELS) {
            panel.render(r, theme, mouseX, mouseY, search);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderHeader(Renderer2D r, Theme theme, double mouseX, double mouseY) {
        r.rect(0, 0, this.width, HEADER_HEIGHT, theme.surface);
        r.rect(0, HEADER_HEIGHT - 1, this.width, 1, theme.accent);

        // Mark + wordmark.
        Icons.draw(r, Icons.LOGO, 10, (HEADER_HEIGHT - 11) / 2.0, 1, theme.accent);

        double textY = (HEADER_HEIGHT - Renderer2D.textHeight()) / 2.0 + 1;
        r.text("LEPTON", 27, textY, theme.text);

        double afterName = 27 + Renderer2D.textWidth("LEPTON") + 8;
        r.rect(afterName, 5, 1, HEADER_HEIGHT - 11, theme.outline);

        // Gate status -- the single most important thing to surface in this UI.
        String denial = SinglePlayerGate.reason();
        boolean allowed = denial == null;

        String status = allowed ? "SINGLEPLAYER" : denial.toUpperCase();
        Color statusColor = allowed ? theme.positive : theme.negative;

        double dotX = afterName + 9;
        r.rect(dotX, HEADER_HEIGHT / 2.0 - 2, 4, 4, statusColor);
        r.text(status, dotX + 9, textY, statusColor);

        // Search box, right-aligned.
        double boxWidth = 130;
        double boxX = this.width - boxWidth - 10;
        double boxY = 4;
        double boxHeight = HEADER_HEIGHT - 9;

        r.rect(boxX, boxY, boxWidth, boxHeight, theme.background);
        r.outline(boxX, boxY, boxWidth, boxHeight, searchFocused ? theme.accent : theme.outline);

        Icons.draw(r, Icons.SEARCH, boxX + 5, boxY + (boxHeight - 7) / 2.0, 1, theme.textDim);

        String shown = search.isEmpty() && !searchFocused ? "Search modules" : search + (searchFocused ? "_" : "");
        Color searchColor = search.isEmpty() && !searchFocused ? theme.textDisabled : theme.text;

        r.text(shown, boxX + 16, boxY + (boxHeight - Renderer2D.textHeight()) / 2.0 + 1, searchColor);
    }

    private Theme theme() {
        Config config = Config.get();
        return config == null ? dev.lepton.gui.theme.Themes.LEPTON_BLUE : config.theme();
    }

    // -- input ----------------------------------------------------------------

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();

        // A capturing keybind row swallows the next mouse button.
        for (Panel panel : PANELS) {
            if (panel.isCapturing() && panel.mouseButtonBind(button)) return true;
        }

        double boxWidth = 130;
        double boxX = this.width - boxWidth - 10;

        if (SettingRow.contains(mouseX, mouseY, boxX, 4, boxWidth, HEADER_HEIGHT - 9)) {
            searchFocused = true;
            return true;
        }

        searchFocused = false;

        // Front-to-back so overlapping panels behave the way they look.
        for (int i = PANELS.size() - 1; i >= 0; i--) {
            if (PANELS.get(i).mouseClicked(mouseX, mouseY, button, search)) return true;
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(Click click) {
        for (Panel panel : PANELS) panel.mouseReleased();
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        for (Panel panel : PANELS) panel.mouseDragged(click.x(), click.y());
        return true;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int key = input.key();

        for (Panel panel : PANELS) {
            if (panel.isCapturing() && panel.keyPressed(key, input.modifiers())) return true;
        }

        if (searchFocused) {
            if (key == 256) { // escape
                if (search.isEmpty()) searchFocused = false;
                else search = "";
                return true;
            }

            if (key == 259 && !search.isEmpty()) { // backspace
                search = search.substring(0, search.length() - 1);
                return true;
            }

            if (key == 257) { // enter
                searchFocused = false;
                return true;
            }
        }

        for (Panel panel : PANELS) {
            if (panel.keyPressed(key, input.modifiers())) return true;
        }

        Config config = Config.get();
        if (config != null && config.guiKeybind.get().matches(true, key)) {
            close();
            return true;
        }

        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharInput input) {
        char c = (char) input.codepoint();

        for (Panel panel : PANELS) {
            if (panel.isCapturing() && panel.charTyped(c)) return true;
        }

        if (searchFocused) {
            search += c;
            return true;
        }

        for (Panel panel : PANELS) {
            if (panel.charTyped(c)) return true;
        }

        return super.charTyped(input);
    }
}
