#include <stdio.h>
#include <string.h>
#include "usac03_asm.h"

// Função auxiliar para imprimir status
void print_status(int result, char* token, char* unit, int value, const char* expected_unit, int expected_value) {
    if (result == 1) {
        printf("  SUCCESS [%s]: Unit='%s' (Exp: '%s'), Value=%d (Exp: %d)\n",
               token, unit, expected_unit, value, expected_value);
    } else {
        printf("  FAILURE [%s]: Result=%d (Unit='%s', Value=%d)\n",
               token, result, unit, value);
    }
}


int main(void) {
    printf("\n=== USAC03: extract_data (Sensor Parsing Simples) ===\n");
    char str_block[] = "TEMP&unit:celsius&value:20#HUM&unit:percentage&value:80";
    char unit[20];
    int value;

    printf("\n--- Testes de Sucesso ---\n");

    // Teste 1: Extrair TEMP (Valor 20, Unidade celsius)
    memset(unit, 0, sizeof unit);
    int res1 = extract_data(str_block, "TEMP", unit, &value);
    print_status(res1, "TEMP", unit, value, "celsius", 20);

    // Teste 2: Extrair HUM (Valor 80, Unidade percentage)
    memset(unit, 0, sizeof unit);
    int res2 = extract_data(str_block, "HUM", unit, &value);
    print_status(res2, "HUM", unit, value, "percentage", 80);

    // Teste 3: Extrair de string simples
    char str_single[] = "HUM&unit:g/l&value:15";
    memset(unit, 0, sizeof unit);
    int res3 = extract_data(str_single, "HUM", unit, &value);
    print_status(res3, "HUM", unit, value, "g/l", 15);


    printf("\n--- Testes de Falha ---\n");

    // Teste 4: Token Inexistente (Deve retornar 0)
    memset(unit, 0, sizeof unit);
    int res4 = extract_data(str_block, "AAAA", unit, &value);
    printf("  FAILURE [AAAA]: Result=%d (Esperado: 0)\n", res4);

    // Teste 5: Valor não numérico
    memset(unit, 0, sizeof unit);
    int res5 = extract_data("TEMP&unit:celsius&value:A", "TEMP", unit, &value);
    printf("  FAILURE [TEMP]: Result=%d (Esperado: 0 - Valor Inválido)\n", res5);


    return 0;
}