package pt.ipp.isep.dei.repository;

import pt.ipp.isep.dei.domain.Item;
import pt.ipp.isep.dei.domain.Order;
import pt.ipp.isep.dei.domain.OrderLine;
import java.util.*;

public class ItemRepository {
    private final Map<String, Item> itemsBySku = new HashMap<>();
    private final List<Order> orders = new ArrayList<>();

    public void addItem(Item item) {
        Objects.requireNonNull(item);
        itemsBySku.put(item.getSku(), item);
    }

    public Item getItemBySku(String sku) {
        return itemsBySku.get(sku);
    }

    public List<Order> getAllOrders() {
        return new ArrayList<>(orders);
    }

    public void addOrder(Order order) {
        this.orders.add(order);
    }

    /**
     * Calcula o peso total de uma Ordem em Toneladas (USLP08)
     */
    public double calculateOrderWeightTon(Order order) {
        double weightKg = 0;
        for (OrderLine line : order.lines) {
            Item item = getItemBySku(line.sku);
            if (item != null) {
                weightKg += (line.requestedQty * item.getUnitWeight());
            }
        }
        return weightKg / 1000.0;
    }
}