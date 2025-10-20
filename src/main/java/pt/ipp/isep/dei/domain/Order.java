package pt.ipp.isep.dei.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Order {
    public final String orderId;       // agora String para suportar ORD00001
    public final int priority;
    public final LocalDate dueDate;
    public final List<OrderLine> lines = new ArrayList<>();

    public Order(String orderId, int priority, LocalDate dueDate) {
        this.orderId = orderId;
        this.priority = priority;
        this.dueDate = dueDate;
    }

    @Override
    public String toString() {
        return String.format("Order{id=%s, priority=%d, due=%s, lines=%d}",
                orderId, priority, dueDate, lines.size());
    }
}
