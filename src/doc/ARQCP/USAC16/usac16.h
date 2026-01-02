#ifndef USAC16_H
#define USAC16_H

#include <stdio.h>

#define MAX_TRACKS 10
#define MAX_TRAINS 5

// Estados dos trilhos
typedef enum {
    TRACK_FREE = 0,      // Livre
    TRACK_ASSIGNED,      // Atribuído
    TRACK_BUSY,          // Ocupado
    TRACK_INOPERATIVE    // Inoperativo
} TrackState;

// Estrutura para trilho
typedef struct {
    int id;
    TrackState state;
    int train_id;        // -1 se não tem trem
    char description[50];
} Track;

// Estrutura para trem
typedef struct {
    int id;
    int assigned_track;  // -1 se não atribuído
    char status[20];     // "stopped", "in_transit", "departing"
} Train;

// Funções principais
void display_tracks(const Track tracks[], int count);
void assign_track_to_train(Track* track, Train* train);
void set_track_inoperative(Track* track);
void set_track_free(Track* track);
void give_departure_order(Train* train, Track* track);
void emergency_stop(void);

#endif