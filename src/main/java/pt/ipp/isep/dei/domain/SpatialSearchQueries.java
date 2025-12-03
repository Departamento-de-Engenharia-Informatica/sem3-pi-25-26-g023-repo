package pt.ipp.isep.dei.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * USEI08 - Spatial Search Queries
 * Provides 5 predefined spatial queries for demonstration as required by USEI08 acceptance criteria.
 * This class works with SpatialSearch to showcase practical applications of KD-Tree spatial searches
 * for European railway stations.
 */
public class SpatialSearchQueries { // CLASSE: contém as 5 queries de demonstração obrigatórias da USEI08

    private final SpatialSearch spatialSearch; // CAMPO: motor de busca espacial injetado no construtor

    /**
     * Constructs a SpatialSearchQueries instance with the specified SpatialSearch engine.
     *
     * @param spatialSearch the spatial search engine to use for queries
     */
    public SpatialSearchQueries(SpatialSearch spatialSearch) { // CONSTRUTOR: recebe o motor de busca para usar nas queries
        this.spatialSearch = spatialSearch; // INICIALIZAÇÃO: guarda a referência para o motor de busca
    }

    /**
     * Represents the result of a spatial query with performance metrics.
     */
    public static class QueryResult { // CLASSE INTERNA: representa o resultado de uma query com métricas de performance
        public final String description; // DESCRIÇÃO: nome descritivo da query executada
        public final List<EuropeanStation> stations; // RESULTADOS: lista de estações encontradas
        public final long executionTimeNs; // TEMPO: duração da execução em nanosegundos
        public final int stationsFound; // CONTAGEM: número total de estações encontradas (calculado automaticamente)

        /**
         * Constructs a QueryResult with execution metrics.
         *
         * @param description query description
         * @param stations list of stations found
         * @param executionTimeNs execution time in nanoseconds
         */
        public QueryResult(String description, List<EuropeanStation> stations, long executionTimeNs) { // CONSTRUTOR: inicializa todos os campos
            this.description = description; // ATRIBUI: descrição da query
            this.stations = stations; // ATRIBUI: lista de estações resultante
            this.executionTimeNs = executionTimeNs; // ATRIBUI: tempo de execução medido
            this.stationsFound = stations.size(); // CALCULA: número de estações baseado no tamanho da lista
        }

        /**
         * Returns execution time in milliseconds.
         *
         * @return execution time in ms
         */
        public double getExecutionTimeMs() { // CONVERSÃO: transforma nanosegundos em milissegundos para leitura mais fácil
            return executionTimeNs / 1_000_000.0; // DIVISÃO: converte 1.000.000 ns para 1 ms
        }

        @Override
        public String toString() { // FORMATAÇÃO: representação textual padrão do resultado
            return String.format("%s: %d stations (%.2f ms)", // FORMATA: descrição + contagem + tempo
                    description, stationsFound, getExecutionTimeMs());
        }
    }

    /**
     * Query 1: All stations in Portugal.
     * Demonstrates country filter within specific geographical area.
     *
     * @return query result with stations in Portugal
     */
    public QueryResult queryAllStationsInPortugal() { // QUERY 1: todas as estações em Portugal
        String description = "All stations in Portugal"; // DESCRIÇÃO: nome amigável da query
        long startTime = System.nanoTime(); // INÍCIO: marca tempo antes da execução

        List<EuropeanStation> results = spatialSearch.searchByGeographicalArea( // EXECUÇÃO: chama o motor de busca espacial
                36.0, 42.0, // LATITUDE: limites de Portugal continental
                -9.5, -6.0, // LONGITUDE: limites de Portugal continental
                "PT", // FILTRO: apenas estações de Portugal
                null, // FILTRO: qualquer tipo de cidade (não filtra)
                null // FILTRO: qualquer tipo de estação principal (não filtra)
        );

        long endTime = System.nanoTime(); // FIM: marca tempo após a execução
        return new QueryResult(description, results, endTime - startTime); // RESULTADO: cria objeto com métricas
    }

    /**
     * Query 2: Main stations in Lisbon area.
     * Demonstrates combination of geographical area and station type filter.
     *
     * @return query result with main stations in Lisbon area
     */
    public QueryResult queryMainStationsInLisbon() { // QUERY 2: estações principais na área de Lisboa
        String description = "Main stations in Lisbon area";
        long startTime = System.nanoTime();

        List<EuropeanStation> results = spatialSearch.searchByGeographicalArea(
                38.70, 38.75, // LATITUDE: área restrita de Lisboa
                -9.15, -9.10, // LONGITUDE: área restrita de Lisboa
                "PT", // FILTRO: Portugal
                null, // FILTRO: qualquer tipo de cidade
                true // FILTRO: apenas estações principais (main stations)
        );

        long endTime = System.nanoTime();
        return new QueryResult(description, results, endTime - startTime);
    }

    /**
     * Query 3: City stations in France.
     * Demonstrates country and city status filters.
     *
     * @return query result with city stations in France
     */
    public QueryResult queryCityStationsInFrance() { // QUERY 3: estações de cidade em França
        String description = "City stations in France";
        long startTime = System.nanoTime();

        List<EuropeanStation> results = spatialSearch.searchByGeographicalArea(
                42.0, 51.0, // LATITUDE: limites aproximados de França
                -5.0, 8.0, // LONGITUDE: limites aproximados de França
                "FR", // FILTRO: França
                true, // FILTRO: apenas estações de cidade (city stations)
                null // FILTRO: qualquer tipo de estação principal
        );

        long endTime = System.nanoTime();
        return new QueryResult(description, results, endTime - startTime);
    }

    /**
     * Query 4: Non-main stations in Italy.
     * Demonstrates filter for non-main stations.
     *
     * @return query result with non-main stations in Italy
     */
    public QueryResult queryNonMainStationsInItaly() { // QUERY 4: estações NÃO principais em Itália
        String description = "Non-main stations in Italy";
        long startTime = System.nanoTime();

        List<EuropeanStation> results = spatialSearch.searchByGeographicalArea(
                35.0, 47.0, // LATITUDE: limites aproximados de Itália
                6.0, 18.0, // LONGITUDE: limites aproximados de Itália
                "IT", // FILTRO: Itália
                null, // FILTRO: qualquer tipo de cidade
                false // FILTRO: apenas estações NÃO principais (false)
        );

        long endTime = System.nanoTime();
        return new QueryResult(description, results, endTime - startTime);
    }

    /**
     * Query 5: All stations in Madrid metropolitan area.
     * Demonstrates precise geographical area search.
     *
     * @return query result with stations in Madrid area
     */
    public QueryResult queryStationsInMadrid() { // QUERY 5: todas as estações na área metropolitana de Madrid
        String description = "All stations in Madrid area";
        long startTime = System.nanoTime();

        List<EuropeanStation> results = spatialSearch.searchByGeographicalArea(
                40.30, 40.50, // LATITUDE: área precisa de Madrid
                -3.80, -3.60, // LONGITUDE: área precisa de Madrid
                "ES", // FILTRO: Espanha
                null, // FILTRO: qualquer tipo de cidade
                null // FILTRO: qualquer tipo de estação principal
        );

        long endTime = System.nanoTime();
        return new QueryResult(description, results, endTime - startTime);
    }

    /**
     * Executes all 5 predefined queries and returns results with performance metrics.
     *
     * @return list of query results for all demo queries
     */
    public List<QueryResult> executeAllDemoQueries() { // MÉTODO: executa todas as 5 queries obrigatórias de uma vez
        List<QueryResult> allResults = new ArrayList<>(); // LISTA: cria lista vazia para armazenar todos os resultados

        allResults.add(queryAllStationsInPortugal()); // EXECUTA: query 1 e adiciona à lista
        allResults.add(queryMainStationsInLisbon()); // EXECUTA: query 2 e adiciona à lista
        allResults.add(queryCityStationsInFrance()); // EXECUTA: query 3 e adiciona à lista
        allResults.add(queryNonMainStationsInItaly()); // EXECUTA: query 4 e adiciona à lista
        allResults.add(queryStationsInMadrid()); // EXECUTA: query 5 e adiciona à lista

        return allResults; // RETORNA: lista com resultados das 5 queries
    }

    /**
     * Executes a custom query with the specified parameters.
     *
     * @param description query description
     * @param latMin minimum latitude
     * @param latMax maximum latitude
     * @param lonMin minimum longitude
     * @param lonMax maximum longitude
     * @param country country filter
     * @param isCity city station filter
     * @param isMain main station filter
     * @return query result with execution metrics
     */
    public QueryResult executeCustomQuery(String description, // MÉTODO: permite criar queries personalizadas além das 5 obrigatórias
                                          double latMin, double latMax,
                                          double lonMin, double lonMax,
                                          String country,
                                          Boolean isCity,
                                          Boolean isMain) {
        long startTime = System.nanoTime(); // INÍCIO: marca tempo de execução

        List<EuropeanStation> results = spatialSearch.searchByGeographicalArea( // EXECUÇÃO: chama motor com parâmetros personalizados
                latMin, latMax, lonMin, lonMax, country, isCity, isMain
        );

        long endTime = System.nanoTime(); // FIM: marca tempo de execução
        return new QueryResult(description, results, endTime - startTime); // RESULTADO: cria objeto com métricas
    }

    /**
     * Generates a comprehensive performance report for the 5 queries.
     *
     * @return formatted performance report string
     */
    public String generatePerformanceReport() { // RELATÓRIO: gera sumário de performance das 5 queries
        List<QueryResult> results = executeAllDemoQueries(); // EXECUÇÃO: obtém resultados de todas as queries

        StringBuilder report = new StringBuilder(); // CONSTRUTOR: para construir string do relatório eficientemente
        report.append("=== USEI08 SPATIAL SEARCH - 5 DEMO QUERIES ===\n\n"); // CABEÇALHO: título do relatório

        int totalStations = 0; // CONTADOR: total de estações encontradas em todas as queries
        double totalTime = 0; // CONTADOR: tempo total de execução de todas as queries

        for (QueryResult result : results) { // LOOP: percorre cada resultado individual
            report.append(String.format("• %s\n", result.toString())); // ADICIONA: linha formatada com resultado da query
            totalStations += result.stationsFound; // SOMA: adiciona estações desta query ao total
            totalTime += result.getExecutionTimeMs(); // SOMA: adiciona tempo desta query ao total
        }

        double avgTime = totalTime / results.size(); // CÁLCULO: tempo médio por query
        double avgStations = (double) totalStations / results.size(); // CÁLCULO: estações médias por query

        report.append("\n=== SUMMARY ===\n"); // SEÇÃO: sumário estatístico
        report.append(String.format("Queries executed: %d\n", results.size())); // ESTATÍSTICA: número de queries
        report.append(String.format("Total stations found: %d\n", totalStations)); // ESTATÍSTICA: total de estações
        report.append(String.format("Average time per query: %.2f ms\n", avgTime)); // ESTATÍSTICA: tempo médio
        report.append(String.format("Average stations per query: %.1f\n", avgStations)); // ESTATÍSTICA: estações médias

        report.append("\n=== KD-TREE EFFICIENCY ===\n"); // SEÇÃO: análise de eficiência
        report.append("Complexity: O(√n) average case\n"); // COMPLEXIDADE: caso médio da KD-Tree
        report.append("Performance: Suitable for large datasets (64k stations)\n"); // AVALIAÇÃO: adequação para grandes datasets

        return report.toString(); // RETORNO: relatório completo como string
    }

    /**
     * Gets sample stations from each query for demonstration.
     *
     * @return formatted string with sample stations
     */
    public String getQuerySamples() { // AMOSTRAS: mostra exemplos de estações de cada query
        List<QueryResult> results = executeAllDemoQueries(); // EXECUÇÃO: obtém resultados das 5 queries

        StringBuilder samples = new StringBuilder(); // CONSTRUTOR: para construir string de amostras
        samples.append("=== SAMPLE STATIONS FROM EACH QUERY ===\n\n"); // CABEÇALHO: título da seção

        for (QueryResult result : results) { // LOOP: percorre cada resultado
            samples.append(String.format("%s:\n", result.description)); // ADICIONA: nome da query

            if (result.stations.isEmpty()) { // VERIFICA: se não encontrou estações
                samples.append("  No stations found\n"); // MENSAGEM: indica que não há resultados
            } else { // SE: encontrou estações
                result.stations.stream() // STREAM: processa lista de estações funcionalmente
                        .limit(3) // LIMITE: pega apenas as primeiras 3 estações
                        .forEach(station -> // AÇÃO: para cada estação, formata linha
                                samples.append(String.format("  • %s (%s) - Lat: %.4f, Lon: %.4f\n", // FORMATA: nome, país, coordenadas
                                        station.getStation(), station.getCountry(),
                                        station.getLatitude(), station.getLongitude()))
                        );

                if (result.stations.size() > 3) { // VERIFICA: se há mais de 3 estações
                    samples.append(String.format("  ... and %d more\n", result.stations.size() - 3)); // INDICA: quantas estações faltam
                }
            }
            samples.append("\n"); // ESPAÇO: linha em branco entre queries
        }

        return samples.toString(); // RETORNO: string com amostras formatadas
    }
}