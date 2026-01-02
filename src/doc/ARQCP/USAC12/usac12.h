#ifndef USAC12_H
#define USAC12_H

#include <stdio.h>
#include <time.h>
#include <ctype.h>  // Para isalnum

#define MAX_USERS 10
#define MAX_ACTIONS 100
#define FILENAME_LEN 100
#define USERNAME_LEN 50
#define ACTION_LEN 100

// Estruturas
typedef struct {
    time_t timestamp;
    char username[USERNAME_LEN];
    char action[ACTION_LEN];
    int track_id;
} UserAction;

typedef struct {
    UserAction actions[MAX_ACTIONS];
    int count;
} ActionLog;

// Protótipos principais (de usac12_log_creator.c)
int create_user_log_file(const char* username, const char* filename);
int read_user_actions(const char* username, ActionLog* log);
int save_actions_to_file(const ActionLog* log, const char* filename);

// Protótipos das funções utilitárias (de usac12_utils.c)
void format_timestamp_str(time_t timestamp, char* buffer, size_t size);
int is_valid_username(const char* username);
int is_valid_filename(const char* filename);
void ensure_txt_extension(char* filename);
void create_default_filename(const char* username, char* filename);
int count_lines_in_file(const char* filename);
void display_file_info(const char* filename);
int file_exists(const char* filename);
int backup_file(const char* source, const char* backup_name);
int get_user_input(char* prompt, char* buffer, size_t size);
void display_actions_formatted(const ActionLog* log);
void generate_random_action(UserAction* action, const char* username);
int create_test_log(const char* username, int num_actions, const char* filename);
int compare_log_files(const char* file1, const char* file2);
int validate_datetime(int year, int month, int day, int hour, int minute, int second);

#endif