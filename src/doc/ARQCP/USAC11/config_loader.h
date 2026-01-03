#ifndef CONFIG_LOADER_H
#define CONFIG_LOADER_H

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <time.h>
#include "../common/data_structures.h"
#include "../assembly/assembly_utils.h"

// ============================================
// CONFIG LOADER - USAC11
// Setup inicial do sistema via arquivos de texto
// ============================================

// Definições
#define MAX_CONFIG_LINE 256
#define MAX_CONFIG_FILES 10
#define CONFIG_VERSION "1.0"
#define DEFAULT_CONFIG_FILE "config_files/station_config.txt"

// Tipos de configuração
typedef enum {
    CONFIG_USER,
    CONFIG_TRACK,
    CONFIG_SENSOR,
    CONFIG_SYSTEM,
    CONFIG_LOG
} ConfigType;

// Estrutura para parsing de configuração
typedef struct {
    char key[50];
    char value[200];
    ConfigType type;
    int line_number;
    int valid;
} ConfigEntry;

// Estrutura para validação de configuração
typedef struct {
    int total_lines;
    int valid_lines;
    int error_lines;
    char errors[10][100];
} ConfigValidation;

// Protótipos de funções principais
int load_initial_config(const char* filename);
int load_config_from_directory(const char* dirname);
int save_current_config(const char* filename);

// Funções de parsing
int parse_config_line(const char* line, ConfigEntry* entry);
ConfigType detect_config_type(const char* key);
int validate_config_entry(const ConfigEntry* entry);

// Funções específicas por tipo
int process_user_config(const char* value);
int process_track_config(const char* value);
int process_sensor_config(const char* value);
int process_system_config(const char* value);

// Funções utilitárias
void trim_string(char* str);
int is_valid_username(const char* username);
int is_valid_password(const char* password);
int is_valid_track_id(int track_id);
int is_valid_sensor_value(float value, float min, float max);

// Funções de validação
ConfigValidation validate_config_file(const char* filename);
void print_config_validation(const ConfigValidation* validation);
int verify_config_consistency(void);

// Funções de backup/restore
int create_config_backup(const char* backup_name);
int restore_config_from_backup(const char* backup_name);
int list_config_backups(void);

// Funções de exportação
int export_config_to_csv(const char* filename);
int export_users_to_file(const char* filename);
int export_tracks_to_file(const char* filename);

// Funções de debug
void print_current_config(void);
void print_config_statistics(void);
const char* config_type_to_string(ConfigType type);

// Variáveis globais para configuração
extern char config_files_loaded[MAX_CONFIG_FILES][100];
extern int config_files_count;
extern time_t last_config_load_time;

#endif // CONFIG_LOADER_H