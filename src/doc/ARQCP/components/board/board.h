#ifndef BOARD_H
#define BOARD_H

#include <stdio.h>
#include <time.h>

// Dados para display no board
typedef struct {
    float temperature;
    float humidity;
    int track_status[10];  // 0=livre, 1=atribuído, 2=ocupado, 3=inoperativo
    int active_trains;
    char message[100];
    time_t timestamp;
} BoardData;

// Funções principais
void board_init(void);
void board_display(const BoardData* data);
void board_display_funny(const BoardData* data);
void board_clear(void);
void board_shutdown(void);

#endif