package pt.ipp.isep.dei.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a client order in the inventory management system.
 *
 * <p>An order includes identifying information, a priority level, a due date,
 * and a list of associated order lines.</p>
 */
public class Order {
    // Unique identifier for the order.
    public final String orderId;
    // Priority level (higher number usually means higher priority).
    public final int priority;
    // Date by which the order must be fulfilled.
    public final LocalDate dueDate;
    // List of items (lines) requested in this order.
    public final List<OrderLine> lines = new ArrayList<>();

    /**
     * Constructs a new Order instance.
     *
     * @param orderId The unique ID of the order.
     * @param priority The priority level of the order.
     * @param dueDate The due date of the order.
     */
    public Order(String orderId, int priority, LocalDate dueDate) {
        this.orderId = orderId;
        this.priority = priority;
        this.dueDate = dueDate;
    }

    /**
     * Returns a string representation of the Order.
     *
     * @return A formatted string showing the ID, priority, due date, and number of lines.
     */
    @Override
    public String toString() {
        return String.format("Order{id=%s, priority=%d, due=%s, lines=%d}",
                orderId, priority, dueDate, lines.size());
    }
}