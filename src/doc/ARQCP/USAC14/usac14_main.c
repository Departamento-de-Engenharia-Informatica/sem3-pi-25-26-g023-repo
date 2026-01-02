#include <stdio.h>
#include "usac14.h"

void display_menu(void) {
    printf("\n=== USAC14 - CONTROLAR SINAIS LUMINOSOS ===\n");
    printf("1. Ver estado de um trilho\n");
    printf("2. Alterar estado de trilho\n");
    printf("3. Controlar todos os trilhos (demo)\n");
    printf("4. Mostrar comandos para LightSigns\n");
    printf("0. Sair\n");
    printf("Opção: ");
}

int main(void) {
    int option, track_id;
    LightState new_state;

    printf("USAC14 - Controlar Sinais Luminosos dos Trilhos\n");
    printf("================================================\n");

    do {
        display_menu();
        scanf("%d", &option);

        switch (option) {
            case 1: {
                Track track;
                printf("ID do trilho (1-%d): ", MAX_TRACKS);
                scanf("%d", &track_id);

                if (track_id < 1 || track_id > MAX_TRACKS) {
                    printf("ID inválido!\n");
                    break;
                }

                track.id = track_id;
                track.state = STATE_GREEN; // Estado padrão
                snprintf(track.description, 50, "Trilho %d", track_id);

                display_track_status(&track);
                break;
            }

            case 2: {
                printf("ID do trilho (1-%d): ", MAX_TRACKS);
                scanf("%d", &track_id);

                if (track_id < 1 || track_id > MAX_TRACKS) {
                    printf("ID inválido!\n");
                    break;
                }

                printf("\nNovo estado:\n");
                printf("1. VERDE (Livre)\n");
                printf("2. AMARELO (Atribuído)\n");
                printf("3. VERMELHO (Ocupado)\n");
                printf("4. VERMELHO PISCANTE (Inoperativo)\n");
                printf("Escolha: ");
                scanf("%d", &option);

                if (option < 1 || option > 4) {
                    printf("Estado inválido!\n");
                    break;
                }

                Track track;
                track.id = track_id;
                track.state = (LightState)(option - 1);
                snprintf(track.description, 50, "Trilho %d", track_id);

                change_track_state(&track, track.state);
                break;
            }

            case 3:
                control_all_tracks();
                break;

            case 4:
                printf("\nComandos para LightSigns component:\n");
                printf("• GE,x  - Verde (trilho x livre)\n");
                printf("• YE,x  - Amarelo (trilho x atribuído)\n");
                printf("• RE,x  - Vermelho (trilho x ocupado)\n");
                printf("• RB,x  - Vermelho piscante (trilho x inoperativo)\n");
                printf("Exemplo: 'GE,05' liga verde no trilho 5\n");
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