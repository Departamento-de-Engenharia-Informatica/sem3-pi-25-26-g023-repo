package pt.ipp.isep.dei.controller;

import pt.ipp.isep.dei.domain.EuropeanStation;
import pt.ipp.isep.dei.domain.Locomotive;
import pt.ipp.isep.dei.domain.LineSegment;
import pt.ipp.isep.dei.domain.RailwayPath;
import pt.ipp.isep.dei.repository.LocomotiveRepository;
import pt.ipp.isep.dei.repository.SegmentLineRepository;
import pt.ipp.isep.dei.repository.StationRepository;
import pt.ipp.isep.dei.domain.RailwayNetworkService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class TravelTimeController {

    private static final double LOCOMOTIVE_DEFAULT_MAX_SPEED = 160.0; // Limite padrão para o cálculo de tempo

    private final StationRepository estacaoRepo;
    private final LocomotiveRepository locomotivaRepo;
    private final RailwayNetworkService networkService;
    private final SegmentLineRepository segmentoRepo;

    public TravelTimeController(StationRepository estacaoRepo,
                                LocomotiveRepository locomotivaRepo,
                                RailwayNetworkService networkService,
                                SegmentLineRepository segmentoRepo) {
        this.estacaoRepo = estacaoRepo;
        this.locomotivaRepo = locomotivaRepo;
        this.networkService = networkService;
        this.segmentoRepo = segmentoRepo;
    }

    public String calculateTravelTime(int idEstacaoPartida, int idEstacaoChegada, int idLocomotiva) {
        // CORREÇÃO DE TIPO: De Station para EuropeanStation
        Optional<EuropeanStation> optPartida = estacaoRepo.findById(idEstacaoPartida);
        Optional<EuropeanStation> optChegada = estacaoRepo.findById(idEstacaoChegada);
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

        Locomotive selectedLocomotive = optLocomotiva.get();

        // O NetworkService já não recebe a Locomotiva, mas sim a velocidade limite
        RailwayPath path = networkService.findFastestPath(idEstacaoPartida, idEstacaoChegada, LOCOMOTIVE_DEFAULT_MAX_SPEED);

        if (path == null || path.isEmpty()) {
            return String.format("❌ ERROR: No railway path found between %s and %s.",
                    optPartida.get().getStation(), optChegada.get().getStation());
        }

        return formatPathResult(path, optPartida.get(), optChegada.get(), selectedLocomotive);
    }

    @Deprecated
    public String getDirectConnectionsInfo(int idEstacaoPartida) {
        // CORREÇÃO DE TIPO
        Optional<EuropeanStation> optPartida = estacaoRepo.findById(idEstacaoPartida);
        if (optPartida.isEmpty()) {
            return String.format("❌ ERROR: Departure station with ID %d not found.", idEstacaoPartida);
        }
        EuropeanStation partida = optPartida.get();

        // CORREÇÃO DE TIPO
        List<EuropeanStation> reachableStations = getDirectlyConnectedStations(idEstacaoPartida);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("📍 Departure: %s (ID: %d)%n", partida.getStation(), partida.getIdEstacao()));
        sb.append("\n" + "-".repeat(40) + "\n");
        sb.append("🎯 Directly reachable destinations:\n");

        if (reachableStations.isEmpty()) {
            sb.append("   No directly connected stations found.\n");
        } else {
            // CORREÇÃO: Usar a constante, pois getMaxSpeed() não existe mais
            // double speedForEstimation = defaultLoco.map(Locomotive::getMaxSpeed).orElse(100.0);
            double speedForEstimation = LOCOMOTIVE_DEFAULT_MAX_SPEED; // <--- CORREÇÃO

            // CORREÇÃO DE TIPO
            for (EuropeanStation destino : reachableStations) {
                Optional<LineSegment> directSegment = segmentoRepo.findDirectSegment(idEstacaoPartida, destino.getIdEstacao());
                if (directSegment.isPresent()) {
                    LineSegment seg = directSegment.get();
                    // CORREÇÃO: Usar a constante, pois getMaxSpeed() não existe mais
                    double effectiveSpeed = Math.min(seg.getVelocidadeMaxima(), speedForEstimation); // <--- CORREÇÃO
                    double timeHours = (effectiveSpeed > 0 && seg.getComprimento() > 0) ? (seg.getComprimento() / effectiveSpeed) : Double.POSITIVE_INFINITY;
                    long timeMinutes = (!Double.isInfinite(timeHours) && !Double.isNaN(timeHours)) ? Math.round(timeHours * 60) : -1;
                    sb.append(String.format("   -> %s (ID: %d) | Dist: %.2f km | Est. Time: ~%s min%n",
                            destino.getStation(), destino.getIdEstacao(), seg.getComprimento(), (timeMinutes >= 0 ? String.valueOf(timeMinutes) : "N/A")));
                } else {
                    sb.append(String.format("   -> %s (ID: %d) (Segment info not found)%n", destino.getStation(), destino.getIdEstacao()));
                }
            }
        }
        sb.append("-".repeat(40) + "\n");
        return sb.toString();
    }

    /**
     * Returns a list of stations directly connected to the departure station.
     */
    // CORREÇÃO DE TIPO DE RETORNO
    public List<EuropeanStation> getDirectlyConnectedStations(int idEstacaoPartida) {
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
            if (neighborId != -1 && neighborId != idEstacaoPartida) {
                reachableStationIds.add(neighborId);
            }
        }

        // CORREÇÃO DE TIPO
        List<EuropeanStation> reachableStations = new ArrayList<>();
        for (int id : reachableStationIds) {
            estacaoRepo.findById(id).ifPresent(reachableStations::add);
        }

        reachableStations.sort(Comparator.comparingInt(EuropeanStation::getIdEstacao));

        return reachableStations;
    }

    /**
     * Helper method to format the fastest path result as a readable string.
     */
    // CORREÇÃO DE TIPO
    private String formatPathResult(RailwayPath path, EuropeanStation partida, EuropeanStation chegada, Locomotive locomotiva) {
        // ... (Corpo do método usa partida.getStation() e chegada.getStation()) ...
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Results for travel from %s to %s:%n", partida.getStation(), chegada.getStation()));
        // CORREÇÃO: Mudar de Max Speed para PowerKW e usar o valor da constante
        sb.append(String.format("   (Selected Locomotive: ID %d - %s, Power: %.0f kW, Max Speed Assumption: %.1f km/h)%n", // <--- MUDANÇA
                locomotiva.getIdLocomotiva(), locomotiva.getModelo(), locomotiva.getPowerKW(), LOCOMOTIVE_DEFAULT_MAX_SPEED)); // <--- MUDANÇA
        sb.append("-".repeat(60) + "\n");
        sb.append("   Fastest path (by segments):\n");

        int i = 1;
        int previousStationId = partida.getIdEstacao();
        double cumulativeTimeHours = 0.0;

        for (LineSegment seg : path.getSegments()) {
            int startId = seg.getIdEstacaoInicio();
            int endId = seg.getIdEstacaoFim();
            String startName, endName;

            // Determinar a direção correta do segmento no percurso
            // CORREÇÃO: Mudar o tipo nas buscas dentro do loop
            Optional<EuropeanStation> optStart = estacaoRepo.findById(startId);
            Optional<EuropeanStation> optEnd = estacaoRepo.findById(endId);

            if (startId == previousStationId) {
                // CORREÇÃO: Usar getStation()
                startName = optStart.map(EuropeanStation::getStation).orElse("ID " + startId);
                endName = optEnd.map(EuropeanStation::getStation).orElse("ID " + endId);
                previousStationId = endId;
            } else if (endId == previousStationId) {
                // CORREÇÃO: Usar getStation()
                startName = optEnd.map(EuropeanStation::getStation).orElse("ID " + endId);
                endName = optStart.map(EuropeanStation::getStation).orElse("ID " + startId);
                previousStationId = startId;
            } else {
                startName = optStart.map(s -> s.getStation() + " (Erro?)").orElse("ID " + startId);
                endName = optEnd.map(s -> s.getStation() + " (Erro?)").orElse("ID " + endId);
                System.err.println("Erro na sequência do caminho: Segmento " + seg.getIdSegmento() + " não conecta com a estação anterior " + previousStationId);
            }


            // CORREÇÃO: Usar a constante, pois getMaxSpeed() não existe mais
            double effectiveSpeed = Math.min(seg.getVelocidadeMaxima(), LOCOMOTIVE_DEFAULT_MAX_SPEED); // <--- CORREÇÃO
            double segmentTimeHours = Double.POSITIVE_INFINITY;
            if (effectiveSpeed > 0 && seg.getComprimento() > 0) {
                segmentTimeHours = seg.getComprimento() / effectiveSpeed;
            }

            if (!Double.isInfinite(segmentTimeHours) && !Double.isNaN(segmentTimeHours)) {
                cumulativeTimeHours += segmentTimeHours;
            } else {
                cumulativeTimeHours = Double.POSITIVE_INFINITY;
            }


            sb.append(String.format("   Segment %d: %s -> %s%n", i++, startName, endName));
            String segmentTimeStr = (!Double.isInfinite(segmentTimeHours) && !Double.isNaN(segmentTimeHours)) ? String.format("%.1f", segmentTimeHours * 60) : "N/A";
            String cumulativeTimeStr = (!Double.isInfinite(cumulativeTimeHours) && !Double.isNaN(cumulativeTimeHours)) ? String.format("%.1f", cumulativeTimeHours * 60) : "N/A";
            sb.append(String.format("      Dist: %.2f km | Eff. Speed: %.1f km/h | Time: %s min \n ",
                    seg.getComprimento(),
                    effectiveSpeed,
                    segmentTimeStr,
                    cumulativeTimeStr));
        }

        sb.append("-".repeat(60) + "\n");

        long totalMinutes = path.getTotalTimeMinutes();
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;

        String formattedTime;
        String formattedHours;
        if (Double.isInfinite(path.getTotalTimeHours()) || Double.isNaN(path.getTotalTimeHours())) {
            formattedTime = "N/A (unreachable or invalid segment speeds)";
            formattedHours = "N/A";
        } else {
            formattedTime = (hours > 0) ? String.format("%d hours and %d minutes", hours, minutes) : String.format("%d minutes", minutes);
            formattedHours = String.format("%.2f", path.getTotalTimeHours());
        }

        sb.append(String.format("   Total Distance: %.2f km%n", path.getTotalDistance()));
        sb.append(String.format("   Estimated Total Time: %s (%s hours)%n", formattedTime, formattedHours));
        sb.append("=".repeat(60) + "\n");

        return sb.toString();
    }

    public StationRepository getStationRepository() {
        return this.estacaoRepo;
    }

    public LocomotiveRepository getLocomotiveRepository() {
        return this.locomotivaRepo;
    }
}