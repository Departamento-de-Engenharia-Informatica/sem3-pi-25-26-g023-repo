package pt.ipp.isep.dei.domain;

import java.util.Stack;

public class Quarantine {
    private final Stack<Return> stack = new Stack<>();

    public void addReturn(Return r) {
        stack.push(r);
    }

    public Return getNextReturn() {
        return stack.pop();
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    public int size() {
        return stack.size();
    }
}

