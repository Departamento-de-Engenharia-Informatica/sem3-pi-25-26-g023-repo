#include "manager.h"

// Loop principal do Manager (algoritmo da página 14)
void manager_main_loop(StationManager* manager) {
    if (!manager) return;

    printf("\n🔄 Iniciando loop principal do Manager...\n");

    char instruction[100];

    while (manager->system_running) {
        // 1. Solicita dados dos sensores
        manager_request_sensor_data(manager);

        // 2. Aguarda instruções da UI (simulado)
        printf("\n📝 Insira comando (ou 'exit' para sair): ");
        if (fgets(instruction, sizeof(instruction), stdin)) {
            instruction[strcspn(instruction, "\n")] = '\0';

            if (strlen(instruction) == 0) continue;

            // 3. Processa instrução
            if (manager_process_command(manager, instruction)) {
                printf("✅ Comando executado com sucesso\n");
            }

            // 4. Atualiza Board
            manager_send_to_board(manager, "Sistema operacional");

            // 5. Regista ação
            manager_add_log(manager, instruction, 1);
        }
    }

    printf("\n🛑 Loop principal terminado\n");
}

// Função principal para testar o Manager
int main(void) {
    printf("🚂 STATION MANAGER - Sistema de Gestão de Estação\n");
    printf("================================================\n");

    // Cria manager
    StationManager* manager = manager_create();
    if (!manager) {
        printf("❌ Erro ao criar manager\n");
        return 1;
    }

    // Login
    printf("\n=== LOGIN ===\n");
    User* user = manager_login(manager, "admin", "ADMIN");
    if (!user) {
        manager_destroy(manager);
        return 1;
    }

    // Verifica integridade
    manager_verify_integrity(manager);

    // Mostra status inicial
    manager_display_status(manager);

    // Inicia loop principal
    manager_main_loop(manager);

    // Logout e cleanup
    manager_logout(manager);
    manager_destroy(manager);

    printf("\n✅ Programa terminado\n");
    return 0;
}