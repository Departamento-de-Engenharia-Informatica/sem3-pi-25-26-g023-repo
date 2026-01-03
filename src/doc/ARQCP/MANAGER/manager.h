#ifndef MANAGER_H
#define MANAGER_H

#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <string.h>

// ============================================
// MANAGER - Componente Principal do Sistema
// ============================================

// Definições
#define MAX_USERS 50
#define MAX_TRACKS 99
#define MAX_LOGS 1000
#define MAX_NAME_LEN 50
#define MAX_CMD_LEN 100
#define SENSOR_CMD "GTH"

// Tipos de usuário
typedef enum {
    USER_ADMIN = 0,
    USER_OPERATOR,
    USER_MANAGER
} UserType;

// Estados do trilho
typedef enum {
    TRACK_FREE = 0,
    TRACK_ASSIGNED,
    TRACK_BUSY,
    TRACK_INOPERATIVE
} TrackState;

// Estruturas de dados
typedef struct {
    char name[MAX_NAME_LEN];
    char username[MAX_NAME_LEN];
    char password[MAX_NAME_LEN];  // Encriptada com Caesar Cipher
    int cipher_key;
    UserType type;
} User;

typedef struct {
    int id;
    TrackState state;
    int train_id;  // -1 se não tem trem
    char description[MAX_NAME_LEN];
    time_t last_update;
} Track;

typedef struct {
    int id;
    char status[20];  // "in_transit", "stopped", "departing"
    int assigned_track;
} Train;

typedef struct {
    float temperature;
    float humidity;
    time_t timestamp;
    int temp_buffer_len;
    int temp_window_len;
    int hum_buffer_len;
    int hum_window_len;
} SensorData;

typedef struct {
    int id;
    int user_id;
    char action[100];
    time_t timestamp;
    int success;
} LogEntry;

// Estrutura PRINCIPAL do Manager
typedef struct {
    User* users;
    Track* tracks;
    Train* trains;
    SensorData sensor_config;
    LogEntry* logs;

    int user_count;
    int track_count;
    int train_count;
    int log_count;

    // Buffers para processamento
    char* sensor_buffer;
    char* command_buffer;

    // Usuário atual logado
    User* current_user;

    // Estado do sistema
    int system_running;
    time_t start_time;
} StationManager;

// ============================================
// FUNÇÕES PRINCIPAIS
// ============================================

// Inicialização e limpeza
StationManager* manager_create(void);
void manager_destroy(StationManager* manager);
int manager_init_from_file(StationManager* manager, const char* filename);

// Sistema de login
User* manager_login(StationManager* manager, const char* username, const char* password);
void manager_logout(StationManager* manager);

// Processamento de comandos
int manager_process_command(StationManager* manager, const char* command);
int manager_handle_ui_instruction(StationManager* manager, const char* instruction);

// Integração com componentes
void manager_request_sensor_data(StationManager* manager);
void manager_send_to_board(StationManager* manager, const char* data);
void manager_send_to_lightsigns(StationManager* manager, const char* command);
void manager_update_sensor_data(StationManager* manager, const char* sensor_str);

// Gestão de dados
int manager_add_user(StationManager* manager, const User* user);
int manager_add_track(StationManager* manager, const Track* track);
int manager_add_log(StationManager* manager, const char* action, int success);

// Funções utilitárias
void manager_display_status(const StationManager* manager);
int manager_verify_integrity(const StationManager* manager);
int manager_save_state(const StationManager* manager, const char* filename);
int manager_load_state(StationManager* manager, const char* filename);

// Função assembly OBRIGATÓRIA (com struct como parâmetro)
extern int manager_process_sensor_data_asm(StationManager* manager, const char* sensor_str);

#endif // MANAGER_H