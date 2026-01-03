#include "ui.h"

// Cores para terminal (opcional)
#define COLOR_RED     "\033[31m"
#define COLOR_GREEN   "\033[32m"
#define COLOR_YELLOW  "\033[33m"
#define COLOR_BLUE    "\033[34m"
#define COLOR_MAGENTA "\033[35m"
#define COLOR_CYAN    "\033[36m"
#define COLOR_RESET   "\033[0m"

// Inicializa UI
void ui_init(void) {
    printf("🎮 UI Component inicializado\n");
    printf("✅ Pronto para receber comandos\n");
}

// Mostra menu
void ui_display_menu(const Menu* menu) {
    if (!menu) return;

    printf("\n%s", COLOR_CYAN);
    printf("╔════════════════════════════════════════╗\n");
    printf("║                                        ║\n");

    // Título centralizado
    int title_len = strlen(menu->title);
    int spaces = (40 - title_len) / 2;
    printf("║");
    for (int i = 0; i < spaces; i++) printf(" ");
    printf("%s", menu->title);
    for (int i = 0; i < 40 - spaces - title_len; i++) printf(" ");
    printf("║\n");

    printf("║                                        ║\n");
    printf("╠════════════════════════════════════════╣\n");

    // Opções
    for (int i = 0; i < menu->option_count; i++) {
        printf("║  %2d. %-34s  ║\n", i + 1, menu->options[i]);
    }

    printf("║                                        ║\n");
    printf("║  0. Sair                               ║\n");
    printf("║                                        ║\n");
    printf("╚════════════════════════════════════════╝\n");
    printf("%s", COLOR_RESET);
}

// Obtém escolha do usuário (com validação robusta)
int ui_get_choice(const Menu* menu) {
    if (!menu) return 0;

    char input[MAX_INPUT];
    int choice = -1;
    int valid = 0;

    while (!valid) {
        printf("\n%sEscolha uma opção (0-%d): %s",
               COLOR_GREEN, menu->option_count, COLOR_RESET);

        if (fgets(input, sizeof(input), stdin) == NULL) {
            ui_display_error("Erro na leitura da entrada");
            return 0;
        }

        // Remove newline
        input[strcspn(input, "\n")] = '\0';

        // Verifica se é número
        valid = 1;
        for (int i = 0; input[i] != '\0'; i++) {
            if (!isdigit(input[i])) {
                ui_display_error("❌ Entrada inválida! Use apenas números.");
                valid = 0;
                break;
            }
        }

        if (valid) {
            choice = atoi(input);
            if (choice < 0 || choice > menu->option_count) {
                ui_display_error("❌ Opção fora do intervalo válido!");
                valid = 0;
            }
        }
    }

    return choice;
}

// Obtém string do usuário
char* ui_get_string(const char* prompt) {
    static char buffer[MAX_INPUT];

    printf("%s%s: %s", COLOR_BLUE, prompt, COLOR_RESET);

    if (fgets(buffer, sizeof(buffer), stdin) == NULL) {
        return NULL;
    }

    // Remove newline
    buffer[strcspn(buffer, "\n")] = '\0';

    return buffer;
}

// Obtém inteiro com validação
int ui_get_integer(const char* prompt, int min, int max) {
    char input[MAX_INPUT];
    int value;
    int valid = 0;

    while (!valid) {
        printf("%s%s (%d-%d): %s", COLOR_BLUE, prompt, min, max, COLOR_RESET);

        if (fgets(input, sizeof(input), stdin) == NULL) {
            return min;
        }

        // Verifica se é número
        valid = 1;
        for (int i = 0; input[i] != '\0' && input[i] != '\n'; i++) {
            if (!isdigit(input[i]) && !(i == 0 && input[i] == '-')) {
                ui_display_error("❌ Entrada inválida! Use apenas números.");
                valid = 0;
                break;
            }
        }

        if (valid) {
            value = atoi(input);
            if (value < min || value > max) {
                printf("%s❌ Valor fora do intervalo!%s\n", COLOR_RED, COLOR_RESET);
                valid = 0;
            }
        }
    }

    return value;
}

// Obtém float com validação
float ui_get_float(const char* prompt, float min, float max) {
    char input[MAX_INPUT];
    float value;
    int valid = 0;

    while (!valid) {
        printf("%s%s (%.1f-%.1f): %s", COLOR_BLUE, prompt, min, max, COLOR_RESET);

        if (fgets(input, sizeof(input), stdin) == NULL) {
            return min;
        }

        // Verifica formato float
        int dots = 0;
        valid = 1;
        for (int i = 0; input[i] != '\0' && input[i] != '\n'; i++) {
            if (!isdigit(input[i])) {
                if (input[i] == '.' && dots == 0) {
                    dots++;
                } else if (i == 0 && input[i] == '-') {
                    // OK, número negativo
                } else {
                    ui_display_error("❌ Formato inválido! Use números decimais.");
                    valid = 0;
                    break;
                }
            }
        }

        if (valid) {
            value = atof(input);
            if (value < min || value > max) {
                printf("%s❌ Valor fora do intervalo!%s\n", COLOR_RED, COLOR_RESET);
                valid = 0;
            }
        }
    }

    return value;
}

// Limpa buffer de entrada
void ui_clear_input_buffer(void) {
    int c;
    while ((c = getchar()) != '\n' && c != EOF);
}

// Aguarda Enter
void ui_press_enter_to_continue(void) {
    printf("\n%sPressione Enter para continuar...%s", COLOR_YELLOW, COLOR_RESET);
    getchar();
}

// Mensagens
void ui_display_error(const char* message) {
    printf("\n%s❌ %s%s\n", COLOR_RED, message, COLOR_RESET);
}

void ui_display_success(const char* message) {
    printf("\n%s✅ %s%s\n", COLOR_GREEN, message, COLOR_RESET);
}

void ui_display_info(const char* message) {
    printf("\n%sℹ️  %s%s\n", COLOR_CYAN, message, COLOR_RESET);
}

// Encerra UI
void ui_shutdown(void) {
    printf("\n👋 UI Component a encerrar...\n");
    printf("✅ UI encerrado\n");
}

// ============================================
// MENUS ESPECÍFICOS
// ============================================

Menu ui_create_main_menu(void) {
    Menu menu;
    strcpy(menu.title, "MENU PRINCIPAL");

    strcpy(menu.options[0], "Gestão de Trilhos");
    strcpy(menu.options[1], "Controlar Sinais Luminosos");
    strcpy(menu.options[2], "Ler Sensores");
    strcpy(menu.options[3], "Ver Painel Sinóptico");
    strcpy(menu.options[4], "Gestão de Usuários");
    strcpy(menu.options[5], "Configuração do Sistema");
    strcpy(menu.options[6], "Ver Logs do Sistema");

    menu.option_count = 7;
    return menu;
}

Menu ui_create_track_menu(void) {
    Menu menu;
    strcpy(menu.title, "GESTÃO DE TRILHOS");

    strcpy(menu.options[0], "Atribuir trilho a trem");
    strcpy(menu.options[1], "Liberar trilho");
    strcpy(menu.options[2], "Marcar trilho como inoperativo");
    strcpy(menu.options[3], "Dar ordem de partida");
    strcpy(menu.options[4], "Ver estado de todos trilhos");
    strcpy(menu.options[5], "Ordem de parada de emergência");

    menu.option_count = 6;
    return menu;
}

Menu ui_create_sensor_menu(void) {
    Menu menu;
    strcpy(menu.title, "SENSORES");

    strcpy(menu.options[0], "Ler temperatura atual");
    strcpy(menu.options[1], "Ler humidade atual");
    strcpy(menu.options[2], "Ler ambos sensores");
    strcpy(menu.options[3], "Configurar sensores");
    strcpy(menu.options[4], "Ver histórico");

    menu.option_count = 5;
    return menu;
}

Menu ui_create_user_menu(void) {
    Menu menu;
    strcpy(menu.title, "USUÁRIOS");

    strcpy(menu.options[0], "Login");
    strcpy(menu.options[1], "Logout");
    strcpy(menu.options[2], "Criar novo usuário");
    strcpy(menu.options[3], "Listar usuários");
    strcpy(menu.options[4], "Alterar senha");

    menu.option_count = 5;
    return menu;
}