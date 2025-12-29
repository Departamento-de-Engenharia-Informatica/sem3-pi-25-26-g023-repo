#include <stdio.h>
#include <stdlib.h>
#include "setup_manager.h"

int main() {
    printf("=== TESTE USAC11 - Setup Inicial via Ficheiro ===\n\n");

    StationSystem system;

    printf("1. Inicializando sistema...\n");
    if (!initialize_station_system(&system, "data/config.txt")) {
        printf("FALHA: Não foi possível inicializar o sistema\n");
        return 1;
    }
    printf("OK: Sistema inicializado\n\n");

    printf("2. Validando sistema...\n");
    if (!validate_system(&system)) {
        printf("FALHA: Sistema inválido\n");
        cleanup_station_system(&system);
        return 1;
    }
    printf("OK: Sistema válido\n\n");

    printf("3. Mostrando resumo...\n");
    print_system_summary(&system);

    printf("4. Salvando estado do sistema...\n");
    if (save_system_state(&system, "system_state.txt")) {
        printf("OK: Estado salvo em 'system_state.txt'\n");
    } else {
        printf("AVISO: Não foi possível salvar o estado\n");
    }

    printf("\n5. Limpando recursos...\n");
    cleanup_station_system(&system);
    printf("OK: Recursos libertados\n\n");

    printf("=== TESTE CONCLUÍDO COM SUCESSO ===\n");
    return 0;
}