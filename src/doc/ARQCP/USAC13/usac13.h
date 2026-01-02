#ifndef USAC13_H
#define USAC13_H

#include <stdio.h>

#define MAX_SENSOR_DATA 50
#define SENSOR_CMD "GTH"

// Estrutura para dados do sensor
typedef struct {
    float temperature;
    float humidity;
    char timestamp[20];
} SensorData;

// Funções principais
void get_sensor_data(SensorData* data);
void display_sensor_data(const SensorData* data);
int simulate_sensor_readings(void);

#endif