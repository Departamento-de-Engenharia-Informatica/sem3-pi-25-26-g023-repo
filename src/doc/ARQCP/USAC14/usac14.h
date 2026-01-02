#ifndef USAC14_H
#define USAC14_H

#include <stdio.h>

#define MAX_TRACKS 10

// Estados dos sinais
typedef enum {
    STATE_GREEN = 0,    // Livre
    STATE_YELLOW,       // Atribuído
    STATE_RED,          // Ocupado
    STATE_BLINKING      // Inoperativo (vermelho piscante)
} LightState;

// Estrutura para trilho
typedef struct {
    int id;
    LightState state;
    char description[50];
} Track;

// Funções principais
void display_track_status(const Track* track);
void change_track_state(Track* track, LightState new_state);
void control_all_tracks(void);

#endif