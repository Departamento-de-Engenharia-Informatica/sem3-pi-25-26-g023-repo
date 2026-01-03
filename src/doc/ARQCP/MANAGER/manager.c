#include "manager.h"
#include "../assembly/usac01_encrypt_data.s"
#include "../assembly/usac02_decrypt_data.s"
#include "../assembly/usac03_extract_data.s"
#include "../assembly/usac04_format_command.s"
#include "../assembly/usac05_enqueue_value.s"
#include "../assembly/usac06_dequeue_value.s"
#include "../assembly/usac09_median.s"

// Declarações externas das funções assembly
extern int encrypt_data(char* in, int key, char* out);
extern int decrypt_data(char* in, int key, char* out);
extern int extract_data(char* str, char* token, char* unit, int* value);
extern int format_command(char* op, int n, char* cmd);
extern int enqueue_value(int* buffer, int length, int *nelem, int* tail, int* head, int value);
extern int dequeue_value(int* buffer, int length, int *nelem, int* tail, int* head, int *value);
extern int median(int* vec, int length, int *me);

// ============================================
// FUNÇÕES DE INICIALIZAÇÃO
// ============================================

StationManager* manager_create(void) {
    StationManager* manager = malloc(sizeof(StationManager));
    if (!manager) return NULL;

    // Inicializa contadores
    manager->user_count = 0;
    manager->track_count = 0;
    manager->train_count = 0;
    manager->log_count = 0;

    // Aloca arrays DINAMICAMENTE (requisito)
    manager->users = malloc(MAX_USERS * sizeof(User));
    manager->tracks = malloc(MAX_TRACKS * sizeof(Track));
    manager->trains = malloc(10 * sizeof(Train));  // 10 trens máximo
    manager->logs = malloc(MAX_LOGS * sizeof(LogEntry));

    // Buffers para processamento
    manager->sensor_buffer = malloc(256);
    manager->command_buffer = malloc(100);

    // Configuração padrão dos sensores
    manager->sensor_config.temp_buffer_len = 10;
    manager->sensor_config.temp_window_len = 5;
    manager->sensor_config.hum_buffer_len = 10;
    manager->sensor_config.hum_window_len = 5;

    // Estado inicial
    manager->current_user = NULL;
    manager->system_running = 1;
    manager->start_time = time(NULL);

    // Cria usuário admin padrão
    User admin;
    strcpy(admin.name, "Administrador");
    strcpy(admin.username, "admin");
    strcpy(admin.password, "DPLQJ");  // "ADMIN" encriptado com chave 3
    admin.cipher_key = 3;
    admin.type = USER_ADMIN;

    manager_add_user(manager, &admin);

    // Cria alguns trilhos padrão
    for (int i = 0; i < 5; i++) {
        Track track;
        track.id = i + 1;
        track.state = TRACK_FREE;
        track.train_id = -1;
        snprintf(track.description, MAX_NAME_LEN, "Trilho %d", i + 1);
        track.last_update = time(NULL);

        manager_add_track(manager, &track);
    }

    printf("✅ Manager criado com sucesso\n");
    printf("   • %d usuários\n", manager->user_count);
    printf("   • %d trilhos\n", manager->track_count);
    printf("   • Sistema iniciado em: %s", ctime(&manager->start_time));

    return manager;
}

void manager_destroy(StationManager* manager) {
    if (!manager) return;

    // Libera TODA a memória alocada dinamicamente
    free(manager->users);
    free(manager->tracks);
    free(manager->trains);
    free(manager->logs);
    free(manager->sensor_buffer);
    free(manager->command_buffer);
    free(manager);

    printf("✅ Manager destruído\n");
}

// ============================================
// SISTEMA DE LOGIN
// ============================================

User* manager_login(StationManager* manager, const char* username, const char* password) {
    if (!manager || !username || !password) return NULL;

    printf("\n🔐 Tentativa de login: %s\n", username);

    // Procura usuário
    for (int i = 0; i < manager->user_count; i++) {
        if (strcmp(manager->users[i].username, username) == 0) {
            User* user = &manager->users[i];

            // Desencripta senha armazenada para comparar
            char decrypted_password[MAX_NAME_LEN];
            if (!decrypt_data(user->password, user->cipher_key, decrypted_password)) {
                printf("❌ Erro ao desencriptar senha\n");
                manager_add_log(manager, "Login failed - decryption error", 0);
                return NULL;
            }

            // Compara senhas
            if (strcmp(decrypted_password, password) == 0) {
                manager->current_user = user;
                printf("✅ Login bem-sucedido: %s (%s)\n",
                       user->name, user->username);

                manager_add_log(manager, "User logged in", 1);
                return user;
            } else {
                printf("❌ Senha incorreta\n");
                manager_add_log(manager, "Login failed - wrong password", 0);
                return NULL;
            }
        }
    }

    printf("❌ Usuário não encontrado: %s\n", username);
    manager_add_log(manager, "Login failed - user not found", 0);
    return NULL;
}

void manager_logout(StationManager* manager) {
    if (!manager || !manager->current_user) return;

    printf("\n👋 Logout: %s\n", manager->current_user->name);
    manager_add_log(manager, "User logged out", 1);
    manager->current_user = NULL;
}

// ============================================
// PROCESSAMENTO DE COMANDOS
// ============================================

int manager_process_command(StationManager* manager, const char* command) {
    if (!manager || !command) return 0;

    printf("\n📨 Processando comando: %s\n", command);

    // Primeiro solicita dados dos sensores
    manager_request_sensor_data(manager);

    // Processa comando específico
    if (strncmp(command, "TRACK_ASSIGN", 12) == 0) {
        int track_id, train_id;
        if (sscanf(command, "TRACK_ASSIGN %d %d", &track_id, &train_id) == 2) {
            // Procura trilho
            for (int i = 0; i < manager->track_count; i++) {
                if (manager->tracks[i].id == track_id) {
                    if (manager->tracks[i].state == TRACK_FREE) {
                        manager->tracks[i].state = TRACK_ASSIGNED;
                        manager->tracks[i].train_id = train_id;
                        manager->tracks[i].last_update = time(NULL);

                        // Envia comando para LightSigns
                        char cmd[20];
                        if (format_command("YE", track_id, cmd)) {
                            manager_send_to_lightsigns(manager, cmd);
                        }

                        // Atualiza Board
                        char board_msg[100];
                        snprintf(board_msg, 100, "Track %d assigned to train %d",
                                track_id, train_id);
                        manager_send_to_board(manager, board_msg);

                        manager_add_log(manager, "Track assigned to train", 1);
                        return 1;
                    } else {
                        printf("❌ Trilho %d não está livre\n", track_id);
                        manager_add_log(manager, "Track assignment failed - not free", 0);

                        // Ordem de parada de emergência
                        manager_send_to_lightsigns(manager, "EMERGENCY_STOP");
                        return 0;
                    }
                }
            }
        }
    }
    else if (strncmp(command, "TRACK_FREE", 10) == 0) {
        int track_id;
        if (sscanf(command, "TRACK_FREE %d", &track_id) == 1) {
            for (int i = 0; i < manager->track_count; i++) {
                if (manager->tracks[i].id == track_id) {
                    manager->tracks[i].state = TRACK_FREE;
                    manager->tracks[i].train_id = -1;
                    manager->tracks[i].last_update = time(NULL);

                    // LightSigns: verde
                    char cmd[20];
                    if (format_command("GE", track_id, cmd)) {
                        manager_send_to_lightsigns(manager, cmd);
                    }

                    manager_add_log(manager, "Track freed", 1);
                    return 1;
                }
            }
        }
    }
    else if (strncmp(command, "GET_SENSORS", 11) == 0) {
        manager_request_sensor_data(manager);
        return 1;
    }
    else if (strncmp(command, "SHOW_BOARD", 10) == 0) {
        manager_display_status(manager);
        return 1;
    }
    else if (strncmp(command, "EXIT", 4) == 0) {
        manager->system_running = 0;
        printf("🛑 Sistema a encerrar...\n");
        return 1;
    }

    printf("❌ Comando não reconhecido: %s\n", command);
    manager_add_log(manager, "Unknown command", 0);
    return 0;
}

// ============================================
// INTEGRAÇÃO COM COMPONENTES
// ============================================

void manager_request_sensor_data(StationManager* manager) {
    if (!manager) return;

    printf("📡 Solicitando dados dos sensores...\n");

    // Simula envio do comando GTH para o componente Sensors
    printf("→ Enviando para Sensors: %s\n", SENSOR_CMD);

    // Simula resposta do sensor (normalmente via serial/GPIO)
    char sensor_response[] = "TEMP&unit::celsius&value::23#HUM&unit::percentage&value::65";
    printf("← Recebido de Sensors: %s\n", sensor_response);

    // Processa dados usando função assembly
    manager_update_sensor_data(manager, sensor_response);
}

void manager_update_sensor_data(StationManager* manager, const char* sensor_str) {
    if (!manager || !sensor_str) return;

    // USA FUNÇÃO ASSEMBLY para extrair dados
    char unit[20];
    int value;

    // Extrai temperatura
    if (extract_data(sensor_str, "TEMP", unit, &value)) {
        manager->sensor_config.temperature = (float)value;
        printf("🌡️  Temperatura: %d %s\n", value, unit);
    }

    // Extrai humidade
    if (extract_data(sensor_str, "HUM", unit, &value)) {
        manager->sensor_config.humidity = (float)value;
        printf("💧 Humidade: %d %s\n", value, unit);
    }

    manager->sensor_config.timestamp = time(NULL);
}

void manager_send_to_board(StationManager* manager, const char* data) {
    if (!manager || !data) return;

    printf("📊 Enviando para Board: %s\n", data);

    // Formata dados para o Board (formato divertido)
    char board_display[256];
    snprintf(board_display, 256,
             "\n╔══════════════════════════════╗\n"
             "║   🚂 ESTAÇÃO FERROVIÁRIA   ║\n"
             "╠══════════════════════════════╣\n"
             "║  %-25s  ║\n"
             "║  🌡️ %.1f°C  💧 %.1f%%      ║\n"
             "╚══════════════════════════════╝\n",
             data,
             manager->sensor_config.temperature,
             manager->sensor_config.humidity);

    printf("%s", board_display);
}

void manager_send_to_lightsigns(StationManager* manager, const char* command) {
    if (!manager || !command) return;

    printf("💡 Enviando para LightSigns: %s\n", command);

    // Formata comando usando função assembly USAC04
    if (strncmp(command, "YE", 2) == 0 ||
        strncmp(command, "GE", 2) == 0 ||
        strncmp(command, "RE", 2) == 0 ||
        strncmp(command, "RB", 2) == 0) {

        int track_num;
        char op[10];
        sscanf(command, "%[^,],%d", op, &track_num);

        char formatted_cmd[20];
        if (format_command(op, track_num, formatted_cmd)) {
            printf("   Comando formatado: %s\n", formatted_cmd);
        }
    }
}

// ============================================
// GESTÃO DE DADOS
// ============================================

int manager_add_user(StationManager* manager, const User* user) {
    if (!manager || !user || manager->user_count >= MAX_USERS) return 0;

    // Encripta senha usando assembly USAC01
    char encrypted_password[MAX_NAME_LEN];
    if (!encrypt_data((char*)user->password, user->cipher_key, encrypted_password)) {
        printf("❌ Erro ao encriptar senha do usuário %s\n", user->username);
        return 0;
    }

    // Copia usuário
    manager->users[manager->user_count] = *user;
    strcpy(manager->users[manager->user_count].password, encrypted_password);
    manager->user_count++;

    printf("✅ Usuário adicionado: %s\n", user->username);
    return 1;
}

int manager_add_track(StationManager* manager, const Track* track) {
    if (!manager || !track || manager->track_count >= MAX_TRACKS) return 0;

    manager->tracks[manager->track_count] = *track;
    manager->track_count++;

    return 1;
}

int manager_add_log(StationManager* manager, const char* action, int success) {
    if (!manager || !action || manager->log_count >= MAX_LOGS) return 0;

    LogEntry* log = &manager->logs[manager->log_count];
    log->id = manager->log_count + 1;
    log->user_id = manager->current_user ? 1 : 0;  // ID do usuário atual
    strncpy(log->action, action, 99);
    log->timestamp = time(NULL);
    log->success = success;

    manager->log_count++;

    // Também escreve em arquivo
    FILE* log_file = fopen("station_log.txt", "a");
    if (log_file) {
        fprintf(log_file, "[%s] %s - %s\n",
                ctime(&log->timestamp),
                action,
                success ? "SUCCESS" : "FAILED");
        fclose(log_file);
    }

    return 1;
}

// ============================================
// FUNÇÕES UTILITÁRIAS
// ============================================

void manager_display_status(const StationManager* manager) {
    if (!manager) return;

    printf("\n📋 STATUS DO SISTEMA\n");
    printf("===================\n");

    // Informações gerais
    printf("Usuário atual: %s\n",
           manager->current_user ? manager->current_user->name : "Nenhum");
    printf("Tempo de execução: %ld segundos\n",
           time(NULL) - manager->start_time);

    // Sensores
    printf("\nSENSORES:\n");
    printf("  Temperatura: %.1f °C\n", manager->sensor_config.temperature);
    printf("  Humidade: %.1f %%\n", manager->sensor_config.humidity);

    // Trilhos
    printf("\nTRILHOS (%d):\n", manager->track_count);
    for (int i = 0; i < manager->track_count; i++) {
        const char* state;
        switch (manager->tracks[i].state) {
            case TRACK_FREE: state = "🟢 LIVRE"; break;
            case TRACK_ASSIGNED: state = "🟡 ATRIBUIDO"; break;
            case TRACK_BUSY: state = "🔴 OCUPADO"; break;
            case TRACK_INOPERATIVE: state = "⚫ INOPERATIVO"; break;
            default: state = "❓ DESCONHECIDO";
        }

        printf("  %d: %s", manager->tracks[i].id, state);
        if (manager->tracks[i].train_id != -1) {
            printf(" (Trem %d)", manager->tracks[i].train_id);
        }
        printf("\n");
    }

    // Logs recentes
    printf("\nÚLTIMAS AÇÕES:\n");
    int start = manager->log_count > 5 ? manager->log_count - 5 : 0;
    for (int i = start; i < manager->log_count; i++) {
        printf("  [%d] %s\n", i+1, manager->logs[i].action);
    }
}

int manager_verify_integrity(const StationManager* manager) {
    if (!manager) return 0;

    printf("\n🔍 Verificando integridade do sistema...\n");

    int errors = 0;

    // Verifica IDs únicos de trilhos
    for (int i = 0; i < manager->track_count; i++) {
        for (int j = i + 1; j < manager->track_count; j++) {
            if (manager->tracks[i].id == manager->tracks[j].id) {
                printf("❌ IDs de trilho duplicados: %d\n", manager->tracks[i].id);
                errors++;
            }
        }
    }

    // Verifica trilhos com estados inválidos
    for (int i = 0; i < manager->track_count; i++) {
        if (manager->tracks[i].state == TRACK_INOPERATIVE &&
            manager->tracks[i].train_id != -1) {
            printf("❌ Trilho %d inoperativo mas tem trem atribuído\n",
                   manager->tracks[i].id);
            errors++;
        }
    }

    if (errors == 0) {
        printf("✅ Integridade verificada - sem erros\n");
        return 1;
    } else {
        printf("⚠️  %d erro(s) encontrado(s)\n", errors);
        return 0;
    }
}