package pt.ipp.isep.dei.controller;

import pt.ipp.isep.dei.domain.*;
import pt.ipp.isep.dei.repository.ItemRepository;
import java.util.List;

/**
 * Controller responsible for handling order allocation logic.
 * <p>
 * This class acts as an intermediary between the UI layer and the
 * {@link OrderAllocator} domain service. It orchestrates the process
 * of planning order fulfillment and provides methods to summarize
 * the allocation results.
 * </p>
 */
public class AllocatorController {

    /**
     * The domain service responsible for the core allocation logic.
     */
    private final OrderAllocator orderAllocator;
    /**
     * The repository for accessing item data (e.g., for weight calculations).
     */
    private final ItemRepository itemRepository;

    /**
     * Constructs an AllocatorController with a default {@link OrderAllocator}
     * and a new {@link ItemRepository}.
     *
     * @param itemRepository The repository for item data. (Note: This constructor seems to re-initialize it)
     */
    public AllocatorController(ItemRepository itemRepository) {
        // FIXME: This constructor re-initializes the repository passed as a parameter.
        // It should probably be: this.itemRepository = itemRepository;
        this.itemRepository = new ItemRepository();
        this.orderAllocator = new OrderAllocator();
    }

    /**
     * Constructs an AllocatorController with specific dependencies.
     * This is the preferred constructor for dependency injection.
     *
     * @param orderAllocator The order allocation service.
     * @param itemRepository The repository for item data.
     */
    public AllocatorController(OrderAllocator orderAllocator, ItemRepository itemRepository) {
        this.orderAllocator = orderAllocator;
        this.itemRepository = itemRepository;
    }

    /**
     * Plans the fulfillment of a list of orders against the current inventory state.
     *
     * @param orders    The list of {@link Order} objects to be fulfilled.
     * @param inventory The list of {@link Box} objects representing the current inventory.
     * @param mode      The allocation mode ({@link OrderAllocator.Mode#STRICT} or {@link OrderAllocator.Mode#PARTIAL}).
     * @return An {@link AllocationResult} object containing the generated allocations and eligibility reports.
     * @throws IllegalArgumentException if the orders list is null/empty or the inventory is null.
     */
    public AllocationResult planOrderFulfillment(List<Order> orders,
                                                 List<Box> inventory,
                                                 OrderAllocator.Mode mode) {

        // Validate inputs
        if (orders == null || orders.isEmpty()) {
            throw new IllegalArgumentException("Orders list cannot be null or empty");
        }
        if (inventory == null) {
            throw new IllegalArgumentException("Inventory cannot be null");
        }

        // Execute allocation
        return orderAllocator.allocateOrders(orders, inventory, mode);
    }

    /**
     * Overloaded method to plan order fulfillment using the default {@link OrderAllocator.Mode#STRICT} mode.
     *
     * @param orders    The list of {@link Order} objects to be fulfilled.
     * @param inventory The list of {@link Box} objects representing the current inventory.
     * @return An {@link AllocationResult} object.
     * @throws IllegalArgumentException if inputs are invalid.
     */
    public AllocationResult planOrderFulfillment(List<Order> orders, List<Box> inventory) {
        return planOrderFulfillment(orders, inventory, OrderAllocator.Mode.STRICT);
    }

    /**
     * Generates a human-readable summary of the allocation eligibility results.
     *
     * @param result The {@link AllocationResult} to summarize.
     * @return A string formatted with the counts of ELIGIBLE, PARTIAL, and UNDISPATCHABLE lines.
     */
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

    /**
     * Calculates the total weight of all generated allocations in an {@link AllocationResult}.
     * This relies on the 'weight' field of each {@link Allocation} being pre-calculated.
     *
     * @param result The {@link AllocationResult} to analyze.
     * @return The sum of weights for all allocations, in kilograms.
     */
    public double getTotalAllocatedWeight(AllocationResult result) {
        return result.allocations.stream()
                .mapToDouble(a -> a.weight)
                .sum();
    }
}