package dev.lepton.systems.modules;

public class Category implements Comparable<Category> {
    public final String name;
    public final int order;

    public Category(String name, int order) {
        this.name = name;
        this.order = order;
    }

    @Override
    public int compareTo(Category other) {
        return Integer.compare(order, other.order);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Category c && c.name.equals(name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
