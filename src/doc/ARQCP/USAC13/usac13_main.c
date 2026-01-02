#include <stdio.h>
#include "usac13.h"

void display_menu(void) {
    printf("\n=== USAC13 - SENSORES DA ESTAÇÃO ===\n");
    printf("1. Ler dados atuais dos sensores\n");
    printf("2. Simular múltiplas leituras\n");
    printf("3. Mostrar comando GTH\n");
    printf("4. Testar integração\n");
    printf("0. Sair\n");
    printf("Opção: ");
}

int main(void) {
    SensorData current_data;
    int option;

    printf("USAC13 - Obter dados dos Sensores\n");
    printf("==================================\n");

    srand(time(NULL)); // Inicializa gerador aleatório

    do {
        display_menu();
        scanf("%d", &option);
        getchar(); // Limpa buffer

        switch (option) {
            case 1:
                get_sensor_data(&current_data);
                display_sensor_data(&current_data);
                break;

            case 2:
                simulate_sensor_readings();
                break;

            case 3:
                printf("\nComando para sensores: %s\n", SENSOR_CMD);
                printf("Formato esperado: TEMP&unit::celsius&value::XX#HUM&unit::percentage&value::XX\n");
                break;

            case 4:
                printf("\nTestando integração com Manager...\n");
                get_sensor_data(&current_data);
                printf("Dados prontos para envio ao Manager:\n");
                printf("TEMP&unit::celsius&value::%.1f#HUM&unit::percentage&value::%.1f\n",
                       current_data.temperature, current_data.humidity);
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