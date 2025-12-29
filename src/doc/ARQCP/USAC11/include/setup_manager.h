#ifndef SETUP_MANAGER_H
#define SETUP_MANAGER_H

#include "data_structures.h"

typedef struct {
    StationConfig config;
    User users[MAX_USERS];
    Track tracks[MAX_TRACKS];
    SensorBuffer tempBuffer;
    SensorBuffer humidBuffer;

    int userCount;
    int trackCount;
    int trainCount;
    int logCount;

    // Ponteiros para buffers alocados dinamicamente
    int* tempBufferData;
    int* humidBufferData;
} StationSystem;

// Funções principais
int initialize_station_system(StationSystem* system, const char* configFile);
void cleanup_station_system(StationSystem* system);
int save_system_state(const StationSystem* system, const char* filename);
int load_system_state(StationSystem* system, const char* filename);

// Funções de validação
int validate_system(const StationSystem* system);
void print_system_summary(const StationSystem* system);

#endif