package pt.ipp.isep.dei.domain;

import java.util.Objects;

/**
 * Represents a physical location in a warehouse, defined by an aisle and a bay number.
 * A {@code BayLocation} can also represent the warehouse entrance (0,0) or
 * an invalid location (values set to -1).
 */
public class BayLocation implements Comparable<BayLocation> {

    private final int aisle;
    private final int bay;

    /**
     * @param aisle aisle number
     * @param bay   bay number
     */
    private BayLocation(int aisle, int bay) {
        this.aisle = aisle;
        this.bay = bay;
    }

    /**
     * Creates a {@code BayLocation} from a {@link PickingAssignment}.
     * @param assignment the picking assignment containing aisle and bay info
     */
    public BayLocation(PickingAssignment assignment) {
        this(
                safeParseInt(assignment != null ? assignment.getAisle() : null, "aisle", assignment),
                safeParseInt(assignment != null ? assignment.getBay() : null, "bay", assignment)
        );
    }

    /**
     * Returns a {@code BayLocation} representing the warehouse entrance (0,0).
     *
     * @return the entrance location
     */
    public static BayLocation entrance() {
        return new BayLocation(0, 0);
    }


    public int getAisle() { return aisle; }


    public int getBay() { return bay; }

    /**
     * Parses an integer safely from a string.
     * Returns {@code -1} if the value is null, empty, "N/A", or not numeric.
     *
     * @param value      string to parse
     * @param fieldName  field name (for logging)
     * @param assignment related picking assignment
     * @return parsed integer or {@code -1} if invalid
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
     * @return {@code true} if valid, otherwise {@code false}
     */
    public boolean isValid() {
        return aisle > 0 && bay > 0;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BayLocation that = (BayLocation) o;
        return aisle == that.aisle && bay == that.bay;
    }


    @Override
    public int hashCode() {
        return Objects.hash(aisle, bay);
    }

    /**
     * Compares this location to another.
     * @param other another bay location
     * @return comparison result
     */
    @Override
    public int compareTo(BayLocation other) {
        if (other == null) return -1;
        if (this.aisle == 0 && this.bay == 0 && (other.aisle > 0 || other.bay > 0)) return -1;
        if (other.aisle == 0 && other.bay == 0 && (this.aisle > 0 || this.bay > 0)) return 1;

        int aisleCompare = Integer.compare(this.aisle, other.aisle);
        if (aisleCompare != 0) return aisleCompare;
        return Integer.compare(this.bay, other.bay);
    }

    /**
     * Returns a readable string of this location.
     * <ul>
     *   <li>(ENTRANCE) for (0,0)</li>
     *   <li>(INVALID:a,b) for invalid values</li>
     *   <li>(a,b) for valid locations</li>
     * </ul>
     *
     * @return formatted string
     */
    @Override
    public String toString() {
        if (aisle < 0 || bay < 0) return "(INVALID:" + aisle + "," + bay + ")";
        if (aisle == 0 && bay == 0) return "(ENTRANCE)";
        return "(" + aisle + "," + bay + ")";
    }
}
