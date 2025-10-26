package pt.ipp.isep.dei.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the result of an order allocation process.
 */
public class AllocationResult {

    /** List of eligibility records for each order line. */
    public final List<Eligibility> eligibilityList = new ArrayList<>();

    /** List of detailed stock allocations. */
    public final List<Allocation> allocations = new ArrayList<>();
}

