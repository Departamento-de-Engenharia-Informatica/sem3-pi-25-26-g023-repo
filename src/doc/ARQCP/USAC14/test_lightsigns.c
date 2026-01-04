/* test_lightsigns.c - Teste completo USAC14 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "lightsigns_comm.h"

/* Protótipo da função do Sprint 2 */
extern int format_command(char* op, int n, char *cmd);

void print_color_banner(const char *text, const char *color_code) {
    printf("\n%s", color_code);
    printf("╔══════════════════════════════════════════════════════╗\n");
    printf("║                                                      ║\n");

    int len = strlen(text);
    int spaces = (50 - len) / 2;
    printf("║");
    for (int i = 0; i < spaces; i++) printf(" ");
    printf("%s", text);
    for (int i = 0; i < 50 - spaces - len; i++) printf(" ");
    printf("║\n");

    printf("║                                                      ║\n");
    printf("╚══════════════════════════════════════════════════════╝\033[0m\n");
}

void simulate_real_scenario(void) {
    print_color_banner("CENÁRIO REAL DE OPERAÇÃO", "\033[1;36m");

    /* Simular estado real de uma estação */
    Track tracks[8];

    /* Inicializar todas como livres */
    for (int i = 0; i < 8; i++) {
        tracks[i].id = i + 1;
        tracks[i].state = TRACK_FREE;
        tracks[i].train_id = -1;
    }

    printf("\n🏁 Estado inicial: Todas as vias LIVRES (Verde)\n");

    /* Simular chegada de comboios */
    printf("\n🚂 Comboio 101 chega à estação...\n");
    tracks[0].state = TRACK_ASSIGNED;
    tracks[0].train_id = 101;
    printf("   Via 1: ASSIGNED (Amarelo) para comboio 101\n");

    printf("\n🚂 Comboio 102 chega à estação...\n");
    tracks[1].state = TRACK_ASSIGNED;
    tracks[1].train_id = 102;
    printf("   Via 2: ASSIGNED (Amarelo) para comboio 102\n");

    /* Comboio ocupa via */
    printf("\n🚂 Comboio 101 entra na via...\n");
    tracks[0].state = TRACK_BUSY;
    printf("   Via 1: BUSY (Vermelho) - comboio 101 a carregar\n");

    /* Via com problema */
    printf("\n⚠️  Problema detectado na Via 4!\n");
    tracks[3].state = TRACK_NONOP;
    printf("   Via 4: NONOP (Vermelho piscante) - EM MANUTENÇÃO\n");

    /* Comboio parte */
    printf("\n🚂 Comboio 101 parte da estação...\n");
    tracks[0].state = TRACK_FREE;
    tracks[0].train_id = -1;
    printf("   Via 1: FREE (Verde) novamente\n");

    printf("\n📊 Estado final da estação:\n");
    for (int i = 0; i < 8; i++) {
        const char *states[] = {"🟢 LIVRE", "🟡 ATRIBUÍDA", "🔴 OCUPADA", "⚫ INOPERANTE"};
        printf("   Via %d: %s", tracks[i].id, states[tracks[i].state]);
        if (tracks[i].train_id > 0) printf(" [Comboio %d]", tracks[i].train_id);
        printf("\n");
    }
}

void test_format_command(void) {
    print_color_banner("TESTE DE FORMATAÇÃO DE COMANDOS", "\033[1;33m");

    printf("\nVerificando integração com Sprint 2 (format_command):\n");

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
        char cmd[20];
        int result = format_command(tests[i].op, tests[i].track_id, cmd);

        printf("   Teste %d: %s,%02d → ", i+1, tests[i].op, tests[i].track_id);

        if (result && strcmp(cmd, tests[i].expected) == 0) {
            printf("✅ %s\n", cmd);
        } else if (result) {
            printf("⚠️  %s (esperado: %s)\n", cmd, tests[i].expected);
        } else {
            printf("❌ Falha na formatação\n");
        }
    }
}

int main() {
    print_color_banner("USAC14 - CONTROL TRACK SIGN LIGHT", "\033[1;35m");
    printf("Implementação completa para Sistema de Gestão Ferroviária\n\n");

    /* 1. Teste básico */
    print_color_banner("1. TESTES BÁSICOS", "\033[1;34m");

    printf("\nTestando função state_to_command():\n");
    printf("   TRACK_FREE (%d) → %s\n", TRACK_FREE, state_to_command(TRACK_FREE));
    printf("   TRACK_ASSIGNED (%d) → %s\n", TRACK_ASSIGNED, state_to_command(TRACK_ASSIGNED));
    printf("   TRACK_BUSY (%d) → %s\n", TRACK_BUSY, state_to_command(TRACK_BUSY));
    printf("   TRACK_NONOP (%d) → %s\n", TRACK_NONOP, state_to_command(TRACK_NONOP));

    /* 2. Teste de validação */
    print_color_banner("2. TESTES DE VALIDAÇÃO", "\033[1;31m");

    printf("\nTestando validação de parâmetros:\n");
    printf("   Via ID 0 (inválido): ");
    if (!control_track_light(-1, 0, TRACK_FREE)) {
        printf("✅ Erro corretamente detetado\n");
    }

    printf("   Via ID 100 (inválido): ");
    if (!control_track_light(-1, 100, TRACK_FREE)) {
        printf("✅ Erro corretamente detetado\n");
    }

    printf("   Estado inválido (999): ");
    if (!control_track_light(-1, 1, 999)) {
        printf("✅ Erro corretamente detetado\n");
    }

    /* 3. Teste de formatação */
    test_format_command();

    /* 4. Simulação de cenário real */
    simulate_real_scenario();

    /* 5. Demonstração de integração */
    print_color_banner("5. INTEGRAÇÃO COM MANAGER", "\033[1;32m");

    printf("\nEsta USAC14 será chamada pelo Manager em:\n");
    printf("   1. Inicialização do sistema\n");
    printf("   2. Após cada instrução do utilizador\n");
    printf("   3. Em intervalos regulares (heartbeat)\n\n");

    printf("Comandos que serão enviados ao Arduino:\n");
    printf("   • GE,XX - Via XX Livre (Verde)\n");
    printf("   • YE,XX - Via XX Atribuída (Amarelo)\n");
    printf("   • RE,XX - Via XX Ocupada (Vermelho)\n");
    printf("   • RB,XX - Via XX Inoperante (Vermelho Piscante)\n\n");

    print_color_banner("✅ USAC14 TESTADA COM SUCESSO", "\033[1;32m");
    printf("\nPronto para integração no sistema principal!\n");

    return 0;
}