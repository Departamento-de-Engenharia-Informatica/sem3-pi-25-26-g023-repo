package pt.ipp.isep.dei.domain;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Order {
        public final int orderId;
        public final int priority;
        public final LocalDate dueDate;
        public final List<OrderLine> lines = new ArrayList<>();

        public Order(int orderId, int priority, LocalDate dueDate) {
            this.orderId = orderId;
            this.priority = priority;
            this.dueDate = dueDate;
        }
    }
}
