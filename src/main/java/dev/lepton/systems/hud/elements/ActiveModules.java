package dev.lepton.systems.hud.elements;

import dev.lepton.gui.theme.Theme;
import dev.lepton.settings.BoolSetting;
import dev.lepton.settings.EnumSetting;
import dev.lepton.systems.hud.HudElement;
import dev.lepton.systems.modules.Module;
import dev.lepton.systems.modules.Modules;
import dev.lepton.utils.render.Renderer2D;
import dev.lepton.utils.render.color.Color;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** The classic arraylist: every active module, stacked and colour-graded. */
public class ActiveModules extends HudElement {
    public enum Sort {
        /** Longest line first -- gives the block its familiar stepped edge. */
        Width,
        Alphabetical
    }

    private final EnumSetting<Sort> sort = sgGeneral.add(new EnumSetting.Builder<Sort>()
        .name("sort")
        .description("Order of the list.")
        .defaultValue(Sort.Width)
        .build());

    private final BoolSetting showInfo = sgGeneral.add(new BoolSetting.Builder()
        .name("show-info")
        .description("Show each module's extra detail, like its mode or range.")
        .defaultValue(true)
        .build());

    private final BoolSetting gradient = sgGeneral.add(new BoolSetting.Builder()
        .name("gradient")
        .description("Fade from the accent colour down the list.")
        .defaultValue(true)
        .build());

    private final BoolSetting background = sgGeneral.add(new BoolSetting.Builder()
        .name("background")
        .description("Draw a panel behind the list.")
        .defaultValue(false)
        .build());

    public ActiveModules() {
        super("ActiveModules", Anchor.TopRight, 4, 4);
    }

    private List<Module> active() {
        Modules modules = Modules.get();
        if (modules == null) return List.of();

        List<Module> list = new ArrayList<>(modules.getActive());

        if (sort.get() == Sort.Width) {
            list.sort(Comparator.comparingInt((Module m) -> Renderer2D.textWidth(lineFor(m))).reversed());
        } else {
            list.sort(Comparator.comparing(m -> m.title));
        }

        return list;
    }

    private String lineFor(Module module) {
        String info = showInfo.get() ? module.getInfoString() : null;
        return info == null ? module.title : module.title + " " + info;
    }

    @Override
    public double width() {
        double widest = 0;

        for (Module module : active()) {
            widest = Math.max(widest, Renderer2D.textWidth(lineFor(module)));
        }

        return widest + 4;
    }

    @Override
    public double height() {
        return Math.max(1, active().size()) * (Renderer2D.textHeight() + 2);
    }

    @Override
    public void render(Renderer2D r, Theme theme, double x, double y) {
        List<Module> list = active();
        if (list.isEmpty()) return;

        double rowHeight = Renderer2D.textHeight() + 2;

        if (background.get()) {
            r.rect(x - 2, y, width() + 4, list.size() * rowHeight, theme.background);
        }

        for (int i = 0; i < list.size(); i++) {
            Module module = list.get(i);

            String title = module.title;
            String info = showInfo.get() ? module.getInfoString() : null;

            double rowY = y + i * rowHeight;
            Color color = colorFor(theme, i, list.size());

            // Right-aligned lists grow leftward from the screen edge.
            if (rightAligned()) {
                double lineWidth = Renderer2D.textWidth(lineFor(module));
                double startX = x + width() - 4 - lineWidth;

                r.text(title, startX, rowY, color);
                if (info != null) r.text(info, startX + Renderer2D.textWidth(title + " "), rowY, theme.textDim);
            } else {
                r.text(title, x, rowY, color);
                if (info != null) r.text(info, x + Renderer2D.textWidth(title + " "), rowY, theme.textDim);
            }
        }
    }

    private Color colorFor(Theme theme, int index, int total) {
        if (!gradient.get() || total <= 1) return theme.accent;

        return theme.accent.lerp(theme.accentBright, index / (double) (total - 1));
    }
}
