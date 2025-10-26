package pt.ipp.isep.dei.domain;

/**
 * Represents the status of a picking assignment.
 */
public enum PickingStatus {
    /** Assignment is waiting to be processed */
    PENDING,

    /** Assignment has been assigned to a picker */
    ASSIGNED,

    /** Assignment has been fully picked */
    PICKED,

    /** Assignment has been partially picked */
    PARTIALLY_PICKED,

    /** Assignment has been deferred for later picking */
    DEFERRED
}
