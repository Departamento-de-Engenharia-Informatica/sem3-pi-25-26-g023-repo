#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include "file_parser.h"

void trim_string(char* str) {
    char* end;

    // Trim leading space
    while(isspace((unsigned char)*str)) str++;

    if(*str == 0) return;

    // Trim trailing space
    end = str + strlen(str) - 1;
    while(end > str && isspace((unsigned char)*end)) end--;

    // Write new null terminator
    *(end + 1) = 0;
}

int is_comment_line(const char* line) {
    trim_string((char*)line);
    return (line[0] == '#' || line[0] == '\0');
}

int parse_key_value(const char* line, char* key, char* value) {
    char* equals_pos = strchr(line, '=');
    if (!equals_pos) return 0;

    // Extrair chave
    strncpy(key, line, equals_pos - line);
    key[equals_pos - line] = '\0';
    trim_string(key);

    // Extrair valor
    strcpy(value, equals_pos + 1);
    trim_string(value);

    return 1;
}

int string_to_role(const char* role_str) {
    if (strcmp(role_str, "ADMIN") == 0) return ROLE_ADMIN;
    if (strcmp(role_str, "OPERATOR") == 0) return ROLE_OPERATOR;
    if (strcmp(role_str, "TRAFFIC_MANAGER") == 0) return ROLE_TRAFFIC_MANAGER;
    if (strcmp(role_str, "VIEWER") == 0) return ROLE_VIEWER;
    return ROLE_VIEWER; // Default
}

int load_config_file(const char* filename, StationConfig* config) {
    FILE* file = fopen(filename, "r");
    if (!file) {
        printf("Erro: Não foi possível abrir o ficheiro %s\n", filename);
        return 0;
    }

    char line[256];
    char key[100], value[100];

    // Valores por defeito
    config->stationId = 1;
    strcpy(config->stationName, "Estação Desconhecida");
    config->maxTracks = 10;
    config->maxUsers = 5;
    config->tempBufferSize = 10;
    config->tempWindowSize = 5;
    config->humidBufferSize = 10;
    config->humidWindowSize = 5;
    config->maxLogEntries = 1000;
    strcpy(config->logFile, "station_log.txt");
    config->maxLoginAttempts = 3;
    config->sessionTimeout = 300;

    while (fgets(line, sizeof(line), file)) {
        if (is_comment_line(line)) continue;

        if (parse_key_value(line, key, value)) {
            if (strcmp(key, "STATION_ID") == 0) {
                config->stationId = atoi(value);
            } else if (strcmp(key, "STATION_NAME") == 0) {
                strncpy(config->stationName, value, MAX_NAME_LEN - 1);
            } else if (strcmp(key, "MAX_TRACKS") == 0) {
                config->maxTracks = atoi(value);
            } else if (strcmp(key, "MAX_USERS") == 0) {
                config->maxUsers = atoi(value);
            } else if (strcmp(key, "TEMP_BUFFER_SIZE") == 0) {
                config->tempBufferSize = atoi(value);
            } else if (strcmp(key, "TEMP_WINDOW_SIZE") == 0) {
                config->tempWindowSize = atoi(value);
            } else if (strcmp(key, "HUMID_BUFFER_SIZE") == 0) {
                config->humidBufferSize = atoi(value);
            } else if (strcmp(key, "HUMID_WINDOW_SIZE") == 0) {
                config->humidWindowSize = atoi(value);
            } else if (strcmp(key, "MAX_LOG_ENTRIES") == 0) {
                config->maxLogEntries = atoi(value);
            } else if (strcmp(key, "LOG_FILE") == 0) {
                strncpy(config->logFile, value, MAX_NAME_LEN - 1);
            } else if (strcmp(key, "MAX_LOGIN_ATTEMPTS") == 0) {
                config->maxLoginAttempts = atoi(value);
            } else if (strcmp(key, "SESSION_TIMEOUT") == 0) {
                config->sessionTimeout = atoi(value);
            }
        }
    }

    fclose(file);
    return 1;
}

int load_users_file(const char* filename, User users[], int* userCount) {
    FILE* file = fopen(filename, "r");
    if (!file) {
        printf("Erro: Não foi possível abrir o ficheiro %s\n", filename);
        return 0;
    }

    char line[256];
    *userCount = 0;

    while (fgets(line, sizeof(line), file) && *userCount < MAX_USERS) {
        if (is_comment_line(line)) continue;

        trim_string(line);

        // Formato: name;username;password;key;role
        char* tokens[5];
        char* token = strtok(line, ";");
        int token_count = 0;

        while (token && token_count < 5) {
            tokens[token_count++] = token;
            token = strtok(NULL, ";");
        }

        if (token_count == 5) {
            User* user = &users[*userCount];

            strncpy(user->name, tokens[0], MAX_NAME_LEN - 1);
            strncpy(user->username, tokens[1], MAX_USERNAME_LEN - 1);
            strncpy(user->password, tokens[2], MAX_PASSWORD_LEN - 1);
            user->cipherKey = atoi(tokens[3]);
            user->role = string_to_role(tokens[4]);

            if (validate_user(user)) {
                (*userCount)++;
            } else {
                printf("Aviso: Utilizador inválido na linha: %s\n", line);
            }
        }
    }

    fclose(file);
    return 1;
}

int load_tracks_file(const char* filename, Track tracks[], int* trackCount) {
    FILE* file = fopen(filename, "r");
    if (!file) {
        printf("Erro: Não foi possível abrir o ficheiro %s\n", filename);
        return 0;
    }

    char line[256];
    *trackCount = 0;

    while (fgets(line, sizeof(line), file) && *trackCount < MAX_TRACKS) {
        if (is_comment_line(line)) continue;

        trim_string(line);

        // Formato: id;state;train_id
        char* tokens[3];
        char* token = strtok(line, ";");
        int token_count = 0;

        while (token && token_count < 3) {
            tokens[token_count++] = token;
            token = strtok(NULL, ";");
        }

        if (token_count == 3) {
            Track* track = &tracks[*trackCount];

            track->id = atoi(tokens[0]);
            track->state = atoi(tokens[1]);
            track->trainId = atoi(tokens[2]);

            if (validate_track(track)) {
                (*trackCount)++;
            } else {
                printf("Aviso: Via inválida na linha: %s\n", line);
            }
        }
    }

    fclose(file);
    return 1;
}

int validate_config(const StationConfig* config) {
    if (config->maxTracks < 1 || config->maxTracks > MAX_TRACKS) {
        printf("Erro: MAX_TRACKS deve estar entre 1 e %d\n", MAX_TRACKS);
        return 0;
    }

    if (config->maxUsers < 1 || config->maxUsers > MAX_USERS) {
        printf("Erro: MAX_USERS deve estar entre 1 e %d\n", MAX_USERS);
        return 0;
    }

    if (config->tempBufferSize < 1 || config->tempWindowSize > config->tempBufferSize) {
        printf("Erro: Configuração inválida dos buffers de temperatura\n");
        return 0;
    }

    if (config->humidBufferSize < 1 || config->humidWindowSize > config->humidBufferSize) {
        printf("Erro: Configuração inválida dos buffers de humidade\n");
        return 0;
    }

    return 1;
}

int validate_user(const User* user) {
    if (strlen(user->username) == 0 || strlen(user->password) == 0) {
        return 0;
    }

    if (user->cipherKey < 1 || user->cipherKey > 26) {
        return 0;
    }

    return 1;
}

int validate_track(const Track* track) {
    if (track->id < 1 || track->id > MAX_TRACKS) {
        return 0;
    }

    if (track->state < TRACK_FREE || track->state > TRACK_INOPERATIVE) {
        return 0;
    }

    return 1;
}