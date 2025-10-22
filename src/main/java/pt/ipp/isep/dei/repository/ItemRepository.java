package pt.ipp.isep.dei.repository;


import pt.ipp.isep.dei.domain.Item;

import java.util.*;

public class ItemRepository {
    private final Map<String, Item> itemsBySku = new HashMap<>();

    public void addItem(Item item) {
        itemsBySku.put(item.getSku(), item);
    }

    public Item getItemBySku(String sku) {
        Item item = itemsBySku.get(sku);
        if (item == null) {
            throw new IllegalArgumentException("Item with SKU " + sku + " not found in repository");
        }
        return item;
    }

    public boolean containsSku(String sku) {

        return itemsBySku.containsKey(sku);
    }

    public List<Item> getAllItems() {

        return new ArrayList<>(itemsBySku.values());
    }
}