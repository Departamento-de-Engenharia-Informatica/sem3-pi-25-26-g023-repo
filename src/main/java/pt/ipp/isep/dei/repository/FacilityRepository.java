package pt.ipp.isep.dei.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Repository class that simulates access to facility data (stations, intersections, etc.).
 * Populated with fixed data matching the USBD32 SQL script.
 */
public class FacilityRepository {

    private final Map<Integer, String> facilityCache;

    public FacilityRepository() {
        this.facilityCache = new HashMap<>();

        // Dados extraídos do SQL (USBD32)
        facilityCache.put(1, "São Romão");
        facilityCache.put(2, "Tamel");
        facilityCache.put(3, "Senhora das Dores");
        facilityCache.put(4, "Lousado");
        facilityCache.put(5, "Porto Campanhã");
        facilityCache.put(6, "Leandro");
        facilityCache.put(7, "Porto São Bento");
        facilityCache.put(8, "Barcelos");
        facilityCache.put(9, "Vila Nova da Cerveira");
        facilityCache.put(10, "Midões");
        facilityCache.put(11, "Valença");
        facilityCache.put(12, "Darque");
        facilityCache.put(13, "Contumil");
        facilityCache.put(14, "Ermesinde");
        facilityCache.put(15, "São Frutuoso");
        facilityCache.put(16, "São Pedro da Torre");
        facilityCache.put(17, "Viana do Castelo");
        facilityCache.put(18, "Famalicão");
        facilityCache.put(19, "Barroselas");
        facilityCache.put(20, "Nine");
        facilityCache.put(21, "Caminha");
        facilityCache.put(22, "Carvalha");
        facilityCache.put(23, "Carreço");

        // IDs mais altos
        facilityCache.put(30, "Braga");
        facilityCache.put(31, "Manzagão");
        facilityCache.put(32, "Cerqueiral");
        facilityCache.put(33, "Gemieira");
        facilityCache.put(35, "Paredes de Coura");

        facilityCache.put(43, "São Gemil");
        facilityCache.put(45, "São Mamede de Infesta");
        facilityCache.put(48, "Leça do Balio");
        facilityCache.put(50, "Leixões");
    }

    public Optional<String> findNameById(int id) {
        return Optional.ofNullable(facilityCache.get(id));
    }

    public Map<Integer, String> findAllFacilityNames() {
        return facilityCache;
    }
}