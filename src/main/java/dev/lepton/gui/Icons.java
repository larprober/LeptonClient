package dev.lepton.gui;

import dev.lepton.utils.render.Renderer2D;
import dev.lepton.utils.render.color.Color;

/**
 * Lepton's icon set, drawn rather than borrowed.
 *
 * <p>Each mark is defined as a small pixel grid ('#' is on) and rendered as filled
 * rectangles, so it stays crisp at any GUI scale instead of blurring like a scaled
 * texture would. Keeping them here means the whole set shares one visual language:
 * 9x9 body, 1px stroke, symmetric where the subject allows it.
 */
public class Icons {
    // -- category marks (9x9) -------------------------------------------------

    /** Crossed blades. */
    public static final String[] COMBAT = {
        "#.......#",
        "##.....##",
        ".##...##.",
        "..##.##..",
        "...###...",
        "..##.##..",
        ".##...##.",
        "##.....##",
        "#.......#"
    };

    /** Upward chevron trail -- travel. */
    public static final String[] MOVEMENT = {
        "....#....",
        "...###...",
        "..##.##..",
        ".##...##.",
        "....#....",
        "...###...",
        "..##.##..",
        ".##...##.",
        "........."
    };

    /** Standing figure. */
    public static final String[] PLAYER = {
        "...###...",
        "...###...",
        ".........",
        ".#######.",
        "....#....",
        "...###...",
        "..##.##..",
        ".##...##.",
        "........."
    };

    /** Open eye. */
    public static final String[] RENDER = {
        ".........",
        "..#####..",
        ".#.....#.",
        "#..###..#",
        "#.##.##.#",
        "#..###..#",
        ".#.....#.",
        "..#####..",
        "........."
    };

    /** Block in isometric. */
    public static final String[] WORLD = {
        "...###...",
        "..##.##..",
        ".##...##.",
        "#########",
        "#...#...#",
        "#...#...#",
        "#...#...#",
        "#########",
        "........."
    };

    /** Radiating star -- the catch-all. */
    public static final String[] MISC = {
        "....#....",
        ".#..#..#.",
        "..#####..",
        ".#######.",
        "##.###.##",
        ".#######.",
        "..#####..",
        ".#..#..#.",
        "....#...."
    };

    // -- UI glyphs (7x7 unless noted) -----------------------------------------

    public static final String[] CHEVRON_RIGHT = {
        "##.....",
        ".##....",
        "..##...",
        "...##..",
        "..##...",
        ".##....",
        "##....."
    };

    public static final String[] CHEVRON_DOWN = {
        ".......",
        "#.....#",
        "##...##",
        ".##.##.",
        "..###..",
        "...#...",
        "......."
    };

    public static final String[] CHECK = {
        ".......",
        "......#",
        ".....##",
        "#...##.",
        "##.##..",
        ".###...",
        "..#...."
    };

    public static final String[] SEARCH = {
        ".####..",
        "#....#.",
        "#....#.",
        "#....#.",
        ".####..",
        "....##.",
        ".....##"
    };

    /** Filled star -- favourite. */
    public static final String[] STAR = {
        "...#...",
        "...#...",
        "#######",
        ".#####.",
        "..###..",
        ".##.##.",
        "##...##"
    };

    public static final String[] CLOSE = {
        "#.....#",
        "##...##",
        ".##.##.",
        "..###..",
        ".##.##.",
        "##...##",
        "#.....#"
    };

    public static final String[] RESET = {
        "..###..",
        ".#...#.",
        "#.....#",
        "#......",
        "#.....#",
        ".#...#.",
        "..###.."
    };

    /**
     * The Lepton mark: a particle inside its orbit. Used for the watermark and
     * the ClickGUI header. 11x11 so the ring has room to read as a circle.
     */
    public static final String[] LOGO = {
        "...#####...",
        "..#.....#..",
        ".#.......#.",
        "#....#....#",
        "#...###...#",
        "#..#####..#",
        "#...###...#",
        "#....#....#",
        ".#.......#.",
        "..#.....#..",
        "...#####..."
    };

    // -- rendering ------------------------------------------------------------

    /** Width of an icon in pixels at the given scale. */
    public static double width(String[] icon, double scale) {
        return icon[0].length() * scale;
    }

    public static double height(String[] icon, double scale) {
        return icon.length * scale;
    }

    /**
     * Draws the mark with its top-left corner at (x, y). Runs of adjacent pixels are
     * merged into single rectangles so a 9x9 mark costs a handful of draw calls, not 81.
     */
    public static void draw(Renderer2D renderer, String[] icon, double x, double y, double scale, Color color) {
        for (int row = 0; row < icon.length; row++) {
            String line = icon[row];
            int col = 0;

            while (col < line.length()) {
                if (line.charAt(col) != '#') {
                    col++;
                    continue;
                }

                int runStart = col;
                while (col < line.length() && line.charAt(col) == '#') col++;

                renderer.rect(
                    x + runStart * scale,
                    y + row * scale,
                    (col - runStart) * scale,
                    scale,
                    color
                );
            }
        }
    }

    /** Draws centred inside the given box. */
    public static void drawCentered(Renderer2D renderer, String[] icon, double centerX, double centerY, double scale, Color color) {
        draw(renderer, icon, centerX - width(icon, scale) / 2.0, centerY - height(icon, scale) / 2.0, scale, color);
    }

    public static String[] forCategory(String categoryName) {
        return switch (categoryName) {
            case "Combat" -> COMBAT;
            case "Movement" -> MOVEMENT;
            case "Player" -> PLAYER;
            case "Render" -> RENDER;
            case "World" -> WORLD;
            default -> MISC;
        };
    }
}
