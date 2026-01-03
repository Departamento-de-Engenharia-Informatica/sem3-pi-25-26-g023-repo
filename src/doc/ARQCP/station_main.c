#include "ui/ui.h"
#include "board/board.h"
#include "manager/manager.h"
#include <time.h>

// Dados globais para simulação
BoardData board_data;
StationManager* station_manager = NULL;

// Atualiza board com dados do manager
void update_board_from_manager(void) {
    if (!station_manager) return;

    board_data.temperature = station_manager->sensor_config.temperature;
    board_data.humidity = station_manager->sensor_config.humidity;
    board_data.timestamp = time(NULL);

    // Atualiza estados dos trilhos
    for (int i = 0; i < 10 && i < station_manager->track_count; i++) {
        switch (station_manager->tracks[i].state) {
            case TRACK_FREE: board_data.track_status[i] = 0; break;
            case TRACK_ASSIGNED: board_data.track_status[i] = 1; break;
            case TRACK_BUSY: board_data.track_status[i] = 2; break;
            case TRACK_INOPERATIVE: board_data.track_status[i] = 3; break;
        }
    }

    // Conta trens ativos
    board_data.active_trains = 0;
    for (int i = 0; i < station_manager->track_count; i++) {
        if (station_manager->tracks[i].train_id != -1) {
            board_data.active_trains++;
        }
    }
}

// Callback quando manager tem dados para board
void on_manager_to_board(const char* message) {
    strncpy(board_data.message, message, 99);
    board_data.message[99] = '\0';

    update_board_from_manager();
    board_display_funny(&board_data);
}

// Programa principal integrado
int main(void) {
    printf("🚂 STATION MANAGEMENT SYSTEM - ARQCP Sprint 3\n");
    printf("=============================================\n\n");

    // 1. Inicializar componentes
    ui_init();
    board_init();

    station_manager = manager_create();
    if (!station_manager) {
        ui_display_error("Falha ao criar manager!");
        return 1;
    }

    // 2. Login
    ui_display_info("=== LOGIN NO SISTEMA ===");
    char* username = ui_get_string("Username");
    char* password = ui_get_string("Password");

    User* user = manager_login(station_manager, username, password);
    if (!user) {
        ui_display_error("Login falhou!");
        manager_destroy(station_manager);
        return 1;
    }

    ui_display_success("Login bem-sucedido!");

    // 3. Menu principal integrado
    Menu main_menu = ui_create_main_menu();
    int choice;

    do {
        update_board_from_manager();

        ui_display_menu(&main_menu);
        choice = ui_get_choice(&main_menu);

        switch (choice) {
            case 1: { // Gestão de Trilhos
                Menu track_menu = ui_create_track_menu();
                ui_display_menu(&track_menu);
                int track_choice = ui_get_choice(&track_menu);

                if (track_choice == 1) { // Atribuir trilho
                    int track_id = ui_get_integer("Número do trilho", 1, 99);
                    int train_id = ui_get_integer("ID do trem", 100, 999);

                    char cmd[50];
                    snprintf(cmd, 50, "TRACK_ASSIGN %d %d", track_id, train_id);
                    manager_process_command(station_manager, cmd);

                    on_manager_to_board("Trilho atribuído");
                }
                // ... outros casos do track_menu
                break;
            }

            case 2: { // Controlar Sinais
                int track_id = ui_get_integer("Trilho", 1, 99);
                int state = ui_get_integer("Estado (1=Verde,2=Amarelo,3=Vermelho,4=Piscante)", 1, 4);

                char* state_cmd;
                switch (state) {
                    case 1: state_cmd = "GE"; break;
                    case 2: state_cmd = "YE"; break;
                    case 3: state_cmd = "RE"; break;
                    case 4: state_cmd = "RB"; break;
                    default: state_cmd = "GE";
                }

                char cmd[20];
                snprintf(cmd, 20, "%s,%d", state_cmd, track_id);

                // Envia para LightSigns via Manager
                manager_send_to_lightsigns(station_manager, cmd);
                on_manager_to_board("Sinal alterado");
                break;
            }

            case 3: { // Ler Sensores
                manager_request_sensor_data(station_manager);
                on_manager_to_board("Sensores lidos");
                break;
            }

            case 4: { // Ver Painel
                update_board_from_manager();
                strcpy(board_data.message, "Visualização do painel");
                board_display_funny(&board_data);
                ui_press_enter_to_continue();
                break;
            }

            case 5: { // Gestão de Usuários
                ui_display_info("Gestão de usuários via Manager");
                // Chamar funções do manager para users
                break;
            }

            case 6: { // Configuração
                on_manager_to_board("Menu de configuração");
                break;
            }

            case 7: { // Logs
                manager_display_status(station_manager);
                ui_press_enter_to_continue();
                break;
            }

            case 0:
                ui_display_info("A encerrar sistema...");
                break;
        }

    } while (choice != 0);

    // 4. Cleanup
    manager_logout(station_manager);
    manager_destroy(station_manager);
    board_shutdown();
    ui_shutdown();

    printf("\n✅ Sistema encerrado com sucesso!\n");
    return 0;
}