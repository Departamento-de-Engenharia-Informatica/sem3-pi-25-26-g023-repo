#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include "usac15.h"

// Gera dados aleatórios para o painel
void generate_board_data(BoardData* data) {
    if (!data) return;

    // Valores aleatórios realistas
    data->temperature = 15.0 + (rand() % 200) / 10.0;  // 15-35°C
    data->humidity = 30.0 + (rand() % 500) / 10.0;     // 30-80%
    data->active_trains = rand() % 5;                  // 0-4 trens

    // Estados aleatórios dos trilhos
    for (int i = 0; i < 10; i++) {
        data->track_status[i] = rand() % 4;  // 0-3
    }

    // Timestamp atual
    time_t now = time(NULL);
    struct tm* tm_info = localtime(&now);
    strftime(data->timestamp, 20, "%Y-%m-%d %H:%M:%S", tm_info);
}

void display_menu(void) {
    printf("\n=== USAC15 - ENVIAR DADOS PARA O PAINEL ===\n");
    printf("1. Mostrar painel sinóptico normal\n");
    printf("2. Mostrar painel divertido (com emojis)\n");
    printf("3. Enviar dados para o Board component\n");
    printf("4. Gerar dados aleatórios e mostrar\n");
    printf("0. Sair\n");
    printf("Opção: ");
}

int main(void) {
    BoardData board_data;
    int option;

    printf("USAC15 - Enviar Dados para o Board Component\n");
    printf("=============================================\n");

    srand(time(NULL));
    generate_board_data(&board_data);

    do {
        display_menu();
        scanf("%d", &option);

        switch (option) {
            case 1:
                display_board(&board_data);
                break;

            case 2:
                create_funny_display(&board_data);
                break;

            case 3:
                send_to_board(&board_data);
                break;

            case 4:
                generate_board_data(&board_data);
                printf("✅ Dados gerados com sucesso!\n");
                create_funny_display(&board_data);
                break;

            case 0:
                printf("A sair...\n");
                break;

            default:
                printf("Opção inválida!\n");
        }

    } while (option != 0);

    return 0;
}