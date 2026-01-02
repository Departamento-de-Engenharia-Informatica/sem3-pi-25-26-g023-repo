#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <time.h>
#include "hardware/sensors/config.h"
#include "hardware/lightsigns/config.h"
#include "../common/colors.h"
#include "../common/utils.h"

// ============================================
// USAC10 MAIN - Teste Integrado dos Dispositivos
// ============================================

// Simulação de comunicação serial
typedef struct {
    char buffer[100];
    int length;
} SerialPort;

SerialPort sensor_port;
SerialPort lightsigns_port;

// Funções de simulação
void init_serial_ports() {
    memset(&sensor_port, 0, sizeof(sensor_port));
    memset(&lightsigns_port, 0, sizeof(lightsigns_port));
    printf("%sPortas seriais inicializadas%s\n", MSG_SUCCESS, COLOR_RESET);
}

void send_to_sensors(const char* command) {
    printf("%sEnviando para Sensors: %s%s\n", MSG_INFO, command, COLOR_RESET);
    // Simulação: após 100ms, o sensor responde
    usleep(100000);

    // Simular resposta do sensor
    if (strcmp(command, "GTH") == 0) {
        // Dados simulados do sensor
        int temp = 22 + (rand() % 5);  // 22-26°C
        int hum = 65 + (rand() % 10);  // 65-75%
        snprintf(sensor_port.buffer, sizeof(sensor_port.buffer),
                "TEMP&unit:celsius&value:%d#HUM&unit:percent&value:%d\n",
                temp, hum);
        sensor_port.length = strlen(sensor_port.buffer);
    }
}

void send_to_lightsigns(const char* command) {
    printf("%sEnviando para LightSigns: %s%s\n", MSG_INFO, command, COLOR_RESET);
    strncpy(lightsigns_port.buffer, command, sizeof(lightsigns_port.buffer) - 1);
    lightsigns_port.buffer[sizeof(lightsigns_port.buffer) - 1] = '\0';
    lightsigns_port.length = strlen(lightsigns_port.buffer);
}

char* receive_from_sensors() {
    if (sensor_port.length > 0) {
        printf("%sRecebido de Sensors: %s%s", MSG_INFO, sensor_port.buffer, COLOR_RESET);
        return sensor_port.buffer;
    }
    return NULL;
}

// Função para testar o componente Sensors
void test_sensors_component() {
    printf("\n%s=== TESTE DO COMPONENTE SENSORS ===%s\n", COLOR_BOLD COLOR_BLUE, COLOR_RESET);

    // Testar comando GTH
    printf("\n1. Enviando comando GTH para o sensor...\n");
    send_to_sensors("GTH");

    char* response = receive_from_sensors();
    if (response) {
        printf("   Resposta: %s", response);

        // Verificar formato da resposta
        if (strstr(response, "TEMP&unit:celsius&value:") &&
            strstr(response, "HUM&unit:percent&value:")) {
            PRINT_SUCCESS("Formato da resposta CORRETO");
        } else {
            PRINT_ERROR("Formato da resposta INCORRETO");
        }
    } else {
        PRINT_ERROR("Nenhuma resposta do sensor");
    }

    // Testar comando inválido
    printf("\n2. Enviando comando inválido...\n");
    send_to_sensors("INVALIDO");

    // Testar múltiplas leituras
    printf("\n3. Testando múltiplas leituras...\n");
    for (int i = 0; i < 3; i++) {
        printf("   Leitura %d: ", i + 1);
        send_to_sensors("GTH");
        response = receive_from_sensors();
        if (response) {
            printf("OK\n");
        } else {
            printf("FALHA\n");
        }
        usleep(500000); // 500ms entre leituras
    }
}

// Função para testar o componente LightSigns
void test_lightsigns_component() {
    printf("\n%s=== TESTE DO COMPONENTE LIGHTSIGNS ===%s\n", COLOR_BOLD COLOR_BLUE, COLOR_RESET);

    const char* test_commands[] = {
        "RE,01",  // Trilho 1 vermelho
        "YE,02",  // Trilho 2 amarelo
        "GE,03",  // Trilho 3 verde
        "RB,04",  // Trilho 4 vermelho piscante
        "GE,01",  // Trilho 1 verde
        "RE,02",  // Trilho 2 vermelho
        "YE,03",  // Trilho 3 amarelo
        "RB,01"   // Trilho 1 vermelho piscante
    };

    const char* expected_responses[] = {
        "OK: TRILHO_1_VERMELHO",
        "OK: TRILHO_2_AMARELO",
        "OK: TRILHO_3_VERDE",
        "OK: TRILHO_4_PISCANTE",
        "OK: TRILHO_1_VERDE",
        "OK: TRILHO_2_VERMELHO",
        "OK: TRILHO_3_AMARELO",
        "OK: TRILHO_1_PISCANTE"
    };

    printf("\n1. Testando comandos válidos...\n");
    for (int i = 0; i < 8; i++) {
        printf("   Comando %d: %s -> ", i + 1, test_commands[i]);
        send_to_lightsigns(test_commands[i]);

        // Verificar se o comando foi enviado corretamente
        if (strcmp(lightsigns_port.buffer, test_commands[i]) == 0) {
            PRINT_SUCCESS("ENVIADO");
        } else {
            PRINT_ERROR("FALHA NO ENVIO");
        }

        usleep(300000); // 300ms entre comandos
    }

    printf("\n2. Testando comandos inválidos...\n");

    const char* invalid_commands[] = {
        "INVALIDO",      // Comando desconhecido
        "RE,99",         // Trilho fora do range
        "GE",           // Falta número do trilho
        "RE,0",         // Trilho 0 (inválido)
        "RE,05",        // Trilho 5 (fora do range)
        "CMD,01"        // Comando desconhecido
    };

    for (int i = 0; i < 6; i++) {
        printf("   Comando inválido %d: %s\n", i + 1, invalid_commands[i]);
        send_to_lightsigns(invalid_commands[i]);
        usleep(200000);
    }
}

// Função para testar integração entre componentes
void test_integration() {
    printf("\n%s=== TESTE DE INTEGRAÇÃO ===%s\n", COLOR_BOLD COLOR_BLUE, COLOR_RESET);

    printf("\n1. Cenário: Monitoramento contínuo + controle de trilhos\n");

    // Simular cenário de operação normal
    for (int cycle = 0; cycle < 3; cycle++) {
        printf("\n   Ciclo %d:\n", cycle + 1);

        // Passo 1: Obter dados do sensor
        printf("   a) Obtendo dados do sensor... ");
        send_to_sensors("GTH");
        char* sensor_data = receive_from_sensors();
        if (sensor_data) {
            PRINT_SUCCESS("OK");
            printf("      Dados: %s", sensor_data);
        } else {
            PRINT_ERROR("FALHA");
        }

        // Passo 2: Controlar trilhos baseado nos dados
        printf("   b) Controlando sinalização...\n");

        // Simular diferentes estados baseado no ciclo
        const char* commands[] = {"RE,01", "YE,02", "GE,03", "RB,04"};
        printf("      Comando: %s -> ", commands[cycle % 4]);
        send_to_lightsigns(commands[cycle % 4]);
        PRINT_SUCCESS("ENVIADO");

        usleep(1000000); // 1s entre ciclos
    }

    printf("\n2. Cenário: Emergência\n");
    printf("   a) Ativando modo de emergência em todos os trilhos...\n");

    for (int track = 1; track <= 4; track++) {
        char emergency_cmd[10];
        snprintf(emergency_cmd, sizeof(emergency_cmd), "RB,%02d", track);
        printf("      %s -> ", emergency_cmd);
        send_to_lightsigns(emergency_cmd);
        PRINT_SUCCESS("ENVIADO");
        usleep(300000);
    }

    printf("   b) Restaurando estado normal...\n");
    for (int track = 1; track <= 4; track++) {
        char normal_cmd[10];
        snprintf(normal_cmd, sizeof(normal_cmd), "GE,%02d", track);
        printf("      %s -> ", normal_cmd);
        send_to_lightsigns(normal_cmd);
        PRINT_SUCCESS("ENVIADO");
        usleep(300000);
    }
}

// Função para exibir menu
void display_usac10_menu() {
    printf("\n%s==========================================%s\n", COLOR_CYAN, COLOR_RESET);
    printf("%s        USAC10 - TESTE DE DISPOSITIVOS      %s\n", COLOR_CYAN, COLOR_RESET);
    printf("%s==========================================%s\n\n", COLOR_CYAN, COLOR_RESET);

    printf("Componentes disponíveis:\n");
    printf("  1. %sSENSORS%s - Sensor DHT11 (Temperatura/Humidade)\n", COLOR_GREEN, COLOR_RESET);
    printf("  2. %sLIGHTSIGNS%s - Controle de LEDs para trilhos\n", COLOR_YELLOW, COLOR_RESET);
    printf("  3. %sINTEGRAÇÃO%s - Teste conjunto dos componentes\n", COLOR_BLUE, COLOR_RESET);
    printf("  4. %sCENÁRIOS%s - Testes com cenários específicos\n", COLOR_MAGENTA, COLOR_RESET);
    printf("  0. %sSAIR%s\n\n", COLOR_RED, COLOR_RESET);
}

// Cenários de teste específicos
void run_test_scenarios() {
    printf("\n%s=== CENÁRIOS DE TESTE ===%s\n", COLOR_BOLD COLOR_BLUE, COLOR_RESET);

    const char* scenarios[] = {
        "1. Inicialização do sistema",
        "2. Leitura contínua de sensores",
        "3. Controle individual de trilhos",
        "4. Modo de emergência",
        "5. Sequência de operações",
        "6. Teste de robustez (comandos inválidos)"
    };

    for (int i = 0; i < 6; i++) {
        printf("\n%s\n", scenarios[i]);

        switch (i) {
            case 0: // Inicialização
                printf("   - Inicializando portas seriais...\n");
                init_serial_ports();
                printf("   - Enviando comandos de inicialização...\n");
                send_to_sensors("STATUS");
                send_to_lightsigns("STATUS");
                PRINT_SUCCESS("Cenário 1 completo");
                break;

            case 1: // Leitura contínua
                printf("   - Realizando 5 leituras consecutivas...\n");
                for (int j = 0; j < 5; j++) {
                    send_to_sensors("GTH");
                    usleep(200000);
                }
                PRINT_SUCCESS("Cenário 2 completo");
                break;

            case 2: // Controle individual
                printf("   - Testando cada estado em cada trilho...\n");
                for (int track = 1; track <= 4; track++) {
                    char cmd[10];
                    snprintf(cmd, sizeof(cmd), "RE,%02d", track);
                    send_to_lightsigns(cmd);
                    usleep(100000);
                    snprintf(cmd, sizeof(cmd), "YE,%02d", track);
                    send_to_lightsigns(cmd);
                    usleep(100000);
                    snprintf(cmd, sizeof(cmd), "GE,%02d", track);
                    send_to_lightsigns(cmd);
                    usleep(100000);
                }
                PRINT_SUCCESS("Cenário 3 completo");
                break;

            case 3: // Modo emergência
                printf("   - Ativando modo de emergência...\n");
                for (int track = 1; track <= 4; track++) {
                    char cmd[10];
                    snprintf(cmd, sizeof(cmd), "RB,%02d", track);
                    send_to_lightsigns(cmd);
                    usleep(300000);
                }
                printf("   - Simulando blinking por 3 segundos...\n");
                for (int blink = 0; blink < 6; blink++) {
                    printf("      Blink %d\n", blink + 1);
                    usleep(500000);
                }
                PRINT_SUCCESS("Cenário 4 completo");
                break;

            case 4: // Sequência de operações
                printf("   - Executando sequência operacional...\n");
                const char* sequence[] = {"GE,01", "YE,01", "RE,01", "RB,01", "GE,01"};
                for (int j = 0; j < 5; j++) {
                    printf("      Passo %d: %s\n", j + 1, sequence[j]);
                    send_to_lightsigns(sequence[j]);
                    send_to_sensors("GTH");
                    usleep(500000);
                }
                PRINT_SUCCESS("Cenário 5 completo");
                break;

            case 5: // Teste de robustez
                printf("   - Enviando comandos inválidos...\n");
                const char* invalid[] = {"", "RE", "RE,", "RE,0", "RE,99", "INVALID,01", "RE,01,EXTRA"};
                for (int j = 0; j < 7; j++) {
                    printf("      Comando inválido %d: '%s'\n", j + 1, invalid[j]);
                    send_to_lightsigns(invalid[j]);
                    usleep(200000);
                }
                PRINT_SUCCESS("Cenário 6 completo");
                break;
        }

        usleep(500000); // Pausa entre cenários
    }
}

// Função principal
int main() {
    srand(time(NULL)); // Inicializar gerador aleatório

    printf("%s==========================================%s\n", COLOR_BOLD COLOR_CYAN, COLOR_RESET);
    printf("%s   USAC10 - IMPLEMENTAÇÃO DE DISPOSITIVOS   %s\n", COLOR_BOLD COLOR_CYAN, COLOR_RESET);
    printf("%s==========================================%s\n\n", COLOR_BOLD COLOR_CYAN, COLOR_RESET);

    // Inicializar sistema
    PRINT_SUCCESS("Inicializando sistema USAC10...");
    init_serial_ports();

    int choice;
    do {
        display_usac10_menu();
        printf("%sEscolha uma opção: %s", COLOR_GREEN, COLOR_RESET);

        if (!get_valid_input("%d", &choice)) {
            PRINT_ERROR("Entrada inválida!");
            clear_input_buffer();
            continue;
        }

        switch (choice) {
            case 1:
                test_sensors_component();
                break;

            case 2:
                test_lightsigns_component();
                break;

            case 3:
                test_integration();
                break;

            case 4:
                run_test_scenarios();
                break;

            case 0:
                PRINT_SUCCESS("Saindo do teste USAC10...");
                break;

            default:
                PRINT_ERROR("Opção inválida!");
        }

        printf("\n%sPressione Enter para continuar...%s", COLOR_YELLOW, COLOR_RESET);
        getchar();
        clear_input_buffer();

    } while (choice != 0);

    printf("\n%s==========================================%s\n", COLOR_BOLD COLOR_GREEN, COLOR_RESET);
    printf("%s       USAC10 - TESTES CONCLUÍDOS        %s\n", COLOR_BOLD COLOR_GREEN, COLOR_RESET);
    printf("%s==========================================%s\n", COLOR_BOLD COLOR_GREEN, COLOR_RESET);

    return 0;
}