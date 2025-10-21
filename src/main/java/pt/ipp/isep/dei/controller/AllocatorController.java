package pt.ipp.isep.dei.controller;

import pt.ipp.isep.dei.domain.*;
import pt.ipp.isep.dei.repository.ItemRepository;
import java.util.List;

public class AllocatorController {

        private final OrderAllocator orderAllocator;
        private final ItemRepository itemRepository;

        public AllocatorController(ItemRepository itemRepository) {
            this.itemRepository = new ItemRepository();
            this.orderAllocator = new OrderAllocator();
        }

    public AllocatorController(OrderAllocator orderAllocator, ItemRepository itemRepository) {
        this.orderAllocator = orderAllocator;
        this.itemRepository = itemRepository;
    }

    public AllocationResult planOrderFulfillment(List<Order> orders,
                                                     List<Box> inventory,
                                                     OrderAllocator.Mode mode) {

            // Validar inputs
            if (orders == null || orders.isEmpty()) {
                throw new IllegalArgumentException("Orders list cannot be null or empty");
            }
            if (inventory == null) {
                throw new IllegalArgumentException("Inventory cannot be null");
            }

            // Executar alocação
            return orderAllocator.allocateOrders(orders, inventory, mode);
        }

        // Método overload com modo STRICT como default
        public AllocationResult planOrderFulfillment(List<Order> orders, List<Box> inventory) {
            return planOrderFulfillment(orders, inventory, OrderAllocator.Mode.STRICT);
        }

        // Método para obter resumo por status
        public String getEligibilitySummary(AllocationResult result) {
            long eligible = result.eligibilityList.stream()
                    .filter(e -> e.status == Status.ELIGIBLE)
                    .count();
            long partial = result.eligibilityList.stream()
                    .filter(e -> e.status == Status.PARTIAL)
                    .count();
            long undispatchable = result.eligibilityList.stream()
                    .filter(e -> e.status == Status.UNDISPATCHABLE)
                    .count();

            return String.format("Eligibility Summary: ELIGIBLE=%d, PARTIAL=%d, UNDISPATCHABLE=%d, Total Allocations=%d",
                    eligible, partial, undispatchable, result.allocations.size());
        }

        // Método para calcular peso total das alocações
        public double getTotalAllocatedWeight(AllocationResult result) {
            return result.allocations.stream()
                    .mapToDouble(a -> a.weight)
                    .sum();
        }
    }

