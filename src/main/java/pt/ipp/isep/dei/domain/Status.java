package pt.ipp.isep.dei.domain;

/**
 * Represents the allocation status of an order line during order fulfillment.
 *
 * ELIGIBLE - Entire requested quantity can be allocated from inventory
 * PARTIAL - Only part of the requested quantity can be allocated
 * UNDISPATCHABLE - No quantity can be allocated from current inventory
 */
public enum Status {
    /** Order line can be fully fulfilled with available stock */
    ELIGIBLE,

    /** Order line can only be partially fulfilled with available stock */
    PARTIAL,

    /** Order line cannot be fulfilled with current inventory */
    UNDISPATCHABLE
}
