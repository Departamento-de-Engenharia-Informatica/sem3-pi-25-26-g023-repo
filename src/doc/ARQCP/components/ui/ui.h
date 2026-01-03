#ifndef UI_H
#define UI_H

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

#define MAX_INPUT 100
#define MAX_OPTIONS 20

// Estrutura para menu
typedef struct {
    char title[50];
    char options[MAX_OPTIONS][50];
    int option_count;
} Menu;

// Funções principais
void ui_init(void);
void ui_display_menu(const Menu* menu);
int ui_get_choice(const Menu* menu);
char* ui_get_string(const char* prompt);
int ui_get_integer(const char* prompt, int min, int max);
float ui_get_float(const char* prompt, float min, float max);
void ui_clear_input_buffer(void);
void ui_press_enter_to_continue(void);
void ui_display_error(const char* message);
void ui_display_success(const char* message);
void ui_display_info(const char* message);
void ui_shutdown(void);

// Menus específicos
Menu ui_create_main_menu(void);
Menu ui_create_track_menu(void);
Menu ui_create_sensor_menu(void);
Menu ui_create_user_menu(void);

#endif