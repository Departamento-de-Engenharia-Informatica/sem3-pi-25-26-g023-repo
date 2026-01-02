#include "config_loader.h"
#include "../common/colors.h"
#include "../common/utils.h"
#include "../assembly/usac01_encrypt_data.s"
#include "../assembly/usac02_decrypt_data.s"

// ============================================
// IMPLEMENTAÇÃO DO CONFIG LOADER - USAC11
// ============================================

// Declarações externas das funções assembly
extern int encrypt_data(char* in, int key, char* out);
extern int decrypt_data(char* in, int key, char* out);

// Variáveis globais
char config_files_loaded[MAX_CONFIG_FILES][100];
int config_files_count = 0;
time_t last_config_load_time = 0;

// Variáveis do sistema (definidas em station_manager.c)
extern Track tracks[MAX_TRACKS];
extern User users[MAX_USERS];
extern SensorData sensor_config;
extern int track_count;
extern int user_count;

// ============================================
// FUNÇÕES PRINCIPAIS
// ============================================

int load_initial_config(const char* filename) {
    printf("\n%s=== CARREGANDO CONFIGURAÇÃO INICIAL ===%s\n",
           COLOR_BOLD COLOR_BLUE, COLOR_RESET);

    if (filename == NULL || strlen(filename) == 0) {
        filename = DEFAULT_CONFIG_FILE;
    }

    printf("Arquivo: %s\n", filename);

    FILE* file = fopen(filename, "r");
    if (file == NULL) {
        printf("%sERRO: Arquivo não encontrado: %s%s\n",
               COLOR_RED, filename, COLOR_RESET);
        return 0;
    }

    // Registrar arquivo carregado
    if (config_files_count < MAX_CONFIG_FILES) {
        strncpy(config_files_loaded[config_files_count], filename, 99);
        config_files_loaded[config_files_count][99] = '\0';
        config_files_count++;
    }

    char line[MAX_CONFIG_LINE];
    int line_number = 0;
    int entries_loaded = 0;
    int entries_failed = 0;

    ConfigValidation validation;
    memset(&validation, 0, sizeof(validation));

    printf("\n%sProcessando configuração...%s\n", COLOR_CYAN, COLOR_RESET);

    while (fgets(line, sizeof(line), file)) {
        line_number++;
        validation.total_lines++;

        // Remover newline
        line[strcspn(line, "\n")] = '\0';

        // Ignorar linhas vazias e comentários
        if (strlen(line) == 0 || line[0] == '#' || line[0] == ';') {
            continue;
        }

        ConfigEntry entry;
        memset(&entry, 0, sizeof(entry));
        entry.line_number = line_number;

        if (parse_config_line(line, &entry)) {
            if (validate_config_entry(&entry)) {
                if (process_config_entry(&entry)) {
                    entries_loaded++;
                    validation.valid_lines++;
                } else {
                    entries_failed++;
                    validation.error_lines++;
                    snprintf(validation.errors[validation.error_lines-1], 100,
                            "Linha %d: Falha ao processar '%s'",
                            line_number, entry.key);
                }
            } else {
                entries_failed++;
                validation.error_lines++;
                snprintf(validation.errors[validation.error_lines-1], 100,
                        "Linha %d: Entrada inválida '%s'",
                        line_number, entry.key);
            }
        } else {
            entries_failed++;
            validation.error_lines++;
            snprintf(validation.errors[validation.error_lines-1], 100,
                    "Linha %d: Formato inválido", line_number);
        }
    }

    fclose(file);

    // Atualizar timestamp
    last_config_load_time = time(NULL);

    // Exibir resumo
    printf("\n%s=== RESUMO DA CARGA ===%s\n", COLOR_BOLD COLOR_GREEN, COLOR_RESET);
    printf("Linhas processadas: %d\n", validation.total_lines);
    printf("Entradas carregadas: %s%d%s\n",
           entries_loaded > 0 ? COLOR_GREEN : COLOR_RED,
           entries_loaded, COLOR_RESET);
    printf("Entradas com erro: %s%d%s\n",
           entries_failed > 0 ? COLOR_RED : COLOR_GREEN,
           entries_failed, COLOR_RESET);

    if (entries_failed > 0) {
        printf("\n%sErros encontrados:%s\n", COLOR_RED, COLOR_RESET);
        for (int i = 0; i < validation.error_lines && i < 10; i++) {
            printf("  • %s\n", validation.errors[i]);
        }
    }

    // Verificar consistência
    if (!verify_config_consistency()) {
        printf("%sAVISO: Problemas de consistência na configuração%s\n",
               COLOR_YELLOW, COLOR_RESET);
    }

    printf("\n%sConfiguração carregada com sucesso!%s\n",
           COLOR_BOLD COLOR_GREEN, COLOR_RESET);

    return entries_loaded > 0;
}

int load_config_from_directory(const char* dirname) {
    printf("\n%s=== CARREGANDO CONFIGURAÇÃO DO DIRETÓRIO ===%s\n",
           COLOR_BOLD COLOR_BLUE, COLOR_RESET);
    printf("Diretório: %s\n", dirname);

    // Lista de arquivos de configuração esperados
    const char* config_files[] = {
        "station_config.txt",
        "users_config.txt",
        "tracks_config.txt",
        "sensors_config.txt",
        NULL
    };

    int files_loaded = 0;
    int total_entries = 0;

    for (int i = 0; config_files[i] != NULL; i++) {
        char full_path[200];
        snprintf(full_path, sizeof(full_path), "%s/%s", dirname, config_files[i]);

        if (access(full_path, F_OK) == 0) {
            printf("\n%sCarregando: %s%s\n", COLOR_CYAN, config_files[i], COLOR_RESET);
            if (load_initial_config(full_path)) {
                files_loaded++;
            }
        } else {
            printf("%sArquivo não encontrado: %s%s\n",
                   COLOR_YELLOW, config_files[i], COLOR_RESET);
        }
    }

    printf("\n%sTotal de arquivos carregados: %d%s\n",
           files_loaded > 0 ? COLOR_GREEN : COLOR_YELLOW,
           files_loaded, COLOR_RESET);

    return files_loaded;
}

// ============================================
// FUNÇÕES DE PARSING
// ============================================

int parse_config_line(const char* line, ConfigEntry* entry) {
    if (line == NULL || entry == NULL) return 0;

    char line_copy[MAX_CONFIG_LINE];
    strncpy(line_copy, line, sizeof(line_copy) - 1);
    line_copy[sizeof(line_copy) - 1] = '\0';

    // Remover espaços extras
    trim_string(line_copy);

    // Encontrar o separador '='
    char* separator = strchr(line_copy, '=');
    if (separator == NULL) {
        return 0;
    }

    // Dividir em key e value
    *separator = '\0';
    char* key = line_copy;
    char* value = separator + 1;

    // Remover espaços extras do key e value
    trim_string(key);
    trim_string(value);

    if (strlen(key) == 0 || strlen(value) == 0) {
        return 0;
    }

    // Armazenar no entry
    strncpy(entry->key, key, sizeof(entry->key) - 1);
    strncpy(entry->value, value, sizeof(entry->value) - 1);
    entry->type = detect_config_type(key);
    entry->valid = 1;

    return 1;
}

ConfigType detect_config_type(const char* key) {
    if (strstr(key, "USER") != NULL) return CONFIG_USER;
    if (strstr(key, "TRACK") != NULL) return CONFIG_TRACK;
    if (strstr(key, "SENSOR") != NULL) return CONFIG_SENSOR;
    if (strstr(key, "LOG") != NULL) return CONFIG_LOG;
    return CONFIG_SYSTEM;
}

int validate_config_entry(const ConfigEntry* entry) {
    if (entry == NULL || !entry->valid) return 0;

    switch (entry->type) {
        case CONFIG_USER:
            return validate_user_config(entry->value);
        case CONFIG_TRACK:
            return validate_track_config(entry->value);
        case CONFIG_SENSOR:
            return validate_sensor_config(entry->value);
        case CONFIG_SYSTEM:
        case CONFIG_LOG:
            return strlen(entry->value) > 0;
        default:
            return 0;
    }
}

// ============================================
// FUNÇÕES DE PROCESSAMENTO POR TIPO
// ============================================

int process_config_entry(const ConfigEntry* entry) {
    switch (entry->type) {
        case CONFIG_USER:
            return process_user_config(entry->value);
        case CONFIG_TRACK:
            return process_track_config(entry->value);
        case CONFIG_SENSOR:
            return process_sensor_config(entry->value);
        case CONFIG_SYSTEM:
            return process_system_config(entry->value);
        case CONFIG_LOG:
            return process_log_config(entry->value);
        default:
            return 0;
    }
}

int process_user_config(const char* value) {
    // Formato: nome:username:senha:chave:tipo
    char name[50], username[50], password[50];
    int cipher_key;
    char type_str[20];

    if (sscanf(value, "%[^:]:%[^:]:%[^:]:%d:%s",
               name, username, password, &cipher_key, type_str) != 5) {
        printf("%sERRO: Formato de usuário inválido: %s%s\n",
               COLOR_RED, value, COLOR_RESET);
        return 0;
    }

    // Validar campos
    if (!is_valid_username(username)) {
        printf("%sERRO: Nome de usuário inválido: %s%s\n",
               COLOR_RED, username, COLOR_RESET);
        return 0;
    }

    if (!is_valid_password(password)) {
        printf("%sERRO: Senha inválida (apenas maiúsculas): %s%s\n",
               COLOR_RED, password, COLOR_RESET);
        return 0;
    }

    if (cipher_key < 1 || cipher_key > 26) {
        printf("%sERRO: Chave de criptografia inválida (1-26): %d%s\n",
               COLOR_RED, cipher_key, COLOR_RESET);
        return 0;
    }

    // Converter tipo de string para enum
    UserType user_type;
    if (strcmp(type_str, "ADMIN") == 0) user_type = USER_ADMIN;
    else if (strcmp(type_str, "OPERATOR") == 0) user_type = USER_OPERATOR;
    else if (strcmp(type_str, "MANAGER") == 0) user_type = USER_MANAGER;
    else {
        printf("%sERRO: Tipo de usuário desconhecido: %s%s\n",
               COLOR_RED, type_str, COLOR_RESET);
        return 0;
    }

    // Verificar se usuário já existe
    for (int i = 0; i < user_count; i++) {
        if (strcmp(users[i].username, username) == 0) {
            printf("%sAVISO: Usuário já existe: %s%s\n",
                   COLOR_YELLOW, username, COLOR_RESET);
            return 0;
        }
    }

    // Criptografar senha usando USAC01
    char encrypted_password[50];
    if (!encrypt_data(password, cipher_key, encrypted_password)) {
        printf("%sERRO: Falha ao criptografar senha%s\n", COLOR_RED, COLOR_RESET);
        return 0;
    }

    // Adicionar usuário
    if (user_count >= MAX_USERS) {
        printf("%sERRO: Número máximo de usuários atingido%s\n",
               COLOR_RED, COLOR_RESET);
        return 0;
    }

    User* user = &users[user_count];
    strncpy(user->name, name, MAX_NAME_LEN - 1);
    strncpy(user->username, username, MAX_NAME_LEN - 1);
    strncpy(user->password, encrypted_password, MAX_NAME_LEN - 1);
    user->cipher_key = cipher_key;
    user->type = user_type;

    user_count++;

    printf("%sUsuário adicionado: %s (%s)%s\n",
           COLOR_GREEN, username, name, COLOR_RESET);

    return 1;
}

int process_track_config(const char* value) {
    // Formato: id:descricao:estado
    int id;
    char description[100];
    char state_str[20];

    if (sscanf(value, "%d:%[^:]:%s", &id, description, state_str) != 3) {
        printf("%sERRO: Formato de trilho inválido: %s%s\n",
               COLOR_RED, value, COLOR_RESET);
        return 0;
    }

    if (!is_valid_track_id(id)) {
        printf("%sERRO: ID de trilho inválido: %d%s\n",
               COLOR_RED, id, COLOR_RESET);
        return 0;
    }

    // Verificar se trilho já existe
    for (int i = 0; i < track_count; i++) {
        if (tracks[i].id == id) {
            printf("%sAVISO: Trilho já existe: %d%s\n",
                   COLOR_YELLOW, id, COLOR_RESET);
            return 0;
        }
    }

    // Converter estado de string para enum
    TrackState state;
    if (strcmp(state_str, "FREE") == 0) state = TRACK_FREE;
    else if (strcmp(state_str, "ASSIGNED") == 0) state = TRACK_ASSIGNED;
    else if (strcmp(state_str, "BUSY") == 0) state = TRACK_BUSY;
    else if (strcmp(state_str, "INOPERATIVE") == 0) state = TRACK_INOPERATIVE;
    else {
        printf("%sERRO: Estado de trilho desconhecido: %s%s\n",
               COLOR_RED, state_str, COLOR_RESET);
        return 0;
    }

    // Adicionar trilho
    if (track_count >= MAX_TRACKS) {
        printf("%sERRO: Número máximo de trilhos atingido%s\n",
               COLOR_RED, COLOR_RESET);
        return 0;
    }

    Track* track = &tracks[track_count];
    track->id = id;
    strncpy(track->description, description, MAX_NAME_LEN - 1);
    track->state = state;
    track->train_id = -1; // Nenhum trem atribuído inicialmente
    track->last_update = time(NULL);

    track_count++;

    printf("%sTrilho adicionado: %d - %s (%s)%s\n",
           COLOR_GREEN, id, description, state_str, COLOR_RESET);

    return 1;
}

int process_sensor_config(const char* value) {
    // Formato: tipo:buffer_len:window_len:min:max
    char type[20];
    int buffer_len, window_len;
    float min_val, max_val;

    if (sscanf(value, "%[^:]:%d:%d:%f:%f",
               type, &buffer_len, &window_len, &min_val, &max_val) != 5) {
        printf("%sERRO: Formato de sensor inválido: %s%s\n",
               COLOR_RED, value, COLOR_RESET);
        return 0;
    }

    // Validar valores
    if (buffer_len <= 0 || window_len <= 0) {
        printf("%sERRO: Buffer/window devem ser > 0%s\n", COLOR_RED, COLOR_RESET);
        return 0;
    }

    if (window_len > buffer_len) {
        printf("%sERRO: Janela não pode ser maior que buffer%s\n",
               COLOR_RED, COLOR_RESET);
        return 0;
    }

    if (!is_valid_sensor_value(min_val, -50.0, 50.0) ||
        !is_valid_sensor_value(max_val, -50.0, 50.0) ||
        min_val >= max_val) {
        printf("%sERRO: Valores min/max inválidos%s\n", COLOR_RED, COLOR_RESET);
        return 0;
    }

    // Configurar sensor
    if (strcmp(type, "TEMPERATURE") == 0) {
        sensor_config.temp_buffer_len = buffer_len;
        sensor_config.temp_window_len = window_len;
        printf("%sSensor temperatura configurado: buffer=%d, window=%d%s\n",
               COLOR_GREEN, buffer_len, window_len, COLOR_RESET);
    }
    else if (strcmp(type, "HUMIDITY") == 0) {
        sensor_config.hum_buffer_len = buffer_len;
        sensor_config.hum_window_len = window_len;
        printf("%sSensor humidade configurado: buffer=%d, window=%d%s\n",
               COLOR_GREEN, buffer_len, window_len, COLOR_RESET);
    }
    else {
        printf("%sERRO: Tipo de sensor desconhecido: %s%s\n",
               COLOR_RED, type, COLOR_RESET);
        return 0;
    }

    return 1;
}

int process_system_config(const char* value) {
    // Configurações gerais do sistema
    printf("%sConfiguração do sistema: %s%s\n",
           COLOR_GREEN, value, COLOR_RESET);
    return 1;
}

int process_log_config(const char* value) {
    // Configurações de logging
    printf("%sConfiguração de log: %s%s\n",
           COLOR_GREEN, value, COLOR_RESET);
    return 1;
}

// ============================================
// FUNÇÕES DE VALIDAÇÃO
// ============================================

int validate_user_config(const char* value) {
    // Formato mínimo: nome:username:senha:chave:tipo
    int count = 0;
    for (int i = 0; value[i] != '\0'; i++) {
        if (value[i] == ':') count++;
    }
    return count >= 4; // Pelo menos 4 separadores
}

int validate_track_config(const char* value) {
    // Formato mínimo: id:descricao:estado
    int count = 0;
    for (int i = 0; value[i] != '\0'; i++) {
        if (value[i] == ':') count++;
    }
    return count >= 2; // Pelo menos 2 separadores
}

int validate_sensor_config(const char* value) {
    // Formato mínimo: tipo:buffer:window:min:max
    int count = 0;
    for (int i = 0; value[i] != '\0'; i++) {
        if (value[i] == ':') count++;
    }
    return count >= 4; // Pelo menos 4 separadores
}

int is_valid_username(const char* username) {
    if (username == NULL || strlen(username) < 3 || strlen(username) > 20) {
        return 0;
    }

    // Apenas letras, números e underscore
    for (int i = 0; username[i] != '\0'; i++) {
        if (!isalnum(username[i]) && username[i] != '_') {
            return 0;
        }
    }

    return 1;
}

int is_valid_password(const char* password) {
    if (password == NULL || strlen(password) < 4) {
        return 0;
    }

    // Apenas letras maiúsculas (conforme especificação)
    for (int i = 0; password[i] != '\0'; i++) {
        if (!isupper(password[i])) {
            return 0;
        }
    }

    return 1;
}

int is_valid_track_id(int track_id) {
    return track_id >= 1 && track_id <= MAX_TRACKS;
}

int is_valid_sensor_value(float value, float min, float max) {
    return value >= min && value <= max;
}

// ============================================
// FUNÇÕES UTILITÁRIAS
// ============================================

void trim_string(char* str) {
    if (str == NULL) return;

    // Remover espaços do início
    char* start = str;
    while (isspace(*start)) start++;

    // Remover espaços do fim
    char* end = str + strlen(str) - 1;
    while (end > start && isspace(*end)) end--;

    // Mover string
    if (start != str) {
        memmove(str, start, end - start + 1);
    }

    // Terminar string
    str[end - start + 1] = '\0';
}

int verify_config_consistency(void) {
    int consistent = 1;

    printf("\n%s=== VERIFICAÇÃO DE CONSISTÊNCIA ===%s\n",
           COLOR_BOLD COLOR_CYAN, COLOR_RESET);

    // Verificar se há pelo menos um administrador
    int admin_count = 0;
    for (int i = 0; i < user_count; i++) {
        if (users[i].type == USER_ADMIN) {
            admin_count++;
        }
    }

    if (admin_count == 0) {
        printf("%sAVISO: Nenhum administrador configurado%s\n",
               COLOR_YELLOW, COLOR_RESET);
        consistent = 0;
    } else {
        printf("%s✓ Administradores: %d%s\n",
               COLOR_GREEN, admin_count, COLOR_RESET);
    }

    // Verificar se há trilhos configurados
    if (track_count == 0) {
        printf("%sAVISO: Nenhum trilho configurado%s\n",
               COLOR_YELLOW, COLOR_RESET);
        consistent = 0;
    } else {
        printf("%s✓ Trilhos: %d%s\n",
               COLOR_GREEN, track_count, COLOR_RESET);
    }

    // Verificar configuração de sensores
    if (sensor_config.temp_buffer_len == 0 ||
        sensor_config.hum_buffer_len == 0) {
        printf("%sAVISO: Sensores não configurados%s\n",
               COLOR_YELLOW, COLOR_RESET);
        consistent = 0;
    } else {
        printf("%s✓ Sensores configurados%s\n", COLOR_GREEN, COLOR_RESET);
    }

    // Verificar IDs duplicados
    int duplicate_tracks = 0;
    for (int i = 0; i < track_count; i++) {
        for (int j = i + 1; j < track_count; j++) {
            if (tracks[i].id == tracks[j].id) {
                duplicate_tracks++;
            }
        }
    }

    if (duplicate_tracks > 0) {
        printf("%sERRO: IDs de trilho duplicados: %d%s\n",
               COLOR_RED, duplicate_tracks, COLOR_RESET);
        consistent = 0;
    } else {
        printf("%s✓ IDs de trilho únicos%s\n", COLOR_GREEN, COLOR_RESET);
    }

    // Verificar nomes de usuário duplicados
    int duplicate_users = 0;
    for (int i = 0; i < user_count; i++) {
        for (int j = i + 1; j < user_count; j++) {
            if (strcmp(users[i].username, users[j].username) == 0) {
                duplicate_users++;
            }
        }
    }

    if (duplicate_users > 0) {
        printf("%sERRO: Nomes de usuário duplicados: %d%s\n",
               COLOR_RED, duplicate_users, COLOR_RESET);
        consistent = 0;
    } else {
        printf("%s✓ Nomes de usuário únicos%s\n", COLOR_GREEN, COLOR_RESET);
    }

    return consistent;
}

ConfigValidation validate_config_file(const char* filename) {
    ConfigValidation validation;
    memset(&validation, 0, sizeof(validation));

    FILE* file = fopen(filename, "r");
    if (file == NULL) {
        snprintf(validation.errors[0], 100, "Arquivo não encontrado");
        validation.error_lines = 1;
        return validation;
    }

    char line[MAX_CONFIG_LINE];
    int line_number = 0;

    while (fgets(line, sizeof(line), file)) {
        line_number++;
        validation.total_lines++;

        line[strcspn(line, "\n")] = '\0';

        // Ignorar linhas vazias e comentários
        if (strlen(line) == 0 || line[0] == '#' || line[0] == ';') {
            continue;
        }

        ConfigEntry entry;
        memset(&entry, 0, sizeof(entry));

        if (parse_config_line(line, &entry)) {
            if (validate_config_entry(&entry)) {
                validation.valid_lines++;
            } else {
                validation.error_lines++;
                if (validation.error_lines <= 10) {
                    snprintf(validation.errors[validation.error_lines-1], 100,
                            "Linha %d: '%s' inválido", line_number, entry.key);
                }
            }
        } else {
            validation.error_lines++;
            if (validation.error_lines <= 10) {
                snprintf(validation.errors[validation.error_lines-1], 100,
                        "Linha %d: Formato inválido", line_number);
            }
        }
    }

    fclose(file);
    return validation;
}

void print_config_validation(const ConfigValidation* validation) {
    printf("\n%s=== VALIDAÇÃO DO ARQUIVO ===%s\n",
           COLOR_BOLD COLOR_BLUE, COLOR_RESET);
    printf("Linhas totais: %d\n", validation->total_lines);
    printf("Linhas válidas: %s%d%s\n",
           validation->valid_lines > 0 ? COLOR_GREEN : COLOR_RED,
           validation->valid_lines, COLOR_RESET);
    printf("Linhas com erro: %s%d%s\n",
           validation->error_lines > 0 ? COLOR_RED : COLOR_GREEN,
           validation->error_lines, COLOR_RESET);

    if (validation->error_lines > 0) {
        printf("\n%sErros encontrados:%s\n", COLOR_RED, COLOR_RESET);
        for (int i = 0; i < validation->error_lines && i < 10; i++) {
            printf("  • %s\n", validation->errors[i]);
        }

        if (validation->error_lines > 10) {
            printf("  ... e mais %d erros\n", validation->error_lines - 10);
        }
    }
}

// ============================================
// FUNÇÕES DE BACKUP E EXPORTAÇÃO
// ============================================

int save_current_config(const char* filename) {
    printf("\n%s=== SALVANDO CONFIGURAÇÃO ATUAL ===%s\n",
           COLOR_BOLD COLOR_BLUE, COLOR_RESET);

    FILE* file = fopen(filename, "w");
    if (file == NULL) {
        printf("%sERRO: Não foi possível criar arquivo%s\n",
               COLOR_RED, COLOR_RESET);
        return 0;
    }

    // Cabeçalho
    fprintf(file, "# ===========================================\n");
    fprintf(file, "# CONFIGURAÇÃO DO SISTEMA FERROVIÁRIO\n");
    fprintf(file, "# Gerado em: %s", ctime(&(time_t){time(NULL)}));
    fprintf(file, "# Versão: %s\n", CONFIG_VERSION);
    fprintf(file, "# ===========================================\n\n");

    // Seção de usuários
    fprintf(file, "# USUÁRIOS DO SISTEMA\n");
    fprintf(file, "# Formato: USER=nome:username:senha:chave:tipo\n\n");
    for (int i = 0; i < user_count; i++) {
        // Para senha, precisamos descriptografar para salvar em texto plano
        char decrypted_password[50];
        if (decrypt_data(users[i].password, users[i].cipher_key, decrypted_password)) {
            const char* type_str = "OPERATOR";
            if (users[i].type == USER_ADMIN) type_str = "ADMIN";
            else if (users[i].type == USER_MANAGER) type_str = "MANAGER";

            fprintf(file, "USER=%s:%s:%s:%d:%s\n",
                   users[i].name, users[i].username,
                   decrypted_password, users[i].cipher_key, type_str);
        }
    }

    fprintf(file, "\n# TRILHOS DA ESTAÇÃO\n");
    fprintf(file, "# Formato: TRACK=id:descricao:estado\n\n");
    for (int i = 0; i < track_count; i++) {
        const char* state_str = "FREE";
        if (tracks[i].state == TRACK_ASSIGNED) state_str = "ASSIGNED";
        else if (tracks[i].state == TRACK_BUSY) state_str = "BUSY";
        else if (tracks[i].state == TRACK_INOPERATIVE) state_str = "INOPERATIVE";

        fprintf(file, "TRACK=%d:%s:%s\n",
               tracks[i].id, tracks[i].description, state_str);
    }

    fprintf(file, "\n# CONFIGURAÇÃO DOS SENSORES\n");
    fprintf(file, "# Formato: SENSOR=tipo:buffer_len:window_len:min:max\n\n");
    fprintf(file, "SENSOR=TEMPERATURE:%d:%d:%.1f:%.1f\n",
           sensor_config.temp_buffer_len, sensor_config.temp_window_len,
           -20.0, 60.0);
    fprintf(file, "SENSOR=HUMIDITY:%d:%d:%.1f:%.1f\n",
           sensor_config.hum_buffer_len, sensor_config.hum_window_len,
           0.0, 100.0);

    // Configurações do sistema
    fprintf(file, "\n# CONFIGURAÇÕES DO SISTEMA\n\n");
    fprintf(file, "SYSTEM_VERSION=%s\n", CONFIG_VERSION);
    fprintf(file, "MAX_TRACKS=%d\n", MAX_TRACKS);
    fprintf(file, "MAX_USERS=%d\n", MAX_USERS);
    fprintf(file, "LAST_UPDATE=%ld\n", (long)time(NULL));

    fclose(file);

    printf("%sConfiguração salva em: %s%s\n",
           COLOR_GREEN, filename, COLOR_RESET);
    printf("%sTotal de registros: %d usuários, %d trilhos%s\n",
           COLOR_CYAN, user_count, track_count, COLOR_RESET);

    return 1;
}

int create_config_backup(const char* backup_name) {
    char filename[100];
    if (backup_name == NULL || strlen(backup_name) == 0) {
        // Nome automático com timestamp
        time_t now = time(NULL);
        struct tm* tm_info = localtime(&now);
        strftime(filename, sizeof(filename),
                "config_backup_%Y%m%d_%H%M%S.txt", tm_info);
    } else {
        snprintf(filename, sizeof(filename), "backups/%s.txt", backup_name);
    }

    // Criar diretório de backups se não existir
    system("mkdir -p backups");

    return save_current_config(filename);
}

int export_config_to_csv(const char* filename) {
    FILE* file = fopen(filename, "w");
    if (file == NULL) return 0;

    // Cabeçalho CSV
    fprintf(file, "TYPE,ID,NAME,USERNAME,STATE,DESCRIPTION\n");

    // Usuários
    for (int i = 0; i < user_count; i++) {
        const char* type_str = "USER";
        if (users[i].type == USER_ADMIN) type_str = "ADMIN";
        else if (users[i].type == USER_MANAGER) type_str = "MANAGER";

        fprintf(file, "%s,%d,%s,%s,,%s\n",
               type_str, i+1, users[i].name, users[i].username, users[i].name);
    }

    // Trilhos
    for (int i = 0; i < track_count; i++) {
        const char* state_str = "FREE";
        if (tracks[i].state == TRACK_ASSIGNED) state_str = "ASSIGNED";
        else if (tracks[i].state == TRACK_BUSY) state_str = "BUSY";
        else if (tracks[i].state == TRACK_INOPERATIVE) state_str = "INOPERATIVE";

        fprintf(file, "TRACK,%d,,,%s,%s\n",
               tracks[i].id, state_str, tracks[i].description);
    }

    fclose(file);
    return 1;
}

// ============================================
// FUNÇÕES DE DEBUG E INFORMAÇÃO
// ============================================

void print_current_config(void) {
    printf("\n%s=== CONFIGURAÇÃO ATUAL DO SISTEMA ===%s\n",
           COLOR_BOLD COLOR_BLUE, COLOR_RESET);

    printf("\n%sUSUÁRIOS (%d):%s\n", COLOR_BOLD COLOR_CYAN, user_count, COLOR_RESET);
    for (int i = 0; i < user_count; i++) {
        const char* type_str = "OPERATOR";
        if (users[i].type == USER_ADMIN) type_str = "ADMIN";
        else if (users[i].type == USER_MANAGER) type_str = "MANAGER";

        printf("  %2d. %-20s (%s) - Chave: %d\n",
               i+1, users[i].username, type_str, users[i].cipher_key);
    }

    printf("\n%sTRILHOS (%d):%s\n", COLOR_BOLD COLOR_CYAN, track_count, COLOR_RESET);
    for (int i = 0; i < track_count; i++) {
        const char* state_str = "LIVRE";
        const char* color = COLOR_GREEN;

        if (tracks[i].state == TRACK_ASSIGNED) {
            state_str = "ATRIBUIDO";
            color = COLOR_YELLOW;
        } else if (tracks[i].state == TRACK_BUSY) {
            state_str = "OCUPADO";
            color = COLOR_RED;
        } else if (tracks[i].state == TRACK_INOPERATIVE) {
            state_str = "INOPERATIVO";
            color = COLOR_WHITE;
        }

        printf("  %sTrilho %02d: %-12s - %s%s\n",
               color, tracks[i].id, state_str, tracks[i].description, COLOR_RESET);
    }

    printf("\n%sSENSORES:%s\n", COLOR_BOLD COLOR_CYAN, COLOR_RESET);
    printf("  Temperatura: buffer=%d, janela=%d\n",
           sensor_config.temp_buffer_len, sensor_config.temp_window_len);
    printf("  Humidade: buffer=%d, janela=%d\n",
           sensor_config.hum_buffer_len, sensor_config.hum_window_len);

    printf("\n%sARQUIVOS CARREGADOS (%d):%s\n",
           COLOR_BOLD COLOR_CYAN, config_files_count, COLOR_RESET);
    for (int i = 0; i < config_files_count; i++) {
        printf("  %d. %s\n", i+1, config_files_loaded[i]);
    }

    if (last_config_load_time > 0) {
        char time_str[30];
        format_timestamp(last_config_load_time, time_str, sizeof(time_str));
        printf("\n%sÚltima carga: %s%s\n",
               COLOR_GREEN, time_str, COLOR_RESET);
    }
}

void print_config_statistics(void) {
    printf("\n%s=== ESTATÍSTICAS DA CONFIGURAÇÃO ===%s\n",
           COLOR_BOLD COLOR_BLUE, COLOR_RESET);

    // Contar tipos de usuário
    int admin_count = 0, operator_count = 0, manager_count = 0;
    for (int i = 0; i < user_count; i++) {
        switch (users[i].type) {
            case USER_ADMIN: admin_count++; break;
            case USER_OPERATOR: operator_count++; break;
            case USER_MANAGER: manager_count++; break;
        }
    }

    printf("\n%sUsuários: %d total%s\n", COLOR_CYAN, user_count, COLOR_RESET);
    printf("  • Administradores: %d\n", admin_count);
    printf("  • Operadores: %d\n", operator_count);
    printf("  • Gerentes: %d\n", manager_count);

    // Contar estados dos trilhos
    int free_tracks = 0, assigned_tracks = 0, busy_tracks = 0, inoperative_tracks = 0;
    for (int i = 0; i < track_count; i++) {
        switch (tracks[i].state) {
            case TRACK_FREE: free_tracks++; break;
            case TRACK_ASSIGNED: assigned_tracks++; break;
            case TRACK_BUSY: busy_tracks++; break;
            case TRACK_INOPERATIVE: inoperative_tracks++; break;
        }
    }

    printf("\n%sTrilhos: %d total%s\n", COLOR_CYAN, track_count, COLOR_RESET);
    printf("  • Livres: %d\n", free_tracks);
    printf("  • Atribuídos: %d\n", assigned_tracks);
    printf("  • Ocupados: %d\n", busy_tracks);
    printf("  • Inoperativos: %d\n", inoperative_tracks);

    // Capacidade do sistema
    float user_capacity = (float)user_count / MAX_USERS * 100;
    float track_capacity = (float)track_count / MAX_TRACKS * 100;

    printf("\n%sCapacidade do sistema:%s\n", COLOR_CYAN, COLOR_RESET);
    printf("  • Usuários: %d/%d (%.1f%%)\n",
           user_count, MAX_USERS, user_capacity);
    printf("  • Trilhos: %d/%d (%.1f%%)\n",
           track_count, MAX_TRACKS, track_capacity);

    // Configuração de sensores
    printf("\n%sConfiguração de sensores:%s\n", COLOR_CYAN, COLOR_RESET);
    printf("  • Buffer temperatura: %d\n", sensor_config.temp_buffer_len);
    printf("  • Janela temperatura: %d\n", sensor_config.temp_window_len);
    printf("  • Buffer humidade: %d\n", sensor_config.hum_buffer_len);
    printf("  • Janela humidade: %d\n", sensor_config.hum_window_len);
}

const char* config_type_to_string(ConfigType type) {
    switch (type) {
        case CONFIG_USER: return "USER";
        case CONFIG_TRACK: return "TRACK";
        case CONFIG_SENSOR: return "SENSOR";
        case CONFIG_SYSTEM: return "SYSTEM";
        case CONFIG_LOG: return "LOG";
        default: return "UNKNOWN";
    }
}