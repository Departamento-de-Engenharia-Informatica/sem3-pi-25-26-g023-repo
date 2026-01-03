#ifndef SENSORS_CONFIG_H
#define SENSORS_CONFIG_H

// ============================================
// CONFIGURAÇÃO DO COMPONENTE SENSORS
// USAC10 - Implementação do dispositivo Sensors
// ============================================

// Configurações do sensor DHT
#define DHT_PIN 15               // Pino GPIO do sensor DHT
#define DHT_TYPE DHT11           // Tipo de sensor (DHT11, DHT22)
#define DHT_SAMPLE_INTERVAL 2000 // Intervalo entre leituras (ms)

// Configurações do filtro de mediana móvel
#define MEDIAN_WINDOW_SIZE 5     // Tamanho da janela para mediana móvel
#define BUFFER_SIZE 10           // Tamanho do buffer circular

// Configurações de comunicação serial
#define SERIAL_BAUD_RATE 9600    // Velocidade da comunicação serial
#define COMMAND_TIMEOUT 1000     // Timeout para comandos (ms)

// Comandos suportados
#define CMD_GET_DATA "GTH"       // Comando para obter dados dos sensores
#define CMD_STATUS "STATUS"      // Comando para status do sensor
#define CMD_RESET "RESET"        // Comando para resetar o sensor

// Formatos de saída de dados
#define FORMAT_TEMP "TEMP&unit:celsius&value:"
#define FORMAT_HUM "HUM&unit:percent&value:"
#define DATA_SEPARATOR "#"
#define LINE_TERMINATOR "\n"

// Valores limites
#define TEMP_MIN -20.0          // Temperatura mínima (°C)
#define TEMP_MAX 60.0           // Temperatura máxima (°C)
#define HUM_MIN 0.0             // Humidade mínima (%)
#define HUM_MAX 100.0           // Humidade máxima (%)

// Códigos de erro
#define ERROR_SENSOR_READ_FAIL "ERROR: SENSOR_READ_FAIL"
#define ERROR_INVALID_COMMAND "ERROR: COMANDO_INVALIDO"
#define ERROR_OUT_OF_RANGE "ERROR: VALOR_FORA_DA_FAIXA"
#define ERROR_SENSOR_NOT_FOUND "ERROR: SENSOR_NAO_ENCONTRADO"

// Mensagens de status
#define MSG_SENSOR_INIT "USAC10-SENSORS: Sistema inicializado"
#define MSG_WAITING_CMD "Aguardando comando GTH..."
#define MSG_SENDING_DATA "Enviando dados dos sensores..."
#define MSG_FILTER_ACTIVE "Filtro de mediana ativo (janela: %d)"

// Estrutura para dados do sensor
typedef struct {
    float temperature;
    float humidity;
    unsigned long timestamp;
    int read_error;
} SensorData;

// Estrutura para buffer circular
typedef struct {
    float buffer[MEDIAN_WINDOW_SIZE];
    int head;
    int tail;
    int count;
    bool full;
} CircularBuffer;

// Protótipos de funções
void init_sensor_system();
void read_sensor_data(SensorData* data);
void process_serial_command(String command);
void send_sensor_data();
void update_circular_buffer(CircularBuffer* buffer, float value);
float calculate_median(CircularBuffer* buffer);
void print_sensor_status();

#endif // SENSORS_CONFIG_H