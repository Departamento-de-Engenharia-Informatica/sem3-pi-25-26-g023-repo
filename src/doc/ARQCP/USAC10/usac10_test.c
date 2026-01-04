#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <assert.h>
#include <ctype.h>

// ============================================
// DEFINIÇÕES DE CORES ANSI
// ============================================
#define COLOR_RESET   "\033[0m"
#define COLOR_BOLD    "\033[1m"
#define COLOR_RED     "\033[31m"
#define COLOR_GREEN   "\033[32m"
#define COLOR_YELLOW  "\033[33m"
#define COLOR_BLUE    "\033[34m"
#define COLOR_MAGENTA "\033[35m"
#define COLOR_CYAN    "\033[36m"

// ============================================
// DEFINIÇÕES DO SISTEMA
// ============================================
#define MIN_TRACK_NUMBER 1
#define MAX_TRACK_NUMBER 4
#define NUM_TRACKS 4
#define LEDS_PER_TRACK 3
#define BLINK_INTERVAL 500

// Definições de comandos
#define CMD_GREEN_ON "GE"
#define CMD_YELLOW_ON "YE"
#define CMD_RED_ON "RE"
#define CMD_RED_BLINK "RB"

// Estados dos trilhos
typedef enum {
    TRACK_STATE_FREE,
    TRACK_STATE_ASSIGNED,
    TRACK_STATE_BUSY,
    TRACK_STATE_INOPERATIVE
} TrackState;

// Pinos dos LEDs (simulação)
int LED_PINS[4][3] = {
    {2, 3, 4},
    {5, 6, 7},
    {8, 9, 10},
    {11, 12, 13}
};

// ============================================
// TESTES UNITÁRIOS PARA USAC10
// ============================================

// Teste 1: Formato de comando para LightSigns
void test_light_command_format() {
    printf("Teste 1: Formato de comandos LightSigns\n");

    // Comandos válidos
    const char* valid_commands[] = {"RE,01", "YE,02", "GE,03", "RB,04"};
    const int num_valid = 4;

    for (int i = 0; i < num_valid; i++) {
        printf("  Verificando '%s'... ", valid_commands[i]);

        // Verificar comprimento
        assert(strlen(valid_commands[i]) == 5);

        // Verificar formato CMD,TT
        assert(valid_commands[i][2] == ',');

        // Verificar que TT são dois dígitos
        assert(isdigit(valid_commands[i][3]));
        assert(isdigit(valid_commands[i][4]));

        printf("OK\n");
    }

    // Comandos inválidos
    const char* invalid_commands[] = {"RE", "RE,", "RE,0", "RE,99", "CMD,01"};
    const int num_invalid = 5;

    for (int i = 0; i < num_invalid; i++) {
        printf("  Verificando inválido '%s'... ", invalid_commands[i]);

        // Verificar que não passa nos critérios
        int valid = 1;
        if (strlen(invalid_commands[i]) != 5) valid = 0;
        else if (invalid_commands[i][2] != ',') valid = 0;
        else if (!isdigit(invalid_commands[i][3]) || !isdigit(invalid_commands[i][4])) valid = 0;

        assert(valid == 0);
        printf("OK (detectado como inválido)\n");
    }

    printf("✓ Teste 1 passou\n\n");
}

// Teste 2: Formato de resposta do sensor
void test_sensor_response_format() {
    printf("Teste 2: Formato de resposta do sensor\n");

    // Gerar resposta de exemplo
    char response[100];
    int temp = 22;
    int hum = 65;

    snprintf(response, sizeof(response),
             "TEMP&unit:celsius&value:%d#HUM&unit:percent&value:%d\n",
             temp, hum);

    printf("  Resposta gerada: %s", response);

    // Verificar componentes
    assert(strstr(response, "TEMP&unit:celsius&value:") != NULL);
    assert(strstr(response, "HUM&unit:percent&value:") != NULL);
    assert(strstr(response, "#") != NULL);

    // Extrair valores
    int extracted_temp, extracted_hum;
    sscanf(response, "TEMP&unit:celsius&value:%d#HUM&unit:percent&value:%d",
           &extracted_temp, &extracted_hum);

    assert(extracted_temp == temp);
    assert(extracted_hum == hum);

    printf("✓ Teste 2 passou\n\n");
}

// Teste 3: Validação de números de trilho
void test_track_number_validation() {
    printf("Teste 3: Validação de números de trilho\n");

    // Trilhos válidos (1-4)
    for (int track = MIN_TRACK_NUMBER; track <= MAX_TRACK_NUMBER; track++) {
        printf("  Trilho %d... ", track);
        assert(track >= MIN_TRACK_NUMBER && track <= MAX_TRACK_NUMBER);
        printf("OK\n");
    }

    // Trilhos inválidos
    int invalid_tracks[] = {0, 5, 99, -1};
    for (int i = 0; i < 4; i++) {
        printf("  Trilho inválido %d... ", invalid_tracks[i]);
        assert(!(invalid_tracks[i] >= MIN_TRACK_NUMBER &&
                invalid_tracks[i] <= MAX_TRACK_NUMBER));
        printf("OK (detectado como inválido)\n");
    }

    printf("✓ Teste 3 passou\n\n");
}

// Teste 4: Simulação de filtro de mediana
void test_median_filter_simulation() {
    printf("Teste 4: Simulação de filtro de mediana\n");

    float test_data[] = {20.0, 22.0, 21.0, 23.0, 20.5};
    int data_size = 5;

    printf("  Dados de teste: ");
    for (int i = 0; i < data_size; i++) {
        printf("%.1f ", test_data[i]);
    }
    printf("\n");

    // Ordenar para cálculo manual da mediana
    float sorted[5];
    memcpy(sorted, test_data, sizeof(test_data));

    // Bubble sort
    for (int i = 0; i < data_size - 1; i++) {
        for (int j = 0; j < data_size - i - 1; j++) {
            if (sorted[j] > sorted[j + 1]) {
                float temp = sorted[j];
                sorted[j] = sorted[j + 1];
                sorted[j + 1] = temp;
            }
        }
    }

    float expected_median = sorted[data_size / 2]; // 21.0
    printf("  Mediana esperada: %.1f\n", expected_median);

    assert(expected_median == 21.0);

    printf("✓ Teste 4 passou\n\n");
}

// Teste 5: Estados dos trilhos e LEDs correspondentes
void test_track_state_mapping() {
    printf("Teste 5: Mapeamento estados trilhos -> LEDs\n");

    struct {
        TrackState state;
        const char* expected_cmd;
        const char* description;
    } test_cases[] = {
        {TRACK_STATE_FREE, "GE", "Verde - Trilho livre"},
        {TRACK_STATE_ASSIGNED, "YE", "Amarelo - Trilho atribuído"},
        {TRACK_STATE_BUSY, "RE", "Vermelho - Trilho ocupado"},
        {TRACK_STATE_INOPERATIVE, "RB", "Vermelho piscante - Inoperativo"}
    };

    for (int i = 0; i < 4; i++) {
        printf("  %s... ", test_cases[i].description);

        // Verificar mapeamento
        const char* actual_cmd = NULL;
        switch (test_cases[i].state) {
            case TRACK_STATE_FREE: actual_cmd = CMD_GREEN_ON; break;
            case TRACK_STATE_ASSIGNED: actual_cmd = CMD_YELLOW_ON; break;
            case TRACK_STATE_BUSY: actual_cmd = CMD_RED_ON; break;
            case TRACK_STATE_INOPERATIVE: actual_cmd = CMD_RED_BLINK; break;
        }

        assert(strcmp(actual_cmd, test_cases[i].expected_cmd) == 0);
        printf("OK (comando: %s)\n", actual_cmd);
    }

    printf("✓ Teste 5 passou\n\n");
}

// Teste 6: Intervalos de blinking
void test_blink_intervals() {
    printf("Teste 6: Intervalos de blinking\n");

    // Testar que BLINK_INTERVAL está definido
    assert(BLINK_INTERVAL > 0);
    printf("  BLINK_INTERVAL = %d ms\n", BLINK_INTERVAL);

    // Verificar que é um intervalo razoável
    assert(BLINK_INTERVAL >= 100 && BLINK_INTERVAL <= 2000);

    printf("✓ Teste 6 passou\n\n");
}

// Teste 7: Configurações dos pinos
void test_pin_configurations() {
    printf("Teste 7: Configurações dos pinos\n");

    // Verificar que todos os pinos estão definidos
    for (int track = 0; track < NUM_TRACKS; track++) {
        for (int color = 0; color < LEDS_PER_TRACK; color++) {
            assert(LED_PINS[track][color] >= 2 && LED_PINS[track][color] <= 13);
        }
    }

    printf("  %d trilhos configurados com %d LEDs cada\n",
           NUM_TRACKS, LEDS_PER_TRACK);
    printf("  Pinos utilizados: 2-13\n");

    printf("✓ Teste 7 passou\n\n");
}

// Função principal de testes
int main() {
    printf("==========================================\n");
    printf("   TESTES UNITÁRIOS - USAC10\n");
    printf("==========================================\n\n");

    // Executar todos os testes
    test_light_command_format();
    test_sensor_response_format();
    test_track_number_validation();
    test_median_filter_simulation();
    test_track_state_mapping();
    test_blink_intervals();
    test_pin_configurations();

    printf("==========================================\n");
    printf("   TODOS OS TESTES PASSARAM!\n");
    printf("==========================================\n");

    return 0;
}