#include "usac16.h"
#include <string.h>

// Mostra estado de todos os trilhos
void display_tracks(const Track tracks[], int count) {
    printf("\n=== ESTADO DOS TRILHOS ===\n");
    for (int i = 0; i < count; i++) {
        const char* state_str;
        const char* color_code;

        switch (tracks[i].state) {
            case TRACK_FREE:
                state_str = "LIVRE";
                color_code = "\033[32m";
                break;
            case TRACK_ASSIGNED:
                state_str = "ATRIBUIDO";
                color_code = "\033[33m";
                break;
            case TRACK_BUSY:
                state_str = "OCUPADO";
                color_code = "\033[31m";
                break;
            case TRACK_INOPERATIVE:
                state_str = "INOPERATIVO";
                color_code = "\033[37;1m";
                break;
            default:
                state_str = "DESCONHECIDO";
                color_code = "\033[37m";
        }

        printf("%sTrilho %02d: %s", color_code, tracks[i].id, state_str);

        if (tracks[i].train_id != -1) {
            printf(" (Trem %d)", tracks[i].train_id);
        }

        printf(" - %s\033[0m\n", tracks[i].description);
    }
}

// Atribui trilho a trem chegando
void assign_track_to_train(Track* track, Train* train) {
    if (!track || !train) return;

    printf("\nAtribuindo trilho %d ao trem %d...\n", track->id, train->id);

    if (track->state == TRACK_FREE) {
        track->state = TRACK_ASSIGNED;
        track->train_id = train->id;
        train->assigned_track = track->id;
        strcpy(train->status, "stopped");

        printf("✅ Trilho %d atribuído ao trem %d (AMARELO)\n", track->id, train->id);
        printf("📋 Info enviada para o Board\n");
        printf("💡 Sinal luminoso alterado para AMARELO\n");
    } else {
        printf("❌ Trilho %d não está livre! Estado atual: ", track->id);

        switch (track->state) {
            case TRACK_ASSIGNED: printf("ATRIBUIDO\n"); break;
            case TRACK_BUSY: printf("OCUPADO\n"); break;
            case TRACK_INOPERATIVE: printf("INOPERATIVO\n"); break;
            default: printf("DESCONHECIDO\n");
        }

        emergency_stop();
    }
}

// Coloca trilho como inoperativo
void set_track_inoperative(Track* track) {
    if (!track) return;

    printf("\nColocando trilho %d como INOPERATIVO...\n", track->id);

    track->state = TRACK_INOPERATIVE;
    track->train_id = -1; // Remove trem se houver

    printf("✅ Trilho %d marcado como INOPERATIVO\n", track->id);
    printf("📋 Info enviada para o Board\n");
    printf("💡 Sinal luminoso: VERMELHO PISCANTE\n");
}

// Libera trilho
void set_track_free(Track* track) {
    if (!track) return;

    printf("\nLiberando trilho %d...\n", track->id);

    track->state = TRACK_FREE;
    track->train_id = -1;

    printf("✅ Trilho %d liberado (LIVRE)\n", track->id);
    printf("📋 Info enviada para o Board\n");
    printf("💡 Sinal luminoso alterado para VERDE\n");
}

// Dá ordem de partida para trem parado
void give_departure_order(Train* train, Track* track) {
    if (!train || !track) return;

    printf("\nDando ordem de partida para trem %d no trilho %d...\n",
           train->id, track->id);

    if (train->assigned_track == track->id &&
        strcmp(train->status, "stopped") == 0) {

        strcpy(train->status, "departing");
        track->state = TRACK_BUSY;

        printf("✅ Ordem de partida dada ao trem %d\n", train->id);
        printf("📋 Info enviada para o Board\n");
        printf("💡 Sinal luminoso alterado para VERMELHO\n");
        printf("🚂 Trem %d está partindo...\n", train->id);

        // Após partida, trilho fica livre
        printf("\n⏱️  3 segundos depois...\n");
        printf("✅ Trem %d partiu\n", train->id);
        set_track_free(track);

    } else {
        printf("❌ Trem %d não está parado no trilho %d\n", train->id, track->id);
    }
}

// Ordem de parada de emergência
void emergency_stop(void) {
    printf("\n🚨🚨🚨 ORDEM DE PARADA DE EMERGÊNCIA 🚨🚨🚨\n");
    printf("Nenhum trilho disponível para trem chegando!\n");
    printf("⚠️  Enviando comando de parada de emergência...\n");
    printf("📢 Todos os trens devem parar imediatamente!\n");
    printf("🔴 Todos os sinais alterados para VERMELHO\n");
}