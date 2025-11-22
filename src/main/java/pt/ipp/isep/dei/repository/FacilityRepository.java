package pt.ipp.isep.dei.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FacilityRepository {

    // Simulação de cache de Facilities (Populada com dados do DML USBD22)
    private final Map<Integer, String> facilityCache;

    public FacilityRepository() {
        this.facilityCache = new HashMap<>();
        // Inserções do seu DML
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
        // IDs maiores
        facilityCache.put(50, "Leixões");
        facilityCache.put(45, "São Mamede de Infesta");
        facilityCache.put(48, "Leça do Balio");
        facilityCache.put(43, "São Gemil");
    }

    public Optional<String> findNameById(int id) {
        return Optional.ofNullable(facilityCache.get(id));
    }

    /**
     * Retorna o cache completo de facilities (ID -> Nome) para ser usado em ComboBoxes.
     */
    public Map<Integer, String> findAllFacilityNames() {
        return facilityCache;
    }
}