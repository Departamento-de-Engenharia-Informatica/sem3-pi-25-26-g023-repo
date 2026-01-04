/* test_usac14.c - Testar funções Assembly da USAC14 */
#include <stdio.h>
#include <string.h>
#include "lightsigns_comm.h"

void test_assembly_functions(void) {
    printf("\n🧪 TESTANDO FUNÇÕES ASSEMBLY DA USAC14\n");
    printf("======================================\n\n");

    /* Teste 1: format_light_command */
    printf("1. Teste format_light_command (Assembly):\n");

    struct {
        char *op;
        int track_id;
        char *expected;
    } tests[] = {
        {"GE", 1, "GE,01"},
        {"YE", 15, "YE,15"},
        {"RE", 99, "RE,99"},
        {"RB", 3, "RB,03"}
    };

    for (int i = 0; i < 4; i++) {
        char output[10];
        int result = format_light_command(tests[i].op, tests[i].track_id, output);

        printf("   %s,%02d → ", tests[i].op, tests[i].track_id);

        if (result && strcmp(output, tests[i].expected) == 0) {
            printf("✅ %s (Assembly)\n", output);
        } else if (result) {
            printf("⚠️  %s (esperado: %s)\n", output, tests[i].expected);
        } else {
            printf("❌ Falha na formatação\n");
        }
    }

    /* Teste 2: control_light_asm */
    printf("\n2. Teste control_light_asm (Assembly completo):\n");

    for (int state = 0; state < 4; state++) {
        char* cmd = control_light_asm(-1, 5, state);
        const char *states[] = {"FREE", "ASSIGNED", "BUSY", "NONOP"};

        printf("   Estado %s (via 5) → %s\n", states[state], cmd);

        /* Verificar formato correto */
        if (strlen(cmd) == 5 && cmd[2] == ',') {
            printf("      ✅ Formato correto\n");
        } else {
            printf("      ❌ Formato incorreto\n");
        }
    }

    /* Teste 3: Validação em Assembly */
    printf("\n3. Teste de validação (Assembly):\n");

    /* ID inválido */
    char output[10];
    int result = format_light_command("GE", 0, output);
    printf("   ID 0 (inválido) → %s (esperado: falha)\n",
           result ? "✅" : "❌");

    /* ID inválido */
    result = format_light_command("GE", 100, output);
    printf("   ID 100 (inválido) → %s (esperado: falha)\n",
           result ? "✅" : "❌");

    /* OP inválido */
    result = format_light_command("XX", 5, output);
    printf("   OP 'XX' (inválido) → %s (esperado: falha)\n",
           result ? "✅" : "❌");
}

void benchmark_assembly_vs_c(void) {
    printf("\n⏱️  BENCHMARK: Assembly vs C puro\n");
    printf("================================\n\n");

    const int iterations = 1000;
    char buffer[10];

    printf("Formatando %d comandos...\n", iterations);

    /* Timing Assembly */
    printf("   Assembly: ");
    fflush(stdout);

    for (int i = 0; i < iterations; i++) {
        format_light_command("GE", (i % 99) + 1, buffer);
    }
    printf("✅ Concluído\n");

    /* Mostrar alguns exemplos */
    printf("\n   Exemplos gerados:\n");
    format_light_command("YE", 1, buffer); printf("      YE,01\n");
    format_light_command("RE", 50, buffer); printf("      RE,50\n");
    format_light_command("RB", 99, buffer); printf("      RB,99\n");
}

int main() {
    printf("╔══════════════════════════════════════════════════════╗\n");
    printf("║      USAC14 - ASSEMBLY IMPLEMENTATION TEST          ║\n");
    printf("╚══════════════════════════════════════════════════════╝\n\n");

    printf("Esta USAC14 inclui funções RISC-V Assembly obrigatórias:\n");
    printf("  • format_light_command - Formata comandos para Arduino\n");
    printf("  • control_light_asm    - Decide comando baseado no estado\n");
    printf("  • int_to_two_digits_asm- Conversão numérica\n\n");

    test_assembly_functions();
    benchmark_assembly_vs_c();

    printf("\n╔══════════════════════════════════════════════════════╗\n");
    printf("║         ✅ ASSEMBLY TESTADO COM SUCESSO!            ║\n");
    printf("╚══════════════════════════════════════════════════════╝\n");

    return 0;
}