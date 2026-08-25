package dev.lepton.systems.hud.elements;

import dev.lepton.Lepton;
import dev.lepton.SinglePlayerGate;
import dev.lepton.gui.Icons;
import dev.lepton.gui.theme.Theme;
import dev.lepton.settings.BoolSetting;
import dev.lepton.systems.hud.HudElement;
import dev.lepton.utils.render.Renderer2D;
import dev.lepton.utils.render.color.Color;

public class Watermark extends HudElement {
    private final BoolSetting showVersion = sgGeneral.add(new BoolSetting.Builder()
        .name("show-version")
        .description("Append the version number.")
        .defaultValue(true)
        .build());

    private final BoolSetting showGate = sgGeneral.add(new BoolSetting.Builder()
        .name("show-gate-status")
        .description("Show whether Lepton is currently allowed to run.")
        .defaultValue(true)
        .build());

    public Watermark() {
        super("Watermark", Anchor.TopLeft, 4, 4);
    }

    private String label() {
        return showVersion.get() ? Lepton.NAME_SHORT + " " + Lepton.VERSION : Lepton.NAME_SHORT;
    }

    @Override
    public double width() {
        double width = 13 + Renderer2D.textWidth(label());
        if (showGate.get()) width += 8 + Renderer2D.textWidth(gateLabel());
        return width + 8;
    }

    @Override
    public double height() {
        return 14;
    }

    @Override
    public void render(Renderer2D r, Theme theme, double x, double y) {
        r.rect(x, y, width(), height(), theme.background);
        r.rect(x, y, 2, height(), theme.accent);

        double textY = y + (height() - Renderer2D.textHeight()) / 2.0;

        Icons.draw(r, Icons.LOGO, x + 5, y + 1.5, 1, theme.accent);
        r.text(label(), x + 18, textY, theme.text);

        if (!showGate.get()) return;

        boolean allowed = SinglePlayerGate.isAllowed();
        Color color = allowed ? theme.positive : theme.negative;

        double statusX = x + 18 + Renderer2D.textWidth(label()) + 6;
        r.text(gateLabel(), statusX, textY, color);
    }

    private String gateLabel() {
        return SinglePlayerGate.isAllowed() ? "SP" : "OFF";
    }
}
