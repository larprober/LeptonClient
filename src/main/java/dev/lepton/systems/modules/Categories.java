package dev.lepton.systems.modules;

public class Categories {
    public static final Category Combat = new Category("Combat", 0);
    public static final Category Movement = new Category("Movement", 1);
    public static final Category Player = new Category("Player", 2);
    public static final Category Render = new Category("Render", 3);
    public static final Category World = new Category("World", 4);
    public static final Category Misc = new Category("Misc", 5);

    private static final Category[] ALL = { Combat, Movement, Player, Render, World, Misc };

    public static Category[] all() {
        return ALL;
    }

    public static Category byName(String name) {
        for (Category category : ALL) {
            if (category.name.equalsIgnoreCase(name)) return category;
        }
        return null;
    }
}
