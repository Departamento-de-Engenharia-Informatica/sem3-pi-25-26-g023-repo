package pt.ipp.isep.dei.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * USEI08 - Search by Geographical Area
 * Implements range search in KD-Tree for European railway stations with optional filters.
 * Provides efficient spatial queries using KD-Tree pruning to avoid full dataset scans.
 */
public record SpatialSearch(KDTree kdTree) { // RECORD: classe imutável que automaticamente gera getters, equals, hashCode

    /**
     * Constructs a SpatialSearch instance with the specified KD-Tree.
     *
     * @param kdTree the KD-Tree containing European railway stations
     * @throws IllegalArgumentException if kdTree is null
     */
    public SpatialSearch {
        if (kdTree == null) { // VALIDAÇÃO: garante que a KD-Tree não é nula
            throw new IllegalArgumentException("KD-Tree cannot be null"); // EXCEÇÃO: se for nula, lança erro
        }
    }

    /**
     * Searches for stations within specified geographical boundaries with optional filters.
     * Uses KD-Tree properties to prune search space efficiently.
     *
     * @param latMin minimum latitude boundary (-90 to 90)
     * @param latMax maximum latitude boundary (-90 to 90)
     * @param lonMin minimum longitude boundary (-180 to 180)
     * @param lonMax maximum longitude boundary (-180 to 180)
     * @param countryFilter country code filter (e.g., "PT", "ES") or null for any
     * @param isCityFilter filter for city stations (true/false) or null for any
     * @param isMainStationFilter filter for main stations (true/false) or null for any
     * @return list of EuropeanStation objects matching the criteria
     * @throws IllegalArgumentException if coordinate boundaries are invalid
     */
    public List<EuropeanStation> searchByGeographicalArea(double latMin, double latMax, double lonMin, double lonMax,
                                                          String countryFilter, Boolean isCityFilter, Boolean isMainStationFilter) {

        validateCoordinates(latMin, latMax, lonMin, lonMax); // VALIDAÇÃO: verifica se coordenadas estão dentro dos limites geográficos

        List<EuropeanStation> results = new ArrayList<>(); // INICIALIZAÇÃO: cria lista vazia para armazenar resultados
        searchInRangeRecursive(kdTree.getRoot(), latMin, latMax, lonMin, lonMax, // CHAMADA RECURSIVA: inicia busca a partir da raiz da KD-Tree
                countryFilter, isCityFilter, isMainStationFilter, 0, results);
        return results; // RETORNO: devolve lista com todas as estações encontradas
    }

    /**
     * Validates coordinate boundaries according to geographical limits.
     *
     * @param latMin minimum latitude
     * @param latMax maximum latitude
     * @param lonMin minimum longitude
     * @param lonMax maximum longitude
     * @throws IllegalArgumentException if coordinates are outside valid ranges
     */
    private void validateCoordinates(double latMin, double latMax, double lonMin, double lonMax) {
        if (latMin < -90.0 || latMax > 90.0) { // VALIDA LATITUDE: deve estar entre -90 e 90 graus
            throw new IllegalArgumentException("Invalid latitude range: [" + latMin + "," + latMax + "]");
        }
        if (lonMin < -180.0 || lonMax > 180.0) { // VALIDA LONGITUDE: deve estar entre -180 e 180 graus
            throw new IllegalArgumentException("Invalid longitude range: [" + lonMin + ", " + lonMax + "]");
        }
    }

    /**
     * Recursively searches KD-Tree nodes within the specified geographical range.
     * Implements KD-Tree pruning to optimize search performance.
     *
     * @param node current KD-Tree node being processed
     * @param latMin minimum latitude boundary
     * @param latMax maximum latitude boundary
     * @param lonMin minimum longitude boundary
     * @param lonMax maximum longitude boundary
     * @param countryFilter country code filter
     * @param isCityFilter city station filter
     * @param isMainStationFilter main station filter
     * @param depth current depth in KD-Tree
     * @param results list to accumulate matching stations
     */
    private void searchInRangeRecursive(KDTree.Node node, double latMin, double latMax, double lonMin, double lonMax,
                                        String countryFilter, Boolean isCityFilter, Boolean isMainStationFilter,
                                        int depth, List<EuropeanStation> results) {

        if (node == null) { // CASO BASE: se o nó é nulo, termina a recursão nesta ramificação
            return;
        }

        double currentLat = node.getLatitude(); // OBTÉM: latitude do nó atual
        double currentLon = node.getLongitude(); // OBTÉM: longitude do nó atual
        int currentDimension = depth % 2; // CALCULA: dimensão atual (0 = latitude, 1 = longitude) - alterna a cada nível

        boolean inLatRange = (currentLat >= latMin && currentLat <= latMax); // VERIFICA: se nó está dentro do intervalo de latitude
        boolean inLonRange = (currentLon >= lonMin && currentLon <= lonMax); // VERIFICA: se nó está dentro do intervalo de longitude

        if (inLatRange && inLonRange) { // SE: nó está dentro da área retangular de busca
            for (EuropeanStation station : node.getStations()) { // PERCORRE: todas as estações neste nó (pode haver múltiplas com mesma coordenada)
                if (matchesFilters(station, countryFilter, isCityFilter, isMainStationFilter)) { // APLICA FILTROS: verifica se estação atende aos critérios
                    results.add(station); // ADICIONA: estação à lista de resultados se passar nos filtros
                }
            }
        }

        // PODA DA KD-TREE: decide quais subárvores visitar baseado na dimensão atual e limites de busca
        if (currentDimension == 0) { // DIMENSÃO ATUAL: latitude (nível par da árvore)
            if (latMin <= currentLat) { // SE: área de busca inclui valores menores que latitude atual
                searchInRangeRecursive(node.getLeft(), latMin, latMax, lonMin, lonMax, // VISITA: subárvore ESQUERDA (valores menores)
                        countryFilter, isCityFilter, isMainStationFilter, depth + 1, results);
            }
            if (latMax >= currentLat) { // SE: área de busca inclui valores maiores que latitude atual
                searchInRangeRecursive(node.getRight(), latMin, latMax, lonMin, lonMax, // VISITA: subárvore DIREITA (valores maiores)
                        countryFilter, isCityFilter, isMainStationFilter, depth + 1, results);
            }
        } else { // DIMENSÃO ATUAL: longitude (nível ímpar da árvore)
            if (lonMin <= currentLon) { // SE: área de busca inclui valores menores que longitude atual
                searchInRangeRecursive(node.getLeft(), latMin, latMax, lonMin, lonMax, // VISITA: subárvore ESQUERDA (valores menores)
                        countryFilter, isCityFilter, isMainStationFilter, depth + 1, results);
            }
            if (lonMax >= currentLon) { // SE: área de busca inclui valores maiores que longitude atual
                searchInRangeRecursive(node.getRight(), latMin, latMax, lonMin, lonMax, // VISITA: subárvore DIREITA (valores maiores)
                        countryFilter, isCityFilter, isMainStationFilter, depth + 1, results);
            }
        }
    }

    /**
     * Applies optional filters to a station. Null filters are ignored.
     *
     * @param station the station to check
     * @param countryFilter country code filter
     * @param isCityFilter city station filter
     * @param isMainStationFilter main station filter
     * @return true if station matches all specified filters
     */
    private boolean matchesFilters(EuropeanStation station, String countryFilter,
                                   Boolean isCityFilter, Boolean isMainStationFilter) {

        if (countryFilter != null && !countryFilter.equals("all")) { // FILTRO PAÍS: se especificado e não é "all"
            if (!countryFilter.equalsIgnoreCase(station.getCountry())) { // COMPARA: código do país (case insensitive)
                return false; // REJEITA: estação se país não corresponder
            }
        }

        if (isCityFilter != null && isCityFilter != station.isCity()) { // FILTRO CIDADE: se especificado
            return false; // REJEITA: estação se tipo cidade não corresponder
        }

        if (isMainStationFilter != null && isMainStationFilter != station.isMainStation()) { // FILTRO ESTAÇÃO PRINCIPAL: se especificado
            return false; // REJEITA: estação se tipo estação principal não corresponder
        }

        return true; // ACEITA: estação passou em todos os filtros aplicados
    }

    /**
     * Provides complexity analysis for the spatial search operations.
     *
     * @return formatted string with performance analysis
     */
    public String getComplexityAnalysis() {
        return String.format("""
                         USEI08 Complexity Analysis:
                         KD-Tree Properties:
                         - Height: %d
                         - Nodes: %d
                         - Balance: %s
                         
                         Time Complexity:
                         - Best case: O(log n)
                         - Average case: O(√n)
                         - Worst case: O(n)
                         
                         Space Complexity:
                         - Auxiliary: O(1)
                         - Recursion stack: O(log n)
                        """,
                kdTree.height(), // ALTURA: número de níveis da KD-Tree
                kdTree.size(), // TAMANHO: número total de nós na KD-Tree
                kdTree.height() <= 2 * Math.log(kdTree.size()) / Math.log(2) ? "Good" : "Could be improved"); // OPERADOR TERNÁRIO: avalia balanceamento da árvore
    }
}