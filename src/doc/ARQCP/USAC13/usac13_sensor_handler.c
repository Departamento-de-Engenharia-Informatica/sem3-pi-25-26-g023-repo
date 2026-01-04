#include "usac13.h"
#include <stdlib.h>
#include <string.h>
#include <stdio.h>

/* Declaração de funções externas do Sprint 2 (Assembly) */
/* Certifica-te que tens o extract_data.s compilado no projeto */
extern int extract_data(char* str, char* token, char* unit, int* value);

/* Inicializa a estrutura e aloca memória para os buffers */
int sensors_init(SensorData *data, const SensorsConfig *config) {
    if (!data || !config) return 0;

    /* Inicializar a zeros */
    memset(data, 0, sizeof(SensorData));

    /* Alocar buffer circular para temperatura */
    data->temp_length = config->temp.buffer_length;
    if (data->temp_length > 0) {
        data->temp_buffer = (int *)calloc(data->temp_length, sizeof(int));
        if (!data->temp_buffer) return 0;
    }

    /* Alocar buffer circular para humidade */
    data->hum_length = config->hum.buffer_length;
    if (data->hum_length > 0) {
        data->hum_buffer = (int *)calloc(data->hum_length, sizeof(int));
        if (!data->hum_buffer) {
            free(data->temp_buffer); /* Limpar se falhar o segundo */
            return 0;
        }
    }

    return 1;
}

/* Liberta a memória */
void sensors_free(SensorData *data) {
    if (!data) return;

    if (data->temp_buffer) free(data->temp_buffer);
    if (data->hum_buffer) free(data->hum_buffer);

    /* Zera os ponteiros para evitar uso após free */
    data->temp_buffer = NULL;
    data->hum_buffer = NULL;
}

/* Função Wrapper que chama o Assembly para atualizar a struct */
int update_sensors(SensorData *data, int temp_value, int hum_value) {
    if (!data) return 0;

    /* Invoca a função assembly update_sensor_buffer.s */
    return update_sensor_buffer(data, temp_value, hum_value);
}

/* Função de simulação baseada no exemplo bom */
int get_sensor_data_simulated(int *temp_value, int *hum_value) {
    /* Simular resposta exata do Arduino */
    char response[] = "TEMP &unit:celsius &value:23#HUM &unit:percentage &value:65";

    printf("[SIMULACAO] Resposta recebida do Arduino: '%s'\n", response);

    /* Variáveis auxiliares para o parser */
    char unit_temp[20] = {0};
    char unit_hum[20] = {0};

    /* 1. Extrair Temperatura (Assembly Sprint 2) */
    if (!extract_data(response, "TEMP", unit_temp, temp_value)) {
        fprintf(stderr, "[ERRO] Falha ao extrair Temperatura\n");
        return 0;
    }

    /* 2. Extrair Humidade (Assembly Sprint 2) */
    if (!extract_data(response, "HUM", unit_hum, hum_value)) {
        fprintf(stderr, "[ERRO] Falha ao extrair Humidade\n");
        return 0;
    }

    return 1; /* Sucesso */
}