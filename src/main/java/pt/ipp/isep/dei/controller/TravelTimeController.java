package pt.ipp.isep.dei.controller;

import pt.ipp.isep.dei.domain.*;
import pt.ipp.isep.dei.repository.*;

import java.util.*;
import java.util.stream.Collectors; // Necessário para Collectors.joining

/**
 * Controller class responsible for calculating travel times and managing railway network queries.
 * <p>
 * This class interacts with repositories and services to:
 * <ul>
 * <li>Calculate the fastest travel path between two stations given a locomotive.</li>
 * <li>Retrieve stations directly connected to a given station.</li>
 * <li>Format and return travel path results for display.</li>
 * </ul>
 * <p>
 * It is mainly used in the context of USLP03 (User Story: Calculate Travel Time).
 */
public class TravelTimeController {

    private final StationRepository estacaoRepo;
    private final LocomotiveRepository locomotivaRepo;
    private final RailwayNetworkService networkService;
    private final SegmentLineRepository segmentoRepo;

    /**
     * Constructs a TravelTimeController with required repositories and network service.
     *
     * @param estacaoRepo    Repository for stations.
     * @param locomotivaRepo Repository for locomotives.
     * @param networkService Service that computes railway paths and distances.
     * @param segmentoRepo   Repository for line segments (used to calculate direct connections and travel times).
     */
    public TravelTimeController(StationRepository estacaoRepo,
                                LocomotiveRepository locomotivaRepo,
                                RailwayNetworkService networkService,
                                SegmentLineRepository segmentoRepo) {
        this.estacaoRepo = estacaoRepo;
        this.locomotivaRepo = locomotivaRepo;
        this.networkService = networkService;
        this.segmentoRepo = segmentoRepo;
    }

    /**
     * Calculates the fastest travel time between two stations using the selected locomotive.
     * Considers the locomotive's maximum speed. // <-- ALTERADO
     *
     * @param idEstacaoPartida ID of the departure station.
     * @param idEstacaoChegada ID of the arrival station.
     * @param idLocomotiva     ID of the selected locomotive.
     * @return A formatted string with the travel path and estimated time, or an error message if inputs are invalid or no path exists.
     */
    public String calculateTravelTime(int idEstacaoPartida, int idEstacaoChegada, int idLocomotiva) {
        Optional<Station> optPartida = estacaoRepo.findById(idEstacaoPartida);
        Optional<Station> optChegada = estacaoRepo.findById(idEstacaoChegada);
        Optional<Locomotive> optLocomotiva = locomotivaRepo.findById(idLocomotiva);

        if (optPartida.isEmpty()) {
            return String.format("❌ ERROR: Departure station with ID %d not found.", idEstacaoPartida);
        }
        if (optChegada.isEmpty()) {
            return String.format("❌ ERROR: Arrival station with ID %d not found.", idEstacaoChegada);
        }
        if (optLocomotiva.isEmpty()) {
            return String.format("❌ ERROR: Locomotive with ID %d not found.", idLocomotiva);
        }
        if (idEstacaoPartida == idEstacaoChegada) {
            return "❌ ERROR: Departure and arrival stations are the same.";
        }

        Locomotive selectedLocomotive = optLocomotiva.get(); // Obter a locomotiva

        // ALTERADO: Passar o objeto Locomotive para o serviço
        RailwayPath path = networkService.findFastestPath(idEstacaoPartida, idEstacaoChegada, selectedLocomotive);

        if (path == null || path.isEmpty()) {
            return String.format("❌ ERROR: No railway path found between %s and %s.",
                    optPartida.get().getNome(), optChegada.get().getNome());
        }

        // Passar também a locomotiva para a formatação (já era feito)
        return formatPathResult(path, optPartida.get(), optChegada.get(), selectedLocomotive);
    }

    /**
     * Returns a formatted string listing stations directly connected to the departure station.
     * Includes estimated travel time based on a default locomotive speed for display purposes. // <-- ALTERADO
     *
     * @deprecated Use {@link #getDirectlyConnectedStations(int)} for data retrieval and format in the UI.
     *
     * @param idEstacaoPartida ID of the departure station.
     * @return Formatted string showing directly connected stations and estimated times, or an error message if departure station is invalid.
     */
    @Deprecated
    public String getDirectConnectionsInfo(int idEstacaoPartida) {
        Optional<Station> optPartida = estacaoRepo.findById(idEstacaoPartida);
        if (optPartida.isEmpty()) {
            return String.format("❌ ERROR: Departure station with ID %d not found.", idEstacaoPartida);
        }
        Station partida = optPartida.get();
        List<Station> reachableStations = getDirectlyConnectedStations(idEstacaoPartida);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("📍 Departure: %s (ID: %d)%n", partida.getNome(), partida.getIdEstacao()));
        sb.append("\n" + "-".repeat(40) + "\n");
        sb.append("🎯 Directly reachable destinations:\n");

        if (reachableStations.isEmpty()) {
            sb.append("   No directly connected stations found.\n");
        } else {
            // Para estimar o tempo, podemos pegar a primeira locomotiva ou usar uma velocidade padrão
            Optional<Locomotive> defaultLoco = locomotivaRepo.findAll().stream().findFirst();
            double speedForEstimation = defaultLoco.isPresent() ? defaultLoco.get().getMaxSpeed() : 100.0; // Usa 100km/h se não houver locomotivas

            for (Station destino : reachableStations) {
                Optional<LineSegment> directSegment = segmentoRepo.findDirectSegment(idEstacaoPartida, destino.getIdEstacao());
                if (directSegment.isPresent()) {
                    LineSegment seg = directSegment.get();
                    // ALTERADO: Estimar tempo usando min(velocidade_segmento, velocidade_estimada_loco)
                    double effectiveSpeed = Math.min(seg.getVelocidadeMaxima(), speedForEstimation);
                    double timeHours = (effectiveSpeed > 0) ? (seg.getComprimento() / effectiveSpeed) : Double.POSITIVE_INFINITY;
                    long timeMinutes = Math.round(timeHours * 60);
                    sb.append(String.format("   -> %s (ID: %d) | Dist: %.2f km | Est. Time: ~%d min%n",
                            destino.getNome(), destino.getIdEstacao(), seg.getComprimento(), timeMinutes));
                } else {
                    sb.append(String.format("   -> %s (ID: %d) (Segment info not found)%n", destino.getNome(), destino.getIdEstacao()));
                }
            }
        }
        sb.append("-".repeat(40) + "\n");

        return sb.toString();
    }


    /**
     * Returns a list of stations directly connected to the departure station.
     *
     * @param idEstacaoPartida ID of the departure station.
     * @return List of {@link Station} objects directly reachable from the departure station. Returns an empty list if no connections exist or the departure station is invalid.
     */
    public List<Station> getDirectlyConnectedStations(int idEstacaoPartida) {
        if (estacaoRepo.findById(idEstacaoPartida).isEmpty()) {
            System.err.printf("❌ ERROR: Departure station with ID %d not found.%n", idEstacaoPartida);
            return Collections.emptyList();
        }

        List<LineSegment> allSegments = segmentoRepo.findAll();
        Set<Integer> reachableStationIds = new HashSet<>();

        for (LineSegment seg : allSegments) {
            int neighborId = -1;
            if (seg.getIdEstacaoInicio() == idEstacaoPartida) {
                neighborId = seg.getIdEstacaoFim();
            } else if (seg.getIdEstacaoFim() == idEstacaoPartida) {
                neighborId = seg.getIdEstacaoInicio();
            }
            // Evitar adicionar a própria estação de partida à lista de vizinhos
            if (neighborId != -1 && neighborId != idEstacaoPartida) {
                reachableStationIds.add(neighborId);
            }
        }

        List<Station> reachableStations = new ArrayList<>();
        for (int id : reachableStationIds) {
            estacaoRepo.findById(id).ifPresent(reachableStations::add);
        }
        // Ordenar por ID para consistência na UI
        reachableStations.sort(Comparator.comparingInt(Station::getIdEstacao));

        return reachableStations;
    }


    /**
     * Helper method to format the fastest path result as a readable string.
     * Inclui a velocidade efetiva usada em cada segmento. // <-- ALTERADO
     *
     * @param path       RailwayPath object representing the fastest path.
     * @param partida    Departure station.
     * @param chegada    Arrival station.
     * @param locomotiva Selected locomotive.
     * @return Formatted string showing each segment, distances, effective speeds, times, and total travel time.
     */
    private String formatPathResult(RailwayPath path, Station partida, Station chegada, Locomotive locomotiva) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Results for travel from %s to %s:%n", partida.getNome(), chegada.getNome()));
        // ALTERADO: Mostrar velocidade máxima da locomotiva no cabeçalho
        sb.append(String.format("   (Selected Locomotive: ID %d - %s, Max Speed: %.1f km/h)%n",
                locomotiva.getIdLocomotiva(), locomotiva.getModelo(), locomotiva.getMaxSpeed()));
        sb.append("-".repeat(60) + "\n"); // Aumentar linha separadora
        sb.append("   Fastest path (by segments):\n");

        int i = 1;
        int previousStationId = partida.getIdEstacao();
        double cumulativeTimeHours = 0.0; // Para mostrar tempo acumulado

        for (LineSegment seg : path.getSegments()) {
            int startId = seg.getIdEstacaoInicio();
            int endId = seg.getIdEstacaoFim();
            String startName, endName;

            // Determinar a direção correta do segmento no percurso
            Optional<Station> optStart = estacaoRepo.findById(startId);
            Optional<Station> optEnd = estacaoRepo.findById(endId);

            // Lógica mais robusta para determinar qual estação é a 'de' e qual é a 'para' neste passo do caminho
            if (startId == previousStationId) {
                startName = optStart.map(Station::getNome).orElse("ID " + startId);
                endName = optEnd.map(Station::getNome).orElse("ID " + endId);
                previousStationId = endId; // Atualiza a estação 'anterior' para a próxima iteração
            } else if (endId == previousStationId) {
                startName = optEnd.map(Station::getNome).orElse("ID " + endId); // Inverte se o segmento foi usado na direção oposta
                endName = optStart.map(Station::getNome).orElse("ID " + startId);
                previousStationId = startId; // Atualiza a estação 'anterior'
            } else {
                // Caso de erro - o segmento não se conecta ao ponto anterior
                startName = optStart.map(s -> s.getNome() + " (Erro?)").orElse("ID " + startId);
                endName = optEnd.map(s -> s.getNome() + " (Erro?)").orElse("ID " + endId);
                // Não atualiza previousStationId aqui, pois a sequência está quebrada
                System.err.println("Erro na sequência do caminho: Segmento " + seg.getIdSegmento() + " não conecta com a estação anterior " + previousStationId);
            }


            // ALTERADO: Calcular velocidade efetiva e tempo para este segmento (como feito no serviço)
            double effectiveSpeed = Math.min(seg.getVelocidadeMaxima(), locomotiva.getMaxSpeed());
            double segmentTimeHours = Double.POSITIVE_INFINITY;
            if (effectiveSpeed > 0 && seg.getComprimento() > 0) {
                segmentTimeHours = seg.getComprimento() / effectiveSpeed;
            }
            cumulativeTimeHours += segmentTimeHours; // Acumular tempo

            sb.append(String.format("   Segment %d: %s -> %s%n", i++, startName, endName));
            // ALTERADO: Mostrar Distância, Velocidade Efetiva e Tempo do Segmento
            sb.append(String.format("      Dist: %.2f km | Effective Speed: %.1f km/h | Time: %.1f min (Total: %.1f min)%n",
                    seg.getComprimento(),
                    effectiveSpeed,
                    segmentTimeHours * 60,
                    cumulativeTimeHours * 60)); // Mostrar tempo acumulado em minutos
        }

        sb.append("-".repeat(60) + "\n"); // Aumentar linha separadora

        long totalMinutes = path.getTotalTimeMinutes();
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        String formattedTime = (hours > 0) ? String.format("%d hours and %d minutes", hours, minutes) : String.format("%d minutes", minutes);

        sb.append(String.format("   Total Distance: %.2f km%n", path.getTotalDistance()));
        sb.append(String.format("   Estimated Total Time: %s (%.2f hours)%n", formattedTime, path.getTotalTimeHours()));
        sb.append("=".repeat(60) + "\n"); // Linha final

        return sb.toString();
    }
}