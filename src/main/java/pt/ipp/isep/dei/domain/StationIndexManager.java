package pt.ipp.isep.dei.domain;

import java.util.*;
import java.util.stream.Collectors;

import static pt.ipp.isep.dei.UI.CargoHandlingUI.*;

/**
 * Gere os índices BST/AVL para as estações europeias (USEI06).
 * <p>
 * *** ATUALIZADO PARA INCLUIR CAMPOS E MÉTODOS DA USEI07 ***
 */
public class StationIndexManager {

    // Os índices pedidos na USEI06
    private BST<Double, EuropeanStation> bstLatitude;
    private BST<Double, EuropeanStation> bstLongitude;
    private BST<String, EuropeanStation> bstTimeZoneGroup;

    // --- ADICIONADO PARA USEI07 ---
    private KDTree station2DTree;
    private List<EuropeanStation> orderedByLat;
    private List<EuropeanStation> orderedByLon;


    public StationIndexManager() {
        this.bstLatitude = new BST<>();
        this.bstLongitude = new BST<>();
        this.bstTimeZoneGroup = new BST<>();

        // --- ADICIONADO PARA USEI07 ---
        // Inicializa os novos campos
        this.station2DTree = null; // Será construída quando for pedida
        this.orderedByLat = new ArrayList<>();
        this.orderedByLon = new ArrayList<>();
    }

    /**
     * Constrói todos os índices BST/AVL com base na lista de estações.
     * Este método implementa o requisito principal da USEI06.
     * <p>
     * *** LÓGICA ATUALIZADA (v5) - CORREÇÃO DEFINITIVA ***
     */
    public void buildIndexes(List<EuropeanStation> stations) {
        System.out.println("Building BST/AVL indexes for " + stations.size() + " stations...");

        // Reinicializa as árvores
        this.bstLatitude = new BST<>();
        this.bstLongitude = new BST<>();
        this.bstTimeZoneGroup = new BST<>();

        // Pré-ordenar por nome (para o critério de desempate)
        List<EuropeanStation> sortedStations = stations.stream()
                .sorted() // Usa o compareTo(other) da EuropeanStation (que ordena por nome)
                .collect(Collectors.toList());

        // Agora, constrói as árvores balanceadas
        bstLatitude.buildBalancedTree(sortedStations, EuropeanStation::getLatitude);
        bstLongitude.buildBalancedTree(sortedStations, EuropeanStation::getLongitude);
        bstTimeZoneGroup.buildBalancedTree(sortedStations, EuropeanStation::getTimeZoneGroup);

        System.out.println("✅ All BST/AVL indexes built successfully (USEI06).");

        // --- 4. ADICIONADO PARA USEI07 (COM CORREÇÃO DEFINITIVA) ---
        // Extrai e armazena as listas ordenadas (necessárias para a construção da 2D-Tree)
        System.out.println("Extracting ordered lists for 2D-Tree...");

        // CORREÇÃO: O método na tua BST.java chama-se 'inOrderTraversal()'
        // e já retorna uma List<V>, por isso podemos atribuir diretamente.

        this.orderedByLat = this.bstLatitude.inOrderTraversal();
        this.orderedByLon = this.bstLongitude.inOrderTraversal();

        // --- FIM DA CORREÇÃO ---
    }

    /**
     * Executa a query da USEI06:
     * Retorna estações num grupo de fuso horário, ordenadas por país e nome.
     */
    public List<EuropeanStation> getStationsByTimeZoneGroup(String timeZoneGroup) {
        // 1. Encontra todas as estações no grupo
        List<EuropeanStation> stations = bstTimeZoneGroup.findAll(timeZoneGroup);

        // 2. Ordena-as por País (ASC) e depois por Nome (ASC)
        return stations.stream()
                .sorted(Comparator.comparing(EuropeanStation::getCountry)
                        .thenComparing(EuropeanStation::getStation))
                .collect(Collectors.toList());
    }

    /**
     * Executa a query "windowed" da USEI06:
     * Retorna estações num intervalo de fusos horários.
     */
    public List<EuropeanStation> getStationsInTimeZoneWindow(String tzgMin, String tzgMax) {
        List<EuropeanStation> stations = bstTimeZoneGroup.findInRange(tzgMin, tzgMax);

        // Ordena a lista final
        return stations.stream()
                .sorted(Comparator.comparing(EuropeanStation::getTimeZoneGroup)
                        .thenComparing(EuropeanStation::getCountry)
                        .thenComparing(EuropeanStation::getStation))
                .collect(Collectors.toList());
    }

    // Getters para os índices (necessários para as US seguintes)
    public BST<Double, EuropeanStation> getBstLatitude() {
        return bstLatitude;
    }

    public BST<Double, EuropeanStation> getBstLongitude() {
        return bstLongitude;
    }

    public BST<String, EuropeanStation> getBstTimeZoneGroup() {
        return bstTimeZoneGroup;
    }


    // --- 5. ADICIONAR OS 3 NOVOS MÉTODOS SEGUINTES PARA A USEI07 ---

    /**
     * Constrói a 2D-Tree balanceada (se ainda não tiver sido construída).
     * Usa as listas pré-ordenadas extraídas das BSTs da USEI06.
     */
    /**
     * Constrói a 2D-Tree balanceada (se ainda não tiver sido construída).
     * Usa as listas pré-ordenadas extraídas das BSTs da USEI06.
     * <p>
     * (V2: Translated to English)
     */
    public void build2DTree() {
        if (this.station2DTree != null) {
            System.out.println("ℹ️  2D-Tree (USEI07) was already built.");
            return;
        }

        if (this.orderedByLat.isEmpty() || this.orderedByLon.isEmpty()) {
            System.err.println(ANSI_RED + "ERROR: Ordered lists (Lat/Lon) are empty. Were USEI06 indexes loaded?" + ANSI_RESET);
            throw new IllegalStateException("Cannot build 2D-Tree. USEI06 indexes are not ready.");
        }

        System.out.println("⚙️  Building balanced 2D-Tree (USEI07)...");
        long startTime = System.nanoTime();

        this.station2DTree = new KDTree();
        // Passa as listas pré-ordenadas para o construtor da 2D-Tree
        this.station2DTree.buildBalanced(this.orderedByLat, this.orderedByLon);

        long endTime = System.nanoTime();
        System.out.printf(ANSI_GREEN + "✅ 2D-Tree built successfully in %.2f ms.%n" + ANSI_RESET, (endTime - startTime) / 1_000_000.0);
    }

    /**
     * Retorna as estatísticas da 2D-Tree (Tamanho, Altura, Buckets).
     * Constrói a árvore se for a primeira vez que é chamada.
     */
    public Map<String, Object> get2DTreeStats() {
        // Garante que a árvore está construída antes de devolver estatísticas
        if (this.station2DTree == null) {
            build2DTree();
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("size", station2DTree.size());
        stats.put("height", station2DTree.height());
        stats.put("bucketSizes", station2DTree.getBucketSizes());

        return stats;
    }

    /**
     * Getter para a 2D-Tree (necessário para USEI08, 09, 10).
     */
    public KDTree getStation2DTree() {
        // Garante que a árvore está construída antes de ser usada
        if (this.station2DTree == null) {
            build2DTree();
        }
        return station2DTree;
    }
}