package pt.ipp.isep.dei.repository;

import pt.ipp.isep.dei.domain.Item;

import java.util.*;

/**
 * Repository class responsible for managing {@link Item} objects in memory.
 * <p>
 * This repository provides a lightweight, map-based storage mechanism where
 * each {@code Item} is indexed by its unique SKU (Stock Keeping Unit).
 * It does not persist data to a database — instead, it serves as an
 * in-memory collection suitable for testing, caching, or temporary storage.
 */
public class ItemRepository {

    /** Internal map storing items by their SKU identifiers. */
    private final Map<String, Item> itemsBySku = new HashMap<>();

    /**
     * Adds a new item to the repository.
     * <p>
     * If another item with the same SKU already exists, it will be replaced.
     *
     * @param item the {@link Item} to be added to the repository
     * @throws NullPointerException if the provided item or its SKU is {@code null}
     */
    public void addItem(Item item) {
        Objects.requireNonNull(item, "Item cannot be null");
        Objects.requireNonNull(item.getSku(), "Item SKU cannot be null");
        itemsBySku.put(item.getSku(), item);
    }

    /**
     * Retrieves an item from the repository based on its SKU.
     * <p>
     * Throws an {@link IllegalArgumentException} if no item with the given SKU exists.
     *
     * @param sku the unique identifier (SKU) of the item
     * @return the corresponding {@link Item}
     * @throws IllegalArgumentException if no item with the given SKU exists
     * @throws NullPointerException if {@code sku} is {@code null}
     */
    public Item getItemBySku(String sku) {
        Objects.requireNonNull(sku, "SKU cannot be null");
        Item item = itemsBySku.get(sku);
        if (item == null) {
            throw new IllegalArgumentException("Item with SKU " + sku + " not found in repository");
        }
        return item;
    }

    /**
     * Checks whether the repository contains an item with the specified SKU.
     *
     * @param sku the SKU to check
     * @return {@code true} if the repository contains an item with the given SKU; {@code false} otherwise
     * @throws NullPointerException if {@code sku} is {@code null}
     */
    public boolean containsSku(String sku) {
        Objects.requireNonNull(sku, "SKU cannot be null");
        return itemsBySku.containsKey(sku);
    }

    /**
     * Retrieves all items currently stored in the repository.
     * <p>
     * The returned list is a shallow copy — modifications to it will not affect
     * the internal storage of the repository.
     *
     * @return a list of all {@link Item} objects stored in the repository
     */
    public List<Item> getAllItems() {
        return new ArrayList<>(itemsBySku.values());
    }
}
