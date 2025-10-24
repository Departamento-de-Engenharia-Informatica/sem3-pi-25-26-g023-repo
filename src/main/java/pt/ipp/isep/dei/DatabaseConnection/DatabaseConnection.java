package pt.ipp.isep.dei.DatabaseConnection; // O pacote que você quer

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Classe utilitária SIMPLES para obter conexão à base de dados Oracle.
 * ATENÇÃO: As credenciais estão diretamente no código (não recomendado para produção).
 */
public class DatabaseConnection {

    // --- Detalhes da Conexão (Substitua se necessário) ---
    private static final String DB_URL = "jdbc:oracle:thin:@vsgate-s1.dei.isep.ipp.pt:10945:xe";
    private static final String DB_USER = "system";
    private static final String DB_PASSWORD = "oracle";
    // --- Fim dos Detalhes ---

    // Tenta carregar o driver uma vez quando a classe é carregada.
    static {
        try {
            // Garante que o driver JDBC da Oracle está carregado
            Class.forName("oracle.jdbc.driver.OracleDriver");
            System.out.println("Driver Oracle JDBC carregado com sucesso.");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ ERRO FATAL: Driver Oracle JDBC não encontrado no classpath!");
            System.err.println("   Verifique se a dependência Maven 'ojdbc' está correta no pom.xml.");
            e.printStackTrace();
            // Lançar uma exceção pode ser melhor para parar a aplicação se o driver for essencial
            // throw new RuntimeException("Driver Oracle JDBC não encontrado", e);
        }
    }

    /**
     * Obtém uma nova conexão à base de dados usando os detalhes definidos nesta classe.
     * QUEM CHAMA ESTE MÉTODO É RESPONSÁVEL POR FECHAR A CONEXÃO!
     * (Preferencialmente usando try-with-resources)
     *
     * @return Uma instância de Connection.
     * @throws SQLException Se ocorrer um erro ao estabelecer a conexão.
     */
    public static Connection getConnection() throws SQLException {
        // Estabelece e retorna a conexão usando os dados estáticos da classe
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    /**
     * Testa a conexão à base de dados.
     * Imprime uma mensagem de sucesso ou falha na consola.
     * Retorna true se a conexão for bem-sucedida, false caso contrário.
     */
    public static boolean testConnection() {
        System.out.println("\n--- Testando Conexão à Base de Dados ---");
        System.out.println("   URL: " + DB_URL);
        System.out.println("   User: " + DB_USER);

        // Usar try-with-resources garante que a conexão é fechada automaticamente
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Conexão estabelecida com sucesso!");
                System.out.println("----------------------------------------");
                return true; // Conexão OK
            } else {
                System.err.println("❌ Falha ao obter conexão (retornou null ou fechada?).");
            }
        } catch (SQLException e) {
            System.err.println("❌ FALHA NA CONEXÃO: " + e.getMessage());
            // Para debug, descomente a linha abaixo para ver mais detalhes do erro:
            // e.printStackTrace();
        } catch (Exception e) { // Captura outros erros inesperados
            System.err.println("❌ Erro inesperado durante o teste de conexão: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("----------------------------------------");
        return false; // Falha na conexão
    }

    /**
     * Método main para executar um teste rápido e isolado desta classe.
     * @param args Argumentos da linha de comando (não usados).
     */
    public static void main(String[] args) {
        // Executa o teste de conexão quando esta classe é corrida diretamente
        boolean sucesso = testConnection();
        if (sucesso) {
            System.out.println("Teste concluído: SUCESSO.");
        } else {
            System.out.println("Teste concluído: FALHA.");
        }
    }
}