package pt.ipp.isep.dei.repository;

import pt.ipp.isep.dei.DatabaseConnection.DatabaseConnection; // Importa a classe de conexão
import pt.ipp.isep.dei.domain.Estacao;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet; // Para guardar IDs únicos
import java.util.List;
import java.util.Optional;
import java.util.Set; // Para guardar IDs únicos
import java.util.Comparator; // Para ordenar
import java.util.stream.Collectors; // Para ordenar

public class EstacaoRepository {

    private List<Estacao> loadedEstacoes = null;

    // Construtor vazio
    public EstacaoRepository() {
        // System.out.println("EstacaoRepository: Initialized (will connect to DB and load stations on first use)."); // <-- COMENTADO
    }

    /**
     * Carrega as estações da base de dados que são mencionadas na tabela RAILWAY_LINE.
     * Usa uma cache simples para evitar múltiplas leituras.
     */
    private void loadStationsIfNeeded() {
        if (loadedEstacoes != null) return; // Já carregado

        loadedEstacoes = new ArrayList<>();
        Set<Integer> uniqueFacilityIds = new HashSet<>();
        String sqlGetIds = "SELECT start_facility_id, end_facility_id FROM RAILWAY_LINE"; //

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmtIds = conn.prepareStatement(sqlGetIds);
             ResultSet rsIds = stmtIds.executeQuery()) {
            while (rsIds.next()) {
                uniqueFacilityIds.add(rsIds.getInt("start_facility_id"));
                uniqueFacilityIds.add(rsIds.getInt("end_facility_id"));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erro ao buscar IDs de Facilidades da RAILWAY_LINE: " + e.getMessage());
            loadedEstacoes = new ArrayList<>(); return;
        }

        if (uniqueFacilityIds.isEmpty()) {
            // System.out.println("EstacaoRepository: Nenhum ID de facility encontrado na RAILWAY_LINE."); // <-- COMENTADO
            loadedEstacoes = new ArrayList<>(); return;
        }

        String sqlGetDetailsBase = "SELECT facility_id, name FROM FACILITY WHERE facility_id IN ("; //
        StringBuilder sqlGetDetails = new StringBuilder(sqlGetDetailsBase);
        sqlGetDetails.append(uniqueFacilityIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
        sqlGetDetails.append(") ORDER BY facility_id");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmtDetails = conn.prepareStatement(sqlGetDetails.toString());
             ResultSet rsDetails = stmtDetails.executeQuery()) {
            while (rsDetails.next()) {
                loadedEstacoes.add(new Estacao(rsDetails.getInt("facility_id"), rsDetails.getString("name"))); //
            }
            // System.out.println("EstacaoRepository: Loaded " + loadedEstacoes.size() + " stations from RAILWAY_LINE references."); // <-- COMENTADO
        } catch (SQLException e) {
            System.err.println("❌ Erro ao buscar detalhes das Facilidades selecionadas: " + e.getMessage());
            loadedEstacoes = new ArrayList<>();
        }
    }

    // Métodos findAll, findById, findByNome permanecem iguais (eles chamam loadStationsIfNeeded)
    public List<Estacao> findAll() { loadStationsIfNeeded(); return new ArrayList<>(loadedEstacoes); }
    public Optional<Estacao> findById(int id) { loadStationsIfNeeded(); return loadedEstacoes.stream().filter(e -> e.getIdEstacao() == id).findFirst(); }
    public Optional<Estacao> findByNome(String nome) { loadStationsIfNeeded(); return loadedEstacoes.stream().filter(e -> e.getNome().equalsIgnoreCase(nome)).findFirst(); }
}