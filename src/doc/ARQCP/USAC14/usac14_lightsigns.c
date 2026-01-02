#include "usac14.h"
#include <string.h>

// Exibe estado do trilho com cores (simuladas)
void display_track_status(const Track* track) {
    if (!track) return;

    const char* state_str;
    const char* color_code;

    switch (track->state) {
        case STATE_GREEN:
            state_str = "VERDE (LIVRE)";
            color_code = "\033[32m"; // Verde
            break;
        case STATE_YELLOW:
            state_str = "AMARELO (ATRIBUIDO)";
            color_code = "\033[33m"; // Amarelo
            break;
        case STATE_RED:
            state_str = "VERMELHO (OCUPADO)";
            color_code = "\033[31m"; // Vermelho
            break;
        case STATE_BLINKING:
            state_str = "VERMELHO PISCANTE (INOPERATIVO)";
            color_code = "\033[31;5m"; // Vermelho piscante
            break;
        default:
            state_str = "DESCONHECIDO";
            color_code = "\033[37m"; // Branco
    }

    printf("%sTrilho %02d: %s - %s\033[0m\n",
           color_code, track->id, state_str, track->description);
}

// Altera estado do trilho
void change_track_state(Track* track, LightState new_state) {
    if (!track) return;

    printf("Alterando trilho %d: ", track->id);

    // Primeiro desliga todos os LEDs (simulado)
    printf("Desligando todos LEDs do trilho %d... ", track->id);

    // Altera para novo estado
    track->state = new_state;

    printf("Estado alterado para ");
    display_track_status(track);
}

// Controla todos os trilhos (exemplo)
void control_all_tracks(void) {
    printf("\n=== CONTROLANDO TODOS OS TRILHOS ===\n");

    Track tracks[MAX_TRACKS];

    // Inicializa trilhos
    for (int i = 0; i < MAX_TRACKS; i++) {
        tracks[i].id = i + 1;
        tracks[i].state = STATE_GREEN;
        snprintf(tracks[i].description, 50, "Trilho %d", i + 1);
    }

    // Mostra estado inicial
    printf("Estado inicial:\n");
    for (int i = 0; i < MAX_TRACKS; i++) {
        display_track_status(&tracks[i]);
    }

    // Simula algumas mudanças
    printf("\nSimulando operações...\n");

    // Trilho 1: atribuído a trem chegando
    change_track_state(&tracks[0], STATE_YELLOW);

    // Trilho 2: ocupado
    change_track_state(&tracks[1], STATE_RED);

    // Trilho 3: inoperativo (manutenção)
    change_track_state(&tracks[2], STATE_BLINKING);

    printf("\nEstado final:\n");
    for (int i = 0; i < MAX_TRACKS; i++) {
        display_track_status(&tracks[i]);
    }
}