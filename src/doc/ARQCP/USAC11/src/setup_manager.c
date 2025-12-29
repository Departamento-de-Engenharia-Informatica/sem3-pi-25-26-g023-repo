#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include "setup_manager.h"
#include "asm_functions.h"  // Para encrypt_data

int initialize_station_system(StationSystem* system, const char* configFile) {
    // Inicializar a zero
    memset(system, 0, sizeof(StationSystem));

    // Carregar configuração
    if (!load_config_file(configFile, &system->config)) {
        printf("Erro ao carregar ficheiro de configuração: %s\n", configFile);
        return 0;
    }

    if (!validate_config(&system->config)) {
        printf("Configuração inválida\n");
        return 0;
    }

    // Carregar utilizadores
    if (!load_users_file("data/users.txt", system->users, &system->userCount)) {
        printf("Erro ao carregar utilizadores\n");
        return 0;
    }

    // Carregar vias
    if (!load_tracks_file("data/tracks.txt", system->tracks, &system->trackCount)) {
        printf("Erro ao carregar vias\n");
        return 0;
    }

    // Alocar buffers dos sensores dinamicamente
    system->tempBufferData = malloc(system->config.tempBufferSize * sizeof(int));
    system->humidBufferData = malloc(system->config.humidBufferSize * sizeof(int));

    if (!system->tempBufferData || !system->humidBufferData) {
        printf("Erro de alocação de memória para buffers\n");
        free(system->tempBufferData);
        free(system->humidBufferData);
        return 0;
    }

    // Inicializar buffers de temperatura
    system->tempBuffer.buffer = system->tempBufferData;
    system->tempBuffer.length = system->config.tempBufferSize;
    system->tempBuffer.window = system->config.tempWindowSize;
    system->tempBuffer.nelem = 0;
    system->tempBuffer.head = 0;
    system->tempBuffer.tail = 0;

    // Inicializar buffers de humidade
    system->humidBuffer.buffer = system->humidBufferData;
    system->humidBuffer.length = system->config.humidBufferSize;
    system->humidBuffer.window = system->config.humidWindowSize;
    system->humidBuffer.nelem = 0;
    system->humidBuffer.head = 0;
    system->humidBuffer.tail = 0;

    // Encriptar passwords (se não estiverem já encriptadas)
    for (int i = 0; i < system->userCount; i++) {
        // Verificar se password já está encriptada (suposição: se tem apenas maiúsculas)
        int needs_encryption = 1;
        char* pass = system->users[i].password;

        for (int j = 0; pass[j]; j++) {
            if (pass[j] < 'A' || pass[j] > 'Z') {
                needs_encryption = 0;
                break;
            }
        }

        if (!needs_encryption) {
            char encrypted[MAX_PASSWORD_LEN];
            if (encrypt_data(pass, system->users[i].cipherKey, encrypted)) {
                strcpy(system->users[i].password, encrypted);
            }
        }
    }

    printf("Sistema inicializado com sucesso:\n");
    printf("  - Estação: %s (ID: %d)\n", system->config.stationName, system->config.stationId);
    printf("  - Vias: %d\n", system->trackCount);
    printf("  - Utilizadores: %d\n", system->userCount);

    return 1;
}

void cleanup_station_system(StationSystem* system) {
    if (system->tempBufferData) {
        free(system->tempBufferData);
        system->tempBufferData = NULL;
    }

    if (system->humidBufferData) {
        free(system->humidBufferData);
        system->humidBufferData = NULL;
    }

    system->tempBuffer.buffer = NULL;
    system->humidBuffer.buffer = NULL;
}

int save_system_state(const StationSystem* system, const char* filename) {
    FILE* file = fopen(filename, "w");
    if (!file) {
        printf("Erro ao criar ficheiro de estado: %s\n", filename);
        return 0;
    }

    fprintf(file, "# Estado do Sistema - %s\n", ctime(&(time_t){time(NULL)}));
    fprintf(file, "station_id=%d\n", system->config.stationId);
    fprintf(file, "station_name=%s\n", system->config.stationName);
    fprintf(file, "user_count=%d\n", system->userCount);
    fprintf(file, "track_count=%d\n", system->trackCount);
    fprintf(file, "temp_buffer_nelem=%d\n", system->tempBuffer.nelem);
    fprintf(file, "humid_buffer_nelem=%d\n", system->humidBuffer.nelem);

    fclose(file);
    return 1;
}

int load_system_state(StationSystem* system, const char* filename) {
    // Implementação simplificada
    printf("Função load_system_state não implementada completamente\n");
    return 0;
}

int validate_system(const StationSystem* system) {
    if (system->trackCount > system->config.maxTracks) {
        printf("Erro: Número de vias excede o máximo configurado\n");
        return 0;
    }

    if (system->userCount > system->config.maxUsers) {
        printf("Erro: Número de utilizadores excede o máximo configurado\n");
        return 0;
    }

    // Verificar IDs únicos de vias
    for (int i = 0; i < system->trackCount; i++) {
        for (int j = i + 1; j < system->trackCount; j++) {
            if (system->tracks[i].id == system->tracks[j].id) {
                printf("Erro: ID duplicado de via: %d\n", system->tracks[i].id);
                return 0;
            }
        }
    }

    return 1;
}

void print_system_summary(const StationSystem* system) {
    printf("\n=== RESUMO DO SISTEMA ===\n");
    printf("Estação: %s (ID: %d)\n", system->config.stationName, system->config.stationId);
    printf("Configuração:\n");
    printf("  - Máx. vias: %d (atuais: %d)\n", system->config.maxTracks, system->trackCount);
    printf("  - Máx. utilizadores: %d (atuais: %d)\n", system->config.maxUsers, system->userCount);
    printf("  - Buffer temperatura: %d/%d elementos\n", system->tempBuffer.nelem, system->tempBuffer.length);
    printf("  - Buffer humidade: %d/%d elementos\n", system->humidBuffer.nelem, system->humidBuffer.length);

    printf("\nUtilizadores:\n");
    for (int i = 0; i < system->userCount; i++) {
        const char* role_str = "";
        switch(system->users[i].role) {
            case ROLE_ADMIN: role_str = "Admin"; break;
            case ROLE_OPERATOR: role_str = "Operador"; break;
            case ROLE_TRAFFIC_MANAGER: role_str = "Gestor Tráfego"; break;
            case ROLE_VIEWER: role_str = "Visualizador"; break;
        }
        printf("  %-20s (%s) - Chave: %d\n",
               system->users[i].username, role_str, system->users[i].cipherKey);
    }

    printf("\nVias:\n");
    for (int i = 0; i < system->trackCount; i++) {
        const char* state_str = "";
        switch(system->tracks[i].state) {
            case TRACK_FREE: state_str = "Livre"; break;
            case TRACK_BUSY: state_str = "Ocupada"; break;
            case TRACK_ASSIGNED: state_str = "Atribuída"; break;
            case TRACK_INOPERATIVE: state_str = "Inoperacional"; break;
        }
        printf("  Via %2d: %-15s ", system->tracks[i].id, state_str);
        if (system->tracks[i].trainId != -1) {
            printf("- Comboio: %d", system->tracks[i].trainId);
        }
        printf("\n");
    }
    printf("==========================\n\n");
}