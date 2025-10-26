package pt.ipp.isep.dei.domain;

import java.util.Stack;

/**
 * Manages returns in quarantine using LIFO (Last-In-First-Out).
 */
public class Quarantine {
    private final Stack<Return> stack = new Stack<>();

    /** Adds a return to quarantine. */
    public void addReturn(Return r) {
        stack.push(r);
    }

    /** Removes and returns the next return (LIFO). */
    public Return getNextReturn() {
        if (stack.isEmpty()) {
            return null;
        }
        return stack.pop();
    }

    /** Checks if quarantine is empty. */
    public boolean isEmpty() {
        return stack.isEmpty();
    }

    /** Returns the number of returns in quarantine. */
    public int size() {
        return stack.size();
    }
}