package pt.ipp.isep.dei.domain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * Implementação de uma Binary Search Tree (BST) otimizada para o Sprint 2.
 * Esta versão é construída de forma balanceada (bulk-build) para evitar
 * StackOverflowError em datasets grandes (USEI06/07).
 */
public class BST<K extends Comparable<K>, V> {

    private Node<K, V> root;

    private static class Node<K, V> {
        K key;
        V value;
        Node<K, V> left;
        Node<K, V> right;

        Node(K key, V value) {
            this.key = key;
            this.value = value;
            this.left = null;
            this.right = null;
        }
    }

    /**
     * Classe utilitária interna para armazenar pares Chave-Valor antes de ordenar.
     */
    private static class Pair<K, V> {
        K key;
        V value;
        Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }
        K getKey() { return key; }
    }


    public boolean isEmpty() {
        return root == null;
    }

    /**
     * NOVO MÉTODO (Substitui o insert):
     * Constrói uma árvore balanceada a partir de uma lista de valores.
     * Complexidade: O(N log N) (dominado pela ordenação) + O(N) (construção)
     * Isto evita o pior caso O(N^2) da inserção e o O(N) de profundidade de recursão.
     *
     * @param values A lista completa de objetos (ex: 62k EuropeanStation)
     * @param keyExtractor Uma função lambda para extrair a Chave (K) do Valor (V)
     * (ex: EuropeanStation::getLatitude)
     */
    public void buildBalancedTree(List<V> values, Function<V, K> keyExtractor) {
        if (values == null || values.isEmpty()) {
            root = null;
            return;
        }

        // 1. Criar uma lista de Pares (Chave, Valor)
        // O enunciado (USEI06/07) exige que estações com a mesma coordenada
        // sejam ordenadas por nome. Vamos usar o compareTo de EuropeanStation.
        // Adicionamos a Chave (ex: latitude) para a ordenação da árvore.
        List<Pair<K, V>> pairs = new ArrayList<>(values.size());
        for (V value : values) {
            pairs.add(new Pair<>(keyExtractor.apply(value), value));
        }

        // 2. Ordenar a lista pela Chave (K).
        // Se as chaves forem iguais, o Java (TimSort) mantém a ordem original,
        // que podemos pré-ordenar por nome se for preciso (mas o compareTo de V já deve tratar disso).
        // A especificação da BST que fiz (findAll) lida bem com duplicados.
        pairs.sort(Comparator.comparing(Pair::getKey));

        // 3. Construir a árvore recursivamente a partir da lista ordenada
        root = buildBalancedRec(pairs, 0, pairs.size() - 1);
    }

    /**
     * Helper recursivo para a construção balanceada.
     * Encontra o meio da lista, torna-o o nó raiz, e constrói
     * recursivamente as sub-árvores esquerda e direita.
     */
    private Node<K, V> buildBalancedRec(List<Pair<K, V>> pairs, int start, int end) {
        // Caso base: sub-array vazio
        if (start > end) {
            return null;
        }

        // 1. Encontra o meio
        int mid = (start + end) / 2;
        Pair<K, V> midPair = pairs.get(mid);

        // 2. Cria o nó raiz da sub-árvore
        Node<K, V> node = new Node<>(midPair.key, midPair.value);

        // 3. Constrói recursivamente a sub-árvore esquerda (elementos < meio)
        node.left = buildBalancedRec(pairs, start, mid - 1);

        // 4. Constrói recursivamente a sub-árvore direita (elementos > meio)
        node.right = buildBalancedRec(pairs, mid + 1, end);

        return node;
    }


    /**
     * Retorna uma lista de todos os valores na árvore, em ordem (in-order traversal).
     */
    public List<V> inOrderTraversal() {
        List<V> list = new ArrayList<>();
        inOrderRec(root, list);
        return list;
    }

    private void inOrderRec(Node<K, V> node, List<V> list) {
        if (node != null) {
            inOrderRec(node.left, list);
            list.add(node.value);
            inOrderRec(node.right, list);
        }
    }

    /**
     * Encontra todos os valores associados a uma chave específica.
     */
    public List<V> findAll(K key) {
        List<V> matchingValues = new ArrayList<>();
        findAllRec(root, key, matchingValues);
        return matchingValues;
    }

    /**
     * Procura recursiva por uma chave.
     * Esta implementação tem de lidar com chaves duplicadas.
     * A nossa `buildBalancedRec` coloca duplicados em qualquer um dos lados,
     * por isso temos de procurar AMBOS os lados se a chave for igual.
     */
    private void findAllRec(Node<K, V> node, K key, List<V> matchingValues) {
        if (node == null) {
            return;
        }

        int cmp = key.compareTo(node.key);

        if (cmp < 0) {
            // A chave que procuramos é menor, só pode estar à esquerda
            findAllRec(node.left, key, matchingValues);
        } else if (cmp > 0) {
            // A chave que procuramos é maior, só pode estar à direita
            findAllRec(node.right, key, matchingValues);
        } else {
            // Chave encontrada! Adiciona.
            matchingValues.add(node.value);
            // Como chaves duplicadas podem estar em AMBOS os lados,
            // temos de continuar a procurar em ambos os filhos.
            findAllRec(node.left, key, matchingValues);
            findAllRec(node.right, key, matchingValues);
        }
    }

    /**
     * Encontra todos os valores num dado intervalo de chaves [min, max].
     */
    public List<V> findInRange(K min, K max) {
        List<V> list = new ArrayList<>();
        findInRangeRec(root, min, max, list);
        return list;
    }

    private void findInRangeRec(Node<K, V> node, K min, K max, List<V> list) {
        if (node == null) {
            return;
        }

        // Se a chave atual é maior que min, vai para a esquerda
        if (min.compareTo(node.key) < 0) {
            findInRangeRec(node.left, min, max, list);
        }

        // Se a chave atual está dentro do intervalo
        if (min.compareTo(node.key) <= 0 && max.compareTo(node.key) >= 0) {
            list.add(node.value);
        }

        // Se a chave atual é menor que max, vai para a direita
        if (max.compareTo(node.key) > 0) {
            findInRangeRec(node.right, min, max, list);
        }
    }
}