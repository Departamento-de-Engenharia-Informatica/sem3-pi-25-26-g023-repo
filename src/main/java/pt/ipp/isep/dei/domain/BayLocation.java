package pt.ipp.isep.dei.domain;

import java.util.Objects;

/**
 * Represents a specific location in a warehouse, defined by an {@code aisle} and a {@code bay}.
 * <p>
 * This class is immutable and provides reliable implementations for {@code equals}, {@code hashCode}, and {@code compareTo}.
 * It also includes validation and parsing utilities to safely construct instances from textual input (e.g., a {@link PickingAssignment}).
 * </p>
 *
 * <p>
 * Special cases:
 * <ul>
 *     <li>{@link #entrance()} returns the entrance location (0,0), which is not considered invalid.</li>
 *     <li>Invalid locations (e.g., parse errors) are represented with negative values (-1).</li>
 * </ul>
 * </p>
 */
public final class BayLocation implements Comparable<BayLocation> {
    private final int aisle;
    private final int bay;

    /**
     * Private constructor for internal use only (e.g., for the entrance).
     *
     * @param aisle the aisle number
     * @param bay   the bay number
     */
    private BayLocation(int aisle, int bay) {
        this.aisle = aisle;
        this.bay = bay;
    }

    /**
     * Public constructor that creates a {@code BayLocation} from a {@link PickingAssignment}.
     * <p>
     * If parsing fails or the assignment contains invalid values, the fields will default to {@code -1}.
     * </p>
     *
     * @param assignment the {@link PickingAssignment} containing aisle and bay information
     */
    public BayLocation(PickingAssignment assignment) {
        this(safeParseInt(assignment != null ? assignment.getAisle() : null, "aisle", assignment),
                safeParseInt(assignment != null ? assignment.getBay() : null, "bay", assignment));
    }

    /**
     * Static factory method that returns the warehouse entrance location (0,0).
     *
     * @return a {@code BayLocation} instance representing the entrance
     */
    public static BayLocation entrance() {
        return new BayLocation(0, 0);
    }

    /** @return the aisle number */
    public int getAisle() { return aisle; }

    /** @return the bay number */
    public int getBay() { return bay; }

    /**
     * Safely parses an integer from a string. If the value is null, empty, "N/A", or not numeric,
     * the result will be {@code -1}.
     * <p>
     * A warning message is printed to {@code System.err} for debugging purposes.
     * </p>
     *
     * @param value       the string to parse
     * @param fieldName   the name of the field (used for logging)
     * @param assignment  the related {@link PickingAssignment}, for context in logs
     * @return the parsed integer value or {@code -1} if parsing fails
     */
    private static int safeParseInt(String value, String fieldName, PickingAssignment assignment) {
        if (value == null || value.trim().isEmpty() || value.trim().equalsIgnoreCase("N/A")) {
            System.err.printf("⚠️ Warning BayLocation: '%s' null/empty/N/A for %s -> -1%n", fieldName, assignment);
            return -1;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            System.err.printf("⚠️ Warning BayLocation: Failed to parse '%s' ('%s') for %s -> -1%n", value, fieldName, assignment);
            return -1;
        }
    }

    /**
     * Checks if this location is valid.
     * <p>
     * A valid location requires both {@code aisle > 0} and {@code bay > 0}.
     * The entrance (0,0) is treated as a special valid case.
     * </p>
     *
     * @return {@code true} if this location is valid, {@code false} otherwise
     */
    public boolean isValid() {
        return aisle > 0 && bay > 0;
    }

    /**
     * Determines equality between two {@code BayLocation} objects based on their aisle and bay values.
     *
     * @param o the object to compare with
     * @return {@code true} if both objects represent the same location, {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BayLocation that = (BayLocation) o;
        return aisle == that.aisle && bay == that.bay;
    }

    /**
     * Computes the hash code based on {@code aisle} and {@code bay}.
     *
     * @return the computed hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(aisle, bay);
    }

    /**
     * Compares this location with another for ordering.
     * <p>
     * The comparison rules are:
     * <ul>
     *   <li>Entrance (0,0) is considered smaller than any valid location.</li>
     *   <li>Locations are ordered first by aisle, then by bay.</li>
     *   <li>Null values are considered greater than non-null ones.</li>
     * </ul>
     * </p>
     *
     * @param other the other {@code BayLocation} to compare to
     * @return a negative integer, zero, or a positive integer as this object
     *         is less than, equal to, or greater than the specified object
     */
    @Override
    public int compareTo(BayLocation other) {
        if (other == null) return -1;

        if (this.aisle == 0 && this.bay == 0 && (other.aisle > 0 || other.bay > 0)) return -1;
        if (other.aisle == 0 && other.bay == 0 && (this.aisle > 0 || this.bay > 0)) return 1;

        int aisleCompare = Integer.compare(this.aisle, other.aisle);
        if (aisleCompare != 0) {
            return aisleCompare;
        }
        return Integer.compare(this.bay, other.bay);
    }

    /**
     * Returns a string representation of this location for debugging and logging purposes.
     * <ul>
     *   <li>Invalid locations are printed as {@code (INVALID:aisle,bay)}.</li>
     *   <li>The entrance is printed as {@code (ENTRANCE)}.</li>
     *   <li>Valid locations are printed as {@code (aisle,bay)}.</li>
     * </ul>
     *
     * @return a formatted string representation of the location
     */
    @Override
    public String toString() {
        if (aisle < 0 || bay < 0) {
            return "(INVALID:" + aisle + "," + bay + ")";
        }
        if (aisle == 0 && bay == 0) {
            return "(ENTRANCE)";
        }
        return "(" + aisle + "," + bay + ")";
    }
}
