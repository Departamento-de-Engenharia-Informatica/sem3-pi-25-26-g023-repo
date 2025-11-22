package pt.ipp.isep.dei.domain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * Implementation of a Binary Search Tree (BST) optimized for performance, especially
 * with large datasets (e.g., USEI06/07).
 * This version uses a **bulk-build** approach to construct a balanced tree, which
 * prevents the worst-case scenario (O(N^2) insertion time and O(N) stack depth)
 * and avoids potential StackOverflowErrors in large scale operations.
 *
 * @param <K> The type of keys stored in the BST, which must be Comparable.
 * @param <V> The type of values stored (e.g., EuropeanStation).
 */
public class BST<K extends Comparable<K>, V> {

    private Node<K, V> root;

    /**
     * Represents a single node within the Binary Search Tree.
     * Stores the key, the associated value, and references to the left and right children.
     *
     * @param <K> The key type.
     * @param <V> The value type.
     */
    private static class Node<K, V> {
        K key;
        V value;
        Node<K, V> left;
        Node<K, V> right;

        /**
         * Constructs a new Node with the specified key and value.
         *
         * @param key The comparable key for ordering.
         * @param value The value associated with the key.
         */
        Node(K key, V value) {
            this.key = key;
            this.value = value;
            this.left = null;
            this.right = null;
        }
    }

    /**
     * Internal utility class to hold Key-Value pairs before sorting, which is
     * necessary for the balanced tree construction process.
     *
     * @param <K> The key type.
     * @param <V> The value type.
     */
    private static class Pair<K, V> {
        K key;
        V value;

        /**
         * Constructs a new Pair.
         *
         * @param key The key.
         * @param value The value.
         */
        Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }

        /**
         * Gets the key of the pair.
         *
         * @return The key.
         */
        K getKey() {
            return key;
        }
    }


    /**
     * Checks if the Binary Search Tree is empty.
     *
     * @return true if the tree has no nodes (root is null), false otherwise.
     */
    public boolean isEmpty() {
        return root == null;
    }

    /**
     * Builds a perfectly balanced BST from a list of values using a bulk-build approach.
     * This method first extracts the keys, sorts them, and then recursively finds the
     * median element to establish the root of each sub-tree.
     *
     * **Time Complexity:** O(N log N) (dominated by sorting) + O(N) (construction).
     * This approach guarantees a logarithmic height (O(log N)) for optimal search performance.
     *
     * @param values The complete list of value objects (e.g., 62,000 EuropeanStation objects).
     * @param keyExtractor A lambda function to extract the Key (K) from the Value (V),
     * e.g., {@code EuropeanStation::getLatitude}.
     */
    public void buildBalancedTree(List<V> values, Function<V, K> keyExtractor) {
        if (values == null || values.isEmpty()) {
            root = null;
            return;
        }

        // 1. Create a list of (Key, Value) Pairs.
        List<Pair<K, V>> pairs = new ArrayList<>(values.size());
        for (V value : values) {
            pairs.add(new Pair<>(keyExtractor.apply(value), value));
        }

        // 2. Sort the list based on the extracted Key (K).
        // For equal keys (duplicates), Java's TimSort maintains the original order (e.g., sorted by name if pre-sorted).
        pairs.sort(Comparator.comparing(Pair::getKey));

        // 3. Recursively build the balanced tree from the sorted list.
        root = buildBalancedRec(pairs, 0, pairs.size() - 1);
    }

    /**
     * Recursive helper method for balanced tree construction.
     * Finds the middle element of the sorted list, makes it the root, and
     * recursively builds the left and right sub-trees.
     *
     * @param pairs The sorted list of Key-Value pairs.
     * @param start The starting index of the current sub-list.
     * @param end The ending index of the current sub-list.
     * @return The root node of the constructed sub-tree.
     */
    private Node<K, V> buildBalancedRec(List<Pair<K, V>> pairs, int start, int end) {
        // Base case: empty sub-array
        if (start > end) {
            return null;
        }

        // 1. Find the middle element (median)
        int mid = (start + end) / 2;
        Pair<K, V> midPair = pairs.get(mid);

        // 2. Create the root node for this sub-tree
        Node<K, V> node = new Node<>(midPair.key, midPair.value);

        // 3. Recursively build the left sub-tree (elements < median)
        node.left = buildBalancedRec(pairs, start, mid - 1);

        // 4. Recursively build the right sub-tree (elements > median)
        node.right = buildBalancedRec(pairs, mid + 1, end);

        return node;
    }


    /**
     * Returns a list of all values in the tree in ascending key order (In-Order Traversal).
     *
     * @return A List containing all values (V) in sorted key order.
     */
    public List<V> inOrderTraversal() {
        List<V> list = new ArrayList<>();
        inOrderRec(root, list);
        return list;
    }

    /**
     * Recursive helper for the in-order traversal.
     *
     * @param node The current node.
     * @param list The list to collect the values.
     */
    private void inOrderRec(Node<K, V> node, List<V> list) {
        if (node != null) {
            inOrderRec(node.left, list);
            list.add(node.value);
            inOrderRec(node.right, list);
        }
    }

    /**
     * Finds and returns a list of all values associated with a specific key.
     * This method is designed to correctly handle duplicate keys, which are possible
     * when the BST key is a coordinate (e.g., multiple stations at the same latitude).
     *
     * @param key The key to search for.
     * @return A List of all values (V) matching the key. Returns an empty list if not found.
     */
    public List<V> findAll(K key) {
        List<V> matchingValues = new ArrayList<>();
        findAllRec(root, key, matchingValues);
        return matchingValues;
    }

    /**
     * Recursive search helper for {@code findAll}.
     * If a key is matched, the search must continue in BOTH the left and right sub-trees
     * because the balanced construction process may place duplicate keys on either side.
     *
     * @param node The current node.
     * @param key The key to search for.
     * @param matchingValues The list to collect matched values.
     */
    private void findAllRec(Node<K, V> node, K key, List<V> matchingValues) {
        if (node == null) {
            return;
        }

        int cmp = key.compareTo(node.key);

        if (cmp < 0) {
            // Searched key is smaller, continue left
            findAllRec(node.left, key, matchingValues);
        } else if (cmp > 0) {
            // Searched key is larger, continue right
            findAllRec(node.right, key, matchingValues);
        } else {
            // Key found! Add it.
            matchingValues.add(node.value);

            // Continue searching in both children to find duplicates
            findAllRec(node.left, key, matchingValues);
            findAllRec(node.right, key, matchingValues);
        }
    }

    /**
     * Finds all values whose keys fall within the closed interval [min, max].
     * The results are returned in ascending key order.
     *
     * @param min The minimum boundary key (inclusive).
     * @param max The maximum boundary key (inclusive).
     * @return A List of all values (V) within the specified key range.
     */
    public List<V> findInRange(K min, K max) {
        List<V> list = new ArrayList<>();
        findInRangeRec(root, min, max, list);
        return list;
    }

    /**
     * Recursive helper for {@code findInRange}. Uses the key boundaries to prune
     * the search space and ensures duplicates are handled correctly by searching
     * both children when a boundary is matched.
     *
     * @param node The current node.
     * @param min The minimum boundary key.
     * @param max The maximum boundary key.
     * @param list The list to collect the matching values.
     */
    private void findInRangeRec(Node<K, V> node, K min, K max, List<V> list) {
        if (node == null) {
            return;
        }

        // If min <= node.key, continue searching the left sub-tree.
        // The equality check (<= 0) is CRUCIAL for finding duplicates in the left sub-tree.
        if (min.compareTo(node.key) <= 0) {
            findInRangeRec(node.left, min, max, list);
        }

        // If the current key is within the range [min, max], add the value.
        if (min.compareTo(node.key) <= 0 && max.compareTo(node.key) >= 0) {
            list.add(node.value);
        }

        // If max >= node.key, continue searching the right sub-tree.
        // The equality check (>= 0) is CRUCIAL for finding duplicates in the right sub-tree.
        if (max.compareTo(node.key) >= 0) {
            findInRangeRec(node.right, min, max, list);
        }
    }
}