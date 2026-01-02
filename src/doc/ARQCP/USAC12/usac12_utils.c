#include "usac12.h"
#include <stdio.h>
#include <string.h>
#include <time.h>

// ============================================
// FUNÇÕES UTILITÁRIAS PARA USAC12
// ============================================

// Formata timestamp para string
void format_timestamp_str(time_t timestamp, char* buffer, size_t size) {
    if (timestamp == 0) {
        timestamp = time(NULL);
    }

    struct tm* tm_info = localtime(&timestamp);
    snprintf(buffer, size, "%04d-%02d-%02d %02d:%02d:%02d",
             tm_info->tm_year + 1900,
             tm_info->tm_mon + 1,
             tm_info->tm_mday,
             tm_info->tm_hour,
             tm_info->tm_min,
             tm_info->tm_sec);
}

// Verifica se username é válido
int is_valid_username(const char* username) {
    if (!username || strlen(username) == 0 || strlen(username) > USERNAME_LEN - 1) {
        return 0;
    }

    // Verifica caracteres válidos
    for (int i = 0; username[i] != '\0'; i++) {
        if (!(isalnum(username[i]) || username[i] == '_' || username[i] == '.')) {
            return 0;
        }
    }

    return 1;
}

// Verifica se filename é válido
int is_valid_filename(const char* filename) {
    if (!filename || strlen(filename) == 0 || strlen(filename) > FILENAME_LEN - 1) {
        return 0;
    }

    // Verifica extensão .txt
    const char* dot = strrchr(filename, '.');
    if (!dot || strcmp(dot, ".txt") != 0) {
        return 0;
    }

    // Verifica caracteres válidos no nome
    for (int i = 0; filename[i] != '\0' && filename[i] != '.'; i++) {
        if (!(isalnum(filename[i]) || filename[i] == '_' || filename[i] == '-')) {
            return 0;
        }
    }

    return 1;
}

// Adiciona extensão .txt se necessário
void ensure_txt_extension(char* filename) {
    if (strstr(filename, ".txt") == NULL) {
        strcat(filename, ".txt");
    }
}

// Cria nome de arquivo padrão baseado no usuário
void create_default_filename(const char* username, char* filename) {
    time_t now = time(NULL);
    struct tm* tm_info = localtime(&now);

    snprintf(filename, FILENAME_LEN, "%s_actions_%04d%02d%02d_%02d%02d.txt",
             username,
             tm_info->tm_year + 1900,
             tm_info->tm_mon + 1,
             tm_info->tm_mday,
             tm_info->tm_hour,
             tm_info->tm_min);
}

// Conta linhas em um arquivo (para estatísticas simples)
int count_lines_in_file(const char* filename) {
    FILE* file = fopen(filename, "r");
    if (!file) return 0;

    int count = 0;
    char buffer[256];

    while (fgets(buffer, sizeof(buffer), file)) {
        count++;
    }

    fclose(file);
    return count;
}

// Exibe informações sobre um arquivo de log
void display_file_info(const char* filename) {
    printf("\nFile: %s\n", filename);

    int line_count = count_lines_in_file(filename);
    printf("Lines: %d\n", line_count);

    // Tenta obter tamanho do arquivo
    FILE* file = fopen(filename, "rb");
    if (file) {
        fseek(file, 0, SEEK_END);
        long size = ftell(file);
        fclose(file);

        if (size < 1024) {
            printf("Size: %ld bytes\n", size);
        } else {
            printf("Size: %.2f KB\n", size / 1024.0);
        }
    }
}

// Verifica se arquivo existe e pode ser lido
int file_exists(const char* filename) {
    FILE* file = fopen(filename, "r");
    if (file) {
        fclose(file);
        return 1;
    }
    return 0;
}

// Copia conteúdo de um arquivo para outro (backup simples)
int backup_file(const char* source, const char* backup_name) {
    FILE* src = fopen(source, "r");
    if (!src) return 0;

    char backup_file[FILENAME_LEN * 2];
    if (backup_name) {
        snprintf(backup_file, sizeof(backup_file), "%s", backup_name);
    } else {
        time_t now = time(NULL);
        struct tm* tm_info = localtime(&now);
        snprintf(backup_file, sizeof(backup_file),
                 "backup_%04d%02d%02d_%02d%02d.txt",
                 tm_info->tm_year + 1900,
                 tm_info->tm_mon + 1,
                 tm_info->tm_mday,
                 tm_info->tm_hour,
                 tm_info->tm_min);
    }

    FILE* dst = fopen(backup_file, "w");
    if (!dst) {
        fclose(src);
        return 0;
    }

    char buffer[256];
    while (fgets(buffer, sizeof(buffer), src)) {
        fputs(buffer, dst);
    }

    fclose(src);
    fclose(dst);

    printf("Backup created: %s\n", backup_file);
    return 1;
}

// Lê entrada do usuário com validação
int get_user_input(char* prompt, char* buffer, size_t size) {
    printf("%s", prompt);

    if (fgets(buffer, size, stdin) == NULL) {
        return 0;
    }

    // Remove newline
    buffer[strcspn(buffer, "\n")] = '\0';

    return strlen(buffer) > 0;
}

// Exibe ações de forma formatada
void display_actions_formatted(const ActionLog* log) {
    if (log->count == 0) {
        printf("No actions to display.\n");
        return;
    }

    printf("\n=== User Actions ===\n");
    printf("User: %s\n", log->actions[0].username);
    printf("Total actions: %d\n", log->count);
    printf("=========================\n\n");

    char timestamp[20];

    for (int i = 0; i < log->count; i++) {
        const UserAction* action = &log->actions[i];

        format_timestamp_str(action->timestamp, timestamp, sizeof(timestamp));

        printf("Action #%d\n", i + 1);
        printf("  Time: %s\n", timestamp);
        printf("  User: %s\n", action->username);
        printf("  Action: %s\n", action->action);

        if (action->track_id != -1) {
            printf("  Track: %d\n", action->track_id);
        }

        if (i < log->count - 1) {
            printf("  ---\n");
        }
    }
}

// Gera ação aleatória para testes
void generate_random_action(UserAction* action, const char* username) {
    static const char* action_templates[] = {
        "Assigned train to track %d",
        "Read sensor data from track %d",
        "Changed signal status on track %d",
        "Authorized departure from track %d",
        "Set track %d to maintenance mode",
        "Emergency stop issued for track %d",
        "Temperature check on track %d",
        "Humidity reading from track %d"
    };

    static const char* admin_actions[] = {
        "System configuration updated",
        "User permissions modified",
        "Log files reviewed",
        "Backup completed",
        "Security audit performed",
        "System maintenance scheduled"
    };

    strncpy(action->username, username, USERNAME_LEN);
    action->timestamp = time(NULL) - (rand() % 86400); // Últimas 24h

    if (strcmp(username, "admin") == 0) {
        int idx = rand() % (sizeof(admin_actions) / sizeof(admin_actions[0]));
        snprintf(action->action, ACTION_LEN, "%s", admin_actions[idx]);
        action->track_id = -1;
    } else {
        int idx = rand() % (sizeof(action_templates) / sizeof(action_templates[0]));
        action->track_id = (rand() % 10) + 1;
        snprintf(action->action, ACTION_LEN, action_templates[idx], action->track_id);
    }
}

// Cria log de teste com ações aleatórias
int create_test_log(const char* username, int num_actions, const char* filename) {
    ActionLog log;
    log.count = num_actions;

    srand(time(NULL));

    for (int i = 0; i < num_actions; i++) {
        generate_random_action(&log.actions[i], username);
        // Espaça os timestamps
        log.actions[i].timestamp -= (num_actions - i) * 300; // 5 minutos entre ações
    }

    return save_actions_to_file(&log, filename);
}

// Compara dois arquivos de log (simples)
int compare_log_files(const char* file1, const char* file2) {
    FILE* f1 = fopen(file1, "r");
    FILE* f2 = fopen(file2, "r");

    if (!f1 || !f2) {
        if (f1) fclose(f1);
        if (f2) fclose(f2);
        return 0;
    }

    char line1[256], line2[256];
    int equal = 1;
    int line_num = 1;

    while (1) {
        char* r1 = fgets(line1, sizeof(line1), f1);
        char* r2 = fgets(line2, sizeof(line2), f2);

        if (!r1 && !r2) {
            break; // Ambos terminaram
        }

        if ((r1 && !r2) || (!r1 && r2)) {
            equal = 0; // Tamanhos diferentes
            break;
        }

        if (strcmp(line1, line2) != 0) {
            equal = 0;
            printf("Difference at line %d:\n", line_num);
            printf("  File1: %s", line1);
            printf("  File2: %s", line2);
            break;
        }

        line_num++;
    }

    fclose(f1);
    fclose(f2);

    return equal;
}

// Função simples de validação de data/hora
int validate_datetime(int year, int month, int day, int hour, int minute, int second) {
    if (year < 2020 || year > 2030) return 0;
    if (month < 1 || month > 12) return 0;
    if (day < 1 || day > 31) return 0;
    if (hour < 0 || hour > 23) return 0;
    if (minute < 0 || minute > 59) return 0;
    if (second < 0 || second > 59) return 0;

    // Validação básica de dias por mês
    int days_in_month[] = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    if (month == 2 && ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0)) {
        days_in_month[1] = 29; // Ano bissexto
    }

    return day <= days_in_month[month - 1];
}