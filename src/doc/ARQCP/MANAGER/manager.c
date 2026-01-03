#include "manager.h"
#include "serial_linux.h"



// Declarações externas das funções assembly
extern int encrypt_data(char* in, int key, char* out);
extern int decrypt_data(char* in, int key, char* out);
extern int extract_data(char* str, char* token, char* unit, int* value);
extern int format_command(char* op, int n, char* cmd);
// extern int enqueue_value(...); // Adiciona se usares
// extern int dequeue_value(...); // Adiciona se usares
// extern int median(...);        // Adiciona se usares

// ============================================
// FUNÇÕES DE INICIALIZAÇÃO
// ============================================

StationManager* manager_create(void) {
    StationManager* manager = (StationManager*) malloc(sizeof(StationManager));
    if (!manager) return NULL;

    // Inicializa contadores
    manager->user_count = 0;
    manager->track_count = 0;
    manager->train_count = 0;
    manager->log_count = 0;

    // Aloca arrays DINAMICAMENTE
    manager->users = (User*) malloc(MAX_USERS * sizeof(User));
    manager->tracks = (Track*) malloc(MAX_TRACKS * sizeof(Track));
    manager->trains = (Train*) malloc(10 * sizeof(Train));  // 10 comboios máximo
    manager->logs = (LogEntry*) malloc(MAX_LOGS * sizeof(LogEntry));

    // Buffers
    manager->sensor_buffer = (char*) malloc(256);
    manager->command_buffer = (char*) malloc(100);

    // Configuração padrão dos sensores
    manager->sensor_config.temp_buffer_len = 10;
    manager->sensor_config.temp_window_len = 5;
    manager->sensor_config.hum_buffer_len = 10;
    manager->sensor_config.hum_window_len = 5;
    manager->sensor_config.temperature = 0;
    manager->sensor_config.humidity = 0;

    // Estado inicial
    manager->current_user = NULL;
    manager->system_running = 1;
    manager->start_time = time(NULL);

    // --- LIGAÇÃO AO ARDUINO (NOVO) ---
    printf("--- A TENTAR LIGAR AO ARDUINO ---\n");
    // Tenta portas comuns no Linux
    manager->serial_fd = serial_open("/dev/ttyACM0");
    if (manager->serial_fd < 0) {
        manager->serial_fd = serial_open("/dev/ttyUSB0");
    }

    if (manager->serial_fd > 0) {
        printf("✅ SUCESSO: Arduino ligado!\n");
    } else {
        printf("⚠️ AVISO: Arduino não encontrado. A rodar em modo SIMULAÇÃO.\n");
    }

    // Cria utilizador admin padrão
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
    return manager;
}

void manager_destroy(StationManager* manager) {
    if (!manager) return;

    // Fechar porta serial se aberta
    if (manager->serial_fd > 0) {
        serial_close(manager->serial_fd);
        printf("🔌 Conexão Arduino fechada.\n");
    }

    // Libera memória
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

    for (int i = 0; i < manager->user_count; i++) {
        if (strcmp(manager->users[i].username, username) == 0) {
            User* user = &manager->users[i];

            char decrypted_password[MAX_NAME_LEN];
            // Chama Assembly para desencriptar
            if (!decrypt_data(user->password, user->cipher_key, decrypted_password)) {
                printf("❌ Erro ao desencriptar senha\n");
                return NULL;
            }

            if (strcmp(decrypted_password, password) == 0) {
                manager->current_user = user;
                printf("✅ Login com sucesso: %s\n", user->name);
                manager_add_log(manager, "User logged in", 1);
                return user;
            } else {
                printf("❌ Senha incorreta\n");
                manager_add_log(manager, "Login failed - wrong password", 0);
                return NULL;
            }
        }
    }

    printf("❌ Utilizador não encontrado\n");
    return NULL;
}

void manager_logout(StationManager* manager) {
    if (!manager || !manager->current_user) return;
    printf("\n👋 Logout: %s\n", manager->current_user->name);
    manager->current_user = NULL;
}

// ============================================
// PROCESSAMENTO DE COMANDOS
// ============================================

int manager_process_command(StationManager* manager, const char* command) {
    if (!manager || !command) return 0;

    // Atualiza sensores antes de qualquer ação
    manager_request_sensor_data(manager);

    if (strncmp(command, "TRACK_ASSIGN", 12) == 0) {
        int track_id, train_id;
        if (sscanf(command, "TRACK_ASSIGN %d %d", &track_id, &train_id) == 2) {
            for (int i = 0; i < manager->track_count; i++) {
                if (manager->tracks[i].id == track_id) {
                    if (manager->tracks[i].state == TRACK_FREE) {
                        manager->tracks[i].state = TRACK_ASSIGNED;
                        manager->tracks[i].train_id = train_id;

                        // Envia comando para LightSigns (ex: "YE,1" para Amarelo no trilho 1)
                        char cmd[20];
                        snprintf(cmd, 20, "YE,%d", track_id);
                        manager_send_to_lightsigns(manager, cmd);

                        printf("✅ Trilho %d atribuído ao comboio %d\n", track_id, train_id);
                        return 1;
                    } else {
                        printf("❌ Trilho %d ocupado!\n", track_id);
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

                    // LightSigns: Verde (GE)
                    char cmd[20];
                    snprintf(cmd, 20, "GE,%d", track_id);
                    manager_send_to_lightsigns(manager, cmd);

                    printf("✅ Trilho %d libertado\n", track_id);
                    return 1;
                }
            }
        }
    }
    else if (strncmp(command, "GET_SENSORS", 11) == 0) {
        // Já foi chamado no início da função
        return 1;
    }
    else if (strncmp(command, "EXIT", 4) == 0) {
        manager->system_running = 0;
        return 1;
    }

    return 0;
}

// ============================================
// INTEGRAÇÃO COM COMPONENTES (ARDUINO)
// ============================================

void manager_request_sensor_data(StationManager* manager) {
    if (!manager) return;

    char buffer[256] = {0};

    // 1. Tenta obter dados reais do Arduino
    if (manager->serial_fd > 0) {
        // Envia "GTH" e espera resposta
        // O Arduino deve responder algo como: "TEMP&unit:celsius&value:23#HUM&unit:percent&value:60"
        int n = serial_transaction(manager->serial_fd, "GTH", buffer, 256);

        if (n <= 0) {
            printf("❌ Erro: Sem resposta do sensor (Timeout)\n");
            // Em caso de erro, não atualiza ou usa valor de erro
            return;
        }
        // Remove caracteres estranhos do final se houver
        buffer[strcspn(buffer, "\r\n")] = 0;
    }
    else {
        // 2. Modo Simulação (se cabo desligado)
        strcpy(buffer, "TEMP&unit:celsius&value:23#HUM&unit:percent&value:65");
        // printf("[SIMULAÇÃO] Dados gerados: %s\n", buffer);
    }

    // 3. Processa a string (real ou simulada) com Assembly
    manager_update_sensor_data(manager, buffer);
}

void manager_update_sensor_data(StationManager* manager, const char* sensor_str) {
    if (!manager || !sensor_str) return;

    char unit[20];
    int value;

    // Chama assembly usac03_extract_data
    if (extract_data((char*)sensor_str, "TEMP", unit, &value)) {
        manager->sensor_config.temperature = (float)value;
    }

    if (extract_data((char*)sensor_str, "HUM", unit, &value)) {
        manager->sensor_config.humidity = (float)value;
    }

    manager->sensor_config.timestamp = time(NULL);
}

void manager_send_to_lightsigns(StationManager* manager, const char* command) {
    // command vem como "YE,1" ou "RE,2" da lógica interna
    if (!manager || !command) return;

    int track_num;
    char op[10];

    // Parse simples: OpCode,Trilho
    if (sscanf(command, "%[^,],%d", op, &track_num) == 2) {

        // Formatar para o protocolo do Arduino (ex: "YE,01")
        // O Arduino espera 2 digitos para o trilho
        char final_cmd[32];
        snprintf(final_cmd, 32, "%s,%02d", op, track_num);

        // 1. Envio Real
        if (manager->serial_fd > 0) {
            printf("💡 [HARDWARE] A enviar luzes: %s\n", final_cmd);
            serial_send(manager->serial_fd, final_cmd);
        }
        // 2. Simulação
        else {
            printf("💡 [SIMULAÇÃO] Luzes alteradas: %s (Sem hardware)\n", final_cmd);
        }
    }
}

void manager_send_to_board(StationManager* manager, const char* data) {
    if (!manager) return;
    // Função simples de display, mantemos o printf
    printf("\n╔══════════════════════════════╗\n");
    printf("║   🚂 ESTAÇÃO FERROVIÁRIA   ║\n");
    printf("╠══════════════════════════════╣\n");
    printf("║  %-25s  ║\n", data ? data : "Standby");
    printf("║  🌡️ %.1f°C  💧 %.1f%%      ║\n",
           manager->sensor_config.temperature,
           manager->sensor_config.humidity);
    printf("╚══════════════════════════════╝\n");
}

// ============================================
// GESTÃO DE DADOS (Mantidos do original)
// ============================================

int manager_add_user(StationManager* manager, const User* user) {
    if (!manager || !user || manager->user_count >= MAX_USERS) return 0;

    char encrypted_password[MAX_NAME_LEN];
    // Chama assembly para encriptar antes de guardar
    if (!encrypt_data((char*)user->password, user->cipher_key, encrypted_password)) {
        return 0;
    }

    manager->users[manager->user_count] = *user;
    strcpy(manager->users[manager->user_count].password, encrypted_password);
    manager->user_count++;
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
    log->timestamp = time(NULL);
    strncpy(log->action, action, 99);
    log->success = success;

    manager->log_count++;
    return 1;
}

void manager_display_status(const StationManager* manager) {
    if (!manager) return;
    printf("\n--- STATUS GERAL ---\n");
    printf("Sensores: Temp=%.1f Humi=%.1f\n",
           manager->sensor_config.temperature, manager->sensor_config.humidity);

    for(int i=0; i<manager->track_count; i++) {
        printf("Trilho %d: %s\n", manager->tracks[i].id,
               manager->tracks[i].state == TRACK_FREE ? "LIVRE" : "OCUPADO");
    }
}