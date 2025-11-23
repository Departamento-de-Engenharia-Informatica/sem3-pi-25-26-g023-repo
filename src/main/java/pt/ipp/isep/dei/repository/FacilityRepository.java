package pt.ipp.isep.dei.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Repository class that simulates access to facility data (stations, intersections, etc.).
 *
 * <p>It uses an in-memory map (cache) populated with fixed data for quick lookup,
 * avoiding direct database access for frequent name lookups.</p>
 */
public class FacilityRepository {

    // Simulation of Facility cache (Populated with data from DML USBD22)
    private final Map<Integer, String> facilityCache;

    /**
     * Constructs the Facility Repository and initializes the in-memory cache.
     */
    public FacilityRepository() {
        this.facilityCache = new HashMap<>();
        // Insertions from your DML
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
        // Higher IDs
        facilityCache.put(50, "Leixões");
        facilityCache.put(45, "São Mamede de Infesta");
        facilityCache.put(48, "Leça do Balio");
        facilityCache.put(43, "São Gemil");
    }

    /**
     * Finds the name of a facility by its ID.
     *
     * @param id The unique identifier of the facility.
     * @return An {@link Optional} containing the facility name if found, or empty otherwise.
     */
    public Optional<String> findNameById(int id) {
        return Optional.ofNullable(facilityCache.get(id));
    }

    /**
     * Returns the complete facility cache (ID -> Name) to be used in ComboBoxes or other selectors.
     *
     * @return The map containing all facility IDs and names.
     */
    public Map<Integer, String> findAllFacilityNames() {
        return facilityCache;
    }
}