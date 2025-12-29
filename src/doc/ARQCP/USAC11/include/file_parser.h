#ifndef FILE_PARSER_H
#define FILE_PARSER_H

#include "data_structures.h"

int load_config_file(const char* filename, StationConfig* config);
int load_users_file(const char* filename, User users[], int* userCount);
int load_tracks_file(const char* filename, Track tracks[], int* trackCount);
int validate_config(const StationConfig* config);
int validate_user(const User* user);
int validate_track(const Track* track);

// Funções auxiliares
void trim_string(char* str);
int is_comment_line(const char* line);
int parse_key_value(const char* line, char* key, char* value);
int string_to_role(const char* role_str);

#endif