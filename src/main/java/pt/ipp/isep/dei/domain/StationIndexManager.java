package pt.ipp.isep.dei.domain;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Gere os índices BST/AVL para as estações europeias (USEI06).
 */
public class StationIndexManager {

    // Os índices pedidos na USEI06
    private BST<Double, EuropeanStation> bstLatitude;
    private BST<Double, EuropeanStation> bstLongitude;
    private BST<String, EuropeanStation> bstTimeZoneGroup;

    public StationIndexManager() {
        this.bstLatitude = new BST<>();
        this.bstLongitude = new BST<>();
        this.bstTimeZoneGroup = new BST<>();
    }

    /**
     * Constrói todos os índices BST/AVL com base na lista de estações.
     * Este método implementa o requisito principal da USEI06.
     *
     * *** LÓGICA ATUALIZADA PARA USAR buildBalancedTree ***
     */
    public void buildIndexes(List<EuropeanStation> stations) {
        System.out.println("Building BST/AVL indexes for " + stations.size() + " stations...");

        // Reinicializa as árvores
        this.bstLatitude = new BST<>();
        this.bstLongitude = new BST<>();
        this.bstTimeZoneGroup = new BST<>();

        // Pré-ordenar por nome (para o critério de desempate)
        // O enunciado da USEI06/07 diz que estações com as mesmas
        // coordenadas devem ser ordenadas por nome.
        List<EuropeanStation> sortedStations = stations.stream()
                .sorted() // Usa o compareTo(other) da EuropeanStation (que ordena por nome)
                .collect(Collectors.toList());

        // Agora, constrói as árvores balanceadas
        // O 'buildBalancedTree' vai manter a ordem relativa (por nome)
        // para chaves duplicadas (ex: mesma latitude).
        bstLatitude.buildBalancedTree(sortedStations, EuropeanStation::getLatitude);
        bstLongitude.buildBalancedTree(sortedStations, EuropeanStation::getLongitude);
        bstTimeZoneGroup.buildBalancedTree(sortedStations, EuropeanStation::getTimeZoneGroup);

        System.out.println("✅ All BST/AVL indexes built successfully (USEI06).");
    }

    /**
     * Executa a query da USEI06:
     * Retorna estações num grupo de fuso horário, ordenadas por país e nome.
     */
    public List<EuropeanStation> getStationsByTimeZoneGroup(String timeZoneGroup) {
        // 1. Encontra todas as estações no grupo
        List<EuropeanStation> stations = bstTimeZoneGroup.findAll(timeZoneGroup);

        // 2. Ordena-as por País (ASC) e depois por Nome (ASC)
        // O nosso 'buildBalancedTree' e 'findAll' já devem garantir a ordem por nome
        // para chaves iguais, mas voltamos a ordenar para garantir a ordem por País.
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
}