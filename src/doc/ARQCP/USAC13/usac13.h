#ifndef USAC13_H
#define USAC13_H

#include <stdio.h>

/* Configuração dos sensores (necessário para o init) */
typedef struct {
    struct {
        int buffer_length;
        int mm_window;
    } temp, hum;
} SensorsConfig;

/* Estrutura avançada com Buffers Circulares (Igual ao exemplo bom) */
typedef struct {
    /* Dados Temperatura */
    int *temp_buffer;      /* Buffer circular */
    int temp_length;       /* Tamanho total */
    int temp_nelem;        /* Elementos ocupados */
    int temp_tail;         /* Índice Cauda (leitura) */
    int temp_head;         /* Índice Cabeça (escrita) */

    /* Dados Humidade */
    int *hum_buffer;
    int hum_length;
    int hum_nelem;
    int hum_tail;
    int hum_head;

    /* Valores Atuais */
    int temp_current;
    int hum_current;
} SensorData;

/* === Funções C (Gestão de Memória e Lógica) === */
int sensors_init(SensorData *data, const SensorsConfig *config);
void sensors_free(SensorData *data);
int update_sensors(SensorData *data, int temp_val, int hum_val);

/* === Funções Assembly (Obrigatório: Struct como parâmetro) === */
/* Esta função está no ficheiro update_sensor_buffer.s */
int update_sensor_buffer(SensorData *data, int temp_value, int hum_value);

/* === Funções Auxiliares de Teste === */
/* Simula a obtenção de dados como se viessem do Arduino */
int get_sensor_data_simulated(int *temp_value, int *hum_value);

#endif