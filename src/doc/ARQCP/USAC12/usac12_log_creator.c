#include "usac12.h"
#include <string.h>

// Lista de ações de exemplo para diferentes usuários
const char* admin_actions[] = {
    "Logged into system",
    "Loaded station configuration",
    "Created new user 'operator'",
    "Backed up system configuration",
    "Exported logs to CSV",
    "System maintenance completed",
    NULL
};

const char* operator_actions[] = {
    "Started shift",
    "Assigned train T123 to track 1",
    "Read temperature sensor: 22.5°C",
    "Changed light signal to YELLOW",
    "Authorized train departure",
    "Set track 1 to FREE status",
    "Emergency stop on track 3",
    "Ended shift",
    NULL
};

// Cria arquivo de log para um usuário específico
int create_user_log_file(const char* username, const char* filename) {
    FILE* file = fopen(filename, "w");
    if (!file) return 0;

    fprintf(file, "# Log file for user: %s\n", username);
    fprintf(file, "# Generated on: %s\n", __DATE__);
    fprintf(file, "# =====================================\n\n");

    const char** actions_list = NULL;

    // Escolhe ações baseadas no tipo de usuário
    if (strcmp(username, "admin") == 0) {
        actions_list = admin_actions;
    } else if (strcmp(username, "operator") == 0) {
        actions_list = operator_actions;
    } else {
        actions_list = admin_actions; // default
    }

    // Gera entradas de log com timestamps fictícios
    time_t base_time = time(NULL) - 3600; // 1 hora atrás

    for (int i = 0; actions_list[i] != NULL && i < 10; i++) {
        time_t action_time = base_time + (i * 300); // A cada 5 minutos

        struct tm* tm_info = localtime(&action_time);
        fprintf(file, "[%04d-%02d-%02d %02d:%02d:%02d] %s\n",
                tm_info->tm_year + 1900,
                tm_info->tm_mon + 1,
                tm_info->tm_mday,
                tm_info->tm_hour,
                tm_info->tm_min,
                tm_info->tm_sec,
                actions_list[i]);
    }

    fclose(file);
    return 1;
}

// Lê ações de um arquivo existente (simulação)
int read_user_actions(const char* username, ActionLog* log) {
    log->count = 0;

    // Simula leitura de 5 ações
    for (int i = 0; i < 5; i++) {
        UserAction action;
        strncpy(action.username, username, USERNAME_LEN);

        // Ações diferentes por tipo de usuário
        if (strcmp(username, "admin") == 0) {
            snprintf(action.action, ACTION_LEN,
                    "System action %d completed", i+1);
            action.track_id = -1;
        } else {
            snprintf(action.action, ACTION_LEN,
                    "Track %d operation %d", (i % 3) + 1, i+1);
            action.track_id = (i % 3) + 1;
        }

        action.timestamp = time(NULL) - (i * 600); // 10 minutos entre ações

        log->actions[log->count] = action;
        log->count++;
    }

    return 1;
}

// Salva ações em arquivo
int save_actions_to_file(const ActionLog* log, const char* filename) {
    FILE* file = fopen(filename, "w");
    if (!file) return 0;

    fprintf(file, "# User Actions Log\n");
    fprintf(file, "# Total actions: %d\n", log->count);
    fprintf(file, "# ===============================\n\n");

    for (int i = 0; i < log->count; i++) {
        const UserAction* action = &log->actions[i];
        struct tm* tm_info = localtime(&action->timestamp);

        fprintf(file, "TIMESTAMP: %04d-%02d-%02d %02d:%02d:%02d\n",
                tm_info->tm_year + 1900,
                tm_info->tm_mon + 1,
                tm_info->tm_mday,
                tm_info->tm_hour,
                tm_info->tm_min,
                tm_info->tm_sec);

        fprintf(file, "USER: %s\n", action->username);
        fprintf(file, "ACTION: %s\n", action->action);

        if (action->track_id != -1) {
            fprintf(file, "TRACK: %d\n", action->track_id);
        }

        fprintf(file, "---\n");
    }

    fclose(file);
    return 1;
}