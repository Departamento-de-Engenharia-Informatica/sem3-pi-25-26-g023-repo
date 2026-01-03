#include "usac13.h"
#include <time.h>
#include <stdlib.h>
#include <string.h>

// Simula leitura do sensor
void get_sensor_data(SensorData* data) {
    if (!data) return;

    // Simula valores aleatórios realistas
    data->temperature = 20.0 + (rand() % 150) / 10.0;  // 20.0-35.0°C
    data->humidity = 40.0 + (rand() % 300) / 10.0;     // 40.0-70.0%

    // Timestamp atual
    time_t now = time(NULL);
    struct tm* tm_info = localtime(&now);
    strftime(data->timestamp, 20, "%Y-%m-%d %H:%M:%S", tm_info);
}

// Exibe dados formatados
void display_sensor_data(const SensorData* data) {
    if (!data) return;

    printf("\n=== DADOS DOS SENSORES ===\n");
    printf("Timestamp: %s\n", data->timestamp);
    printf("Temperatura: %.1f °C\n", data->temperature);
    printf("Humidade: %.1f %%\n", data->humidity);
    printf("===========================\n");
}

// Simula múltiplas leituras
int simulate_sensor_readings(void) {
    printf("\nSimulando leituras dos sensores...\n");

    SensorData readings[5];

    for (int i = 0; i < 5; i++) {
        get_sensor_data(&readings[i]);
        printf("\nLeitura %d:\n", i + 1);
        display_sensor_data(&readings[i]);
    }

    return 1;
}