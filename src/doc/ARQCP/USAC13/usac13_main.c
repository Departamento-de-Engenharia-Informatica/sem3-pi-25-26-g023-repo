/* usac13_main.c - Versão Hardware Real */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <fcntl.h>
#include <termios.h>
#include <errno.h>
#include "usac13.h"

/* Definições da Porta Série (Ajusta conforme necessário, ex: ttyACM0 ou ttyUSB0) */
#define SERIAL_PORT "/dev/ttyUSB0"
#define BAUD_RATE B115200

/* Declaração da função Assembly do Sprint 2 */
extern int extract_data(char* str, char* token, char* unit, int* value);

/* =========================================================================
   FUNÇÕES DE CONFIGURAÇÃO DA PORTA SÉRIE (HARDWARE)
   ========================================================================= */

/* Configura a porta série para comunicação com o Arduino */
int setup_serial_port(const char *portname) {
    int fd = open(portname, O_RDWR | O_NOCTTY | O_SYNC);
    if (fd < 0) {
        fprintf(stderr, "[ERRO] Erro ao abrir %s: %s\n", portname, strerror(errno));
        return -1;
    }

    struct termios tty;
    if (tcgetattr(fd, &tty) != 0) {
        fprintf(stderr, "[ERRO] tcgetattr: %s\n", strerror(errno));
        return -1;
    }

    /* Configurar Baud Rate */
    cfsetospeed(&tty, BAUD_RATE);
    cfsetispeed(&tty, BAUD_RATE);

    /* Configurar flags (8N1 - 8 data bits, No parity, 1 stop bit) */
    tty.c_cflag = (tty.c_cflag & ~CSIZE) | CS8; /* 8 bits */
    tty.c_cflag &= ~PARENB;                     /* Sem paridade */
    tty.c_cflag &= ~CSTOPB;                     /* 1 stop bit */
    tty.c_cflag |= (CLOCAL | CREAD);            /* Ignorar modem lines, enable receiver */

    /* Raw mode (sem processamento de input/output) */
    tty.c_lflag &= ~(ICANON | ECHO | ECHOE | ISIG);
    tty.c_oflag &= ~OPOST;

    /* Timeouts (Importante para não bloquear para sempre) */
    tty.c_cc[VMIN]  = 0;            /* Non-blocking read */
    tty.c_cc[VTIME] = 20;           /* 2 segundos timeout (decisegundos) */

    if (tcsetattr(fd, TCSANOW, &tty) != 0) {
        fprintf(stderr, "[ERRO] tcsetattr: %s\n", strerror(errno));
        return -1;
    }

    return fd;
}

/* Envia comando GTH e lê a resposta do sensor real */
int get_sensor_data_hardware(int fd, int *temp_val, int *hum_val) {
    char buffer[256];
    char cmd[] = "GTH"; /* Comando acordado com a equipa de Embedded */
    int n;

    /* 1. Limpar lixo do buffer de entrada */
    tcflush(fd, TCIFLUSH);

    /* 2. Enviar pedido */
    n = write(fd, cmd, strlen(cmd));
    if (n < 0) {
        perror("Falha no write");
        return 0;
    }

    /* 3. Aguardar resposta (Arduino demora a processar) */
    usleep(200000); /* 200ms delay */

    /* 4. Ler resposta */
    memset(buffer, 0, sizeof(buffer));
    n = read(fd, buffer, sizeof(buffer) - 1);

    if (n > 0) {
        buffer[n] = '\0';
        printf("    [RX HARDWARE]: %s\n", buffer); // Debug: ver o que chegou

        char unit_temp[20] = {0};
        char unit_hum[20] = {0};

        /* 5. Usar Assembly (Sprint 2) para extrair dados da string real */
        int res_t = extract_data(buffer, "TEMP", unit_temp, temp_val);
        int res_h = extract_data(buffer, "HUM", unit_hum, hum_val);

        if (!res_t || !res_h) {
            printf("    [ERRO] Falha no parser Assembly (formato inválido?)\n");
            return 0;
        }
        return 1; // Sucesso
    }

    printf("    [ERRO] Sem resposta do sensor (timeout)\n");
    return 0;
}

/* Helper para printar o estado (igual ao anterior) */
void print_buffer_status(SensorData *data) {
    printf("   > Buffer Temp (N=%d): [ ", data->temp_nelem);
    for(int i = 0; i < data->temp_nelem; i++) {
        int idx = (data->temp_tail + i) % data->temp_length;
        printf("%d ", data->temp_buffer[idx]);
    }
    printf("]\n");

    printf("   > Buffer Hum  (N=%d): [ ", data->hum_nelem);
    for(int i = 0; i < data->hum_nelem; i++) {
        int idx = (data->hum_tail + i) % data->hum_length;
        printf("%d ", data->hum_buffer[idx]);
    }
    printf("]\n\n");
}

/* =========================================================================
   MAIN
   ========================================================================= */
int main(void) {
    printf("=== USAC13 - Leitura de Sensores Reais (Hardware) ===\n");

    /* 1. Inicializar Hardware */
    printf("[1] A abrir porta série %s...\n", SERIAL_PORT);
    int serial_fd = setup_serial_port(SERIAL_PORT);

    if (serial_fd < 0) {
        printf("[FATAL] Não foi possível ligar ao sensor. Verifica o cabo ou permissões (sudo).\n");
        return 1;
    }
    printf("    Ligação estabelecida com sucesso.\n\n");

    /* 2. Configuração de Memória */
    SensorsConfig config;
    config.temp.buffer_length = 5;
    config.hum.buffer_length = 5;

    SensorData sensor_data;

    if (!sensors_init(&sensor_data, &config)) {
        close(serial_fd);
        return 1;
    }
    printf("[2] Buffers inicializados.\n\n");

    /* 3. Loop de Leitura Real */
    printf("[3] A iniciar ciclo de leitura (CTRL+C para sair)...\n");

    /* Fazemos 10 leituras como exemplo */
    for (int i = 0; i < 10; i++) {
        printf("--- Leitura #%d ---\n", i + 1);

        int t, h;

        /* Chamar função de Hardware em vez da simulada */
        if (get_sensor_data_hardware(serial_fd, &t, &h)) {

            /* Lógica C + Assembly para atualizar struct */
            if (update_sensors(&sensor_data, t, h)) {
                printf("    Atualizado: Temp=%d, Hum=%d\n",
                       sensor_data.temp_current, sensor_data.hum_current);
                print_buffer_status(&sensor_data);
            }
        }

        /* Esperar 1 segundo entre leituras (polling) */
        sleep(1);
    }

    /* 4. Limpeza */
    printf("[4] A terminar...\n");
    sensors_free(&sensor_data);
    close(serial_fd);

    return 0;
}