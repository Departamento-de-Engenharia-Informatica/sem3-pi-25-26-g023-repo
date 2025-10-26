package pt.ipp.isep.dei.domain;

/**
 * Represents an Item from inventory
 */
public class Item {
    private final String sku;
    private final String name;
    private final String category;
    private final String unit;
    private final double unitWeight;

    public Item(String sku, String name, String category, String unit, double unitWeight) {
        this.sku = sku;
        this.name = name;
        this.category = category;
        this.unit = unit;
        this.unitWeight = unitWeight;
    }

    public String getSku() {

        return sku;
    }
    public String getName() {

        return name;
    }
    public String getCategory() {

        return category;
    }
    public String getUnit() {

        return unit;
    }
    public double getUnitWeight() {

        return unitWeight;
    }

    @Override
    public String toString() {

        return String.format("%s (%s) - %.2f %s", name, category, unitWeight, unit);
    }
}