package pt.ipp.isep.dei.domain;

import java.util.Stack;

/**
 * Gerencia devoluções em quarentena usando LIFO (Last-In-First-Out)
 */
public class Quarantine {
    private final Stack<Return> stack = new Stack<>();

    /**
     * Adiciona uma devolução à quarentena
     */
    public void addReturn(Return r) {
        stack.push(r);
    }

    /**
     * Remove e retorna a próxima devolução (LIFO)
     * @return Próxima devolução ou null se vazia
     */
    public Return getNextReturn() {
        if (stack.isEmpty()) {
            return null;
        }
        return stack.pop();
    }

    /**
     * Verifica se a quarentena está vazia
     */
    public boolean isEmpty() {
        return stack.isEmpty();
    }

    /**
     * Retorna o número de devoluções em quarentena
     */
    public int size() {
        return stack.size();
    }
}