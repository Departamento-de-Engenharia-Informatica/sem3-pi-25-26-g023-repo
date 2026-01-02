#include <stdio.h>
#include <stdlib.h>
#include "usac16.h"

// Inicializa trilhos
void init_tracks(Track tracks[]) {
    for (int i = 0; i < MAX_TRACKS; i++) {
        tracks[i].id = i + 1;
        tracks[i].state = TRACK_FREE;
        tracks[i].train_id = -1;
        snprintf(tracks[i].description, 50, "Trilho %d", i + 1);
    }
}

// Inicializa trens
void init_trains(Train trains[]) {
    for (int i = 0; i < MAX_TRAINS; i++) {
        trains[i].id = 100 + i; // IDs 100, 101, 102...
        trains[i].assigned_track = -1;
        strcpy(trains[i].status, "in_transit");
    }
}

void display_menu(void) {
    printf("\n=== USAC16 - GESTÃO DE TRILHOS ===\n");
    printf("1. Ver estado dos trilhos\n");
    printf("2. Atribuir trilho a trem chegando\n");
    printf("3. Colocar trilho como inoperativo\n");
    printf("4. Liberar trilho\n");
    printf("5. Dar ordem de partida a trem\n");
    printf("6. Ordem de parada de emergência\n");
    printf("7. Demo: Cenário completo\n");
    printf("0. Sair\n");
    printf("Opção: ");
}

// Demonstração de cenário completo
void demo_scenario(Track tracks[], Train trains[]) {
    printf("\n🎬 DEMONSTRAÇÃO DE CENÁRIO COMPLETO\n");
    printf("===================================\n");

    // 1. Mostrar estado inicial
    printf("\n1. Estado inicial dos trilhos:\n");
    display_tracks(tracks, MAX_TRACKS);

    // 2. Trem 100 chega e é atribuído ao trilho 1
    printf("\n2. Trem 100 chegando à estação...\n");
    assign_track_to_train(&tracks[0], &trains[0]);

    // 3. Mostrar estado após atribuição
    printf("\n3. Estado após atribuição:\n");
    display_tracks(tracks, MAX_TRACKS);

    // 4. Dar ordem de partida
    printf("\n4. Dando ordem de partida ao trem 100...\n");
    give_departure_order(&trains[0], &tracks[0]);

    // 5. Mostrar estado final
    printf("\n5. Estado final:\n");
    display_tracks(tracks, MAX_TRACKS);

    printf("\n✅ Demonstração completa!\n");
}

int main(void) {
    Track tracks[MAX_TRACKS];
    Train trains[MAX_TRAINS];
    int option, track_id, train_id;

    printf("USAC16 - Gestão de Trilhos da Estação\n");
    printf("======================================\n");

    init_tracks(tracks);
    init_trains(trains);

    do {
        display_menu();
        scanf("%d", &option);

        switch (option) {
            case 1:
                display_tracks(tracks, MAX_TRACKS);
                break;

            case 2:
                printf("ID do trilho (1-%d): ", MAX_TRACKS);
                scanf("%d", &track_id);
                printf("ID do trem (100-%d): ", 100 + MAX_TRAINS - 1);
                scanf("%d", &train_id);

                if (track_id < 1 || track_id > MAX_TRACKS) {
                    printf("❌ Trilho inválido!\n");
                    break;
                }

                if (train_id < 100 || train_id >= 100 + MAX_TRAINS) {
                    printf("❌ Trem inválido!\n");
                    break;
                }

                assign_track_to_train(&tracks[track_id-1], &trains[train_id-100]);
                break;

            case 3:
                printf("ID do trilho (1-%d): ", MAX_TRACKS);
                scanf("%d", &track_id);

                if (track_id < 1 || track_id > MAX_TRACKS) {
                    printf("❌ Trilho inválido!\n");
                    break;
                }

                set_track_inoperative(&tracks[track_id-1]);
                break;

            case 4:
                printf("ID do trilho (1-%d): ", MAX_TRACKS);
                scanf("%d", &track_id);

                if (track_id < 1 || track_id > MAX_TRACKS) {
                    printf("❌ Trilho inválido!\n");
                    break;
                }

                set_track_free(&tracks[track_id-1]);
                break;

            case 5:
                printf("ID do trem (100-%d): ", 100 + MAX_TRAINS - 1);
                scanf("%d", &train_id);
                printf("ID do trilho (1-%d): ", MAX_TRACKS);
                scanf("%d", &track_id);

                if (train_id < 100 || train_id >= 100 + MAX_TRAINS) {
                    printf("❌ Trem inválido!\n");
                    break;
                }

                if (track_id < 1 || track_id > MAX_TRACKS) {
                    printf("❌ Trilho inválido!\n");
                    break;
                }

                give_departure_order(&trains[train_id-100], &tracks[track_id-1]);
                break;

            case 6:
                emergency_stop();
                break;

            case 7:
                demo_scenario(tracks, trains);
                break;

            case 0:
                printf("A sair...\n");
                break;

            default:
                printf("❌ Opção inválida!\n");
        }

    } while (option != 0);

    return 0;
}