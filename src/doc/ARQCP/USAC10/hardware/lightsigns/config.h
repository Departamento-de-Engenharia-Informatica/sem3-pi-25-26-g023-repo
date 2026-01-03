#ifndef LIGHTSIGNS_CONFIG_H
#define LIGHTSIGNS_CONFIG_H

// ============================================
// CONFIGURAÇÃO DO COMPONENTE LIGHTSIGNS
// USAC10 - Implementação do dispositivo LightSigns
// ============================================

// Configuração geral
#define NUM_TRACKS 4             // Número de trilhos suportados
#define LEDS_PER_TRACK 3         // LEDs por trilho (R, Y, G)
#define BLINK_INTERVAL 500       // Intervalo de blinking (ms)
#define COMMAND_PROCESS_DELAY 50 // Delay entre processamentos (ms)

// Pinos dos LEDs (matriz: [trilho][cor])
// Trilho 0: LEDs nos pinos 2(R), 3(Y), 4(G)
// Trilho 1: LEDs nos pinos 5(R), 6(Y), 7(G)
// Trilho 2: LEDs nos pinos 8(R), 9(Y), 10(G)
// Trilho 3: LEDs nos pinos 11(R), 12(Y), 13(G)
const int LED_PINS[NUM_TRACKS][LEDS_PER_TRACK] = {
    {2, 3, 4},   // Trilho 1
    {5, 6, 7},   // Trilho 2
    {8, 9, 10},  // Trilho 3
    {11, 12, 13} // Trilho 4
};

// Índices das cores no array LED_PINS
#define LED_RED 0
#define LED_YELLOW 1
#define LED_GREEN 2

// Comandos suportados (formato: "CMD,TT")
#define CMD_RED_ON "RE"          // Vermelho fixo
#define CMD_YELLOW_ON "YE"       // Amarelo fixo
#define CMD_GREEN_ON "GE"        // Verde fixo
#define CMD_RED_BLINK "RB"       // Vermelho piscante

// Estados dos LEDs
typedef enum {
    LED_STATE_OFF,
    LED_STATE_ON,
    LED_STATE_BLINKING
} LedState;

// Estados dos trilhos
typedef enum {
    TRACK_STATE_FREE,          // Verde
    TRACK_STATE_ASSIGNED,      // Amarelo
    TRACK_STATE_BUSY,          // Vermelho
    TRACK_STATE_INOPERATIVE    // Vermelho piscante
} TrackState;

// Estrutura para controle de trilho
typedef struct {
    TrackState state;
    LedState led_states[LEDS_PER_TRACK];
    unsigned long last_blink_time;
    bool blink_state;
} TrackControl;

// Configuração serial
#define SERIAL_BAUD_RATE 9600
#define MAX_COMMAND_LENGTH 20

// Mensagens de sistema
#define MSG_SYSTEM_INIT "USAC10-LIGHTSIGNS: Sistema inicializado"
#define MSG_READY_FOR_COMMANDS "Pronto para comandos no formato: CMD,TT"
#define MSG_TEST_START "Iniciando teste de LEDs..."
#define MSG_TEST_COMPLETE "Teste de LEDs completo."

// Mensagens de sucesso
#define SUCCESS_RED "OK: TRILHO_%d_VERMELHO"
#define SUCCESS_YELLOW "OK: TRILHO_%d_AMARELO"
#define SUCCESS_GREEN "OK: TRILHO_%d_VERDE"
#define SUCCESS_BLINK "OK: TRILHO_%d_PISCANTE"

// Mensagens de erro
#define ERROR_INVALID_FORMAT "ERROR: FORMATO_INVALIDO (esperado: CMD,TT)"
#define ERROR_INVALID_TRACK "ERROR: TRILHO_INVALIDO (1-%d)"
#define ERROR_UNKNOWN_COMMAND "ERROR: COMANDO_DESCONHECIDO"
#define ERROR_TRACK_OUT_OF_RANGE "ERROR: TRILHO_FORA_DA_FAIXA"

// Validação
#define MIN_TRACK_NUMBER 1
#define MAX_TRACK_NUMBER NUM_TRACKS

// Protótipos de funções
void init_lightsigns_system();
void process_light_command(String command);
void set_track_state(int track_num, TrackState state);
void turn_off_all_track_leds(int track_num);
void update_blinking_leds();
void test_all_leds();
void print_track_status();

#endif // LIGHTSIGNS_CONFIG_H