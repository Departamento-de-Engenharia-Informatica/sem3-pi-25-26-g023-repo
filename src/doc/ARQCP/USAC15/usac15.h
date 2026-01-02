#ifndef USAC15_H
#define USAC15_H

#include <stdio.h>

// Estrutura para dados do painel
typedef struct {
    float temperature;
    float humidity;
    int track_status[10];  // 0=livre, 1=atribuído, 2=ocupado, 3=inoperativo
    int active_trains;
    char timestamp[20];
} BoardData;

// Funções principais
void display_board(const BoardData* data);
void create_funny_display(const BoardData* data);
void send_to_board(const BoardData* data);

#endif