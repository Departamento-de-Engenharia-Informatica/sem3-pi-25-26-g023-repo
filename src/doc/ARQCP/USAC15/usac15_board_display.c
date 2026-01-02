#include "usac15.h"
#include <time.h>
#include <string.h>

// Exibe painel sinóptico formatado
void display_board(const BoardData* data) {
    if (!data) return;

    printf("\n");
    printf("╔══════════════════════════════════════════════╗\n");
    printf("║         PAINEL SINÓPTICO - ESTAÇÃO          ║\n");
    printf("╠══════════════════════════════════════════════╣\n");
    printf("║  Data/Hora: %-30s ║\n", data->timestamp);
    printf("║  Temperatura: %-4.1f°C                         ║\n", data->temperature);
    printf("║  Humidade:    %-4.1f%%                          ║\n", data->humidity);
    printf("║  Trens ativos: %-2d                            ║\n", data->active_trains);
    printf("╠══════════════════════════════════════════════╣\n");
    printf("║              ESTADO DOS TRILHOS              ║\n");
    printf("╠══════════════════════════════════════════════╣\n");

    for (int i = 0; i < 10; i += 2) {
        char status1[20], status2[20];

        // Formata status do trilho
        switch (data->track_status[i]) {
            case 0: strcpy(status1, "● VERDE"); break;
            case 1: strcpy(status1, "● AMARELO"); break;
            case 2: strcpy(status1, "● VERMELHO"); break;
            case 3: strcpy(status1, "✱ INOPERATIVO"); break;
            default: strcpy(status1, "? DESCONHECIDO");
        }

        if (i+1 < 10) {
            switch (data->track_status[i+1]) {
                case 0: strcpy(status2, "● VERDE"); break;
                case 1: strcpy(status2, "● AMARELO"); break;
                case 2: strcpy(status2, "● VERMELHO"); break;
                case 3: strcpy(status2, "✱ INOPERATIVO"); break;
                default: strcpy(status2, "? DESCONHECIDO");
            }

            printf("║  Trilho %02d: %-15s   Trilho %02d: %-15s ║\n",
                   i+1, status1, i+2, status2);
        } else {
            printf("║  Trilho %02d: %-35s ║\n", i+1, status1);
        }
    }

    printf("╚══════════════════════════════════════════════╝\n");
}

// Cria display divertido (como no enunciado)
void create_funny_display(const BoardData* data) {
    if (!data) return;

    printf("\n");
    printf("  ┌────────────────────────────────────────┐\n");
    printf("  │    🚂 ESTAÇÃO FERROVIÁRIA 🚂          │\n");
    printf("  ├────────────────────────────────────────┤\n");
    printf("  │  🌡️  %.1f°C  💧 %.1f%%                │\n",
           data->temperature, data->humidity);
    printf("  │  🕐 %s                    │\n", data->timestamp);
    printf("  │                                        │\n");
    printf("  │  TRILHOS:                              │\n");

    for (int i = 0; i < 10; i++) {
        char symbol[4];
        switch (data->track_status[i]) {
            case 0: strcpy(symbol, "🟢"); break;  // Verde
            case 1: strcpy(symbol, "🟡"); break;  // Amarelo
            case 2: strcpy(symbol, "🔴"); break;  // Vermelho
            case 3: strcpy(symbol, "💥"); break;  // Inoperativo
            default: strcpy(symbol, "❓");
        }

        if (i % 5 == 0) printf("  │   ");
        printf("%s%d ", symbol, i+1);
        if (i % 5 == 4) printf("   │\n");
    }

    if (data->active_trains > 0) {
        printf("  │                                        │\n");
        printf("  │  🚆 Trens ativos: %d                  ", data->active_trains);
        for (int i = 0; i < data->active_trains && i < 5; i++) {
            printf("🚂");
        }
        printf("  │\n");
    }

    printf("  └────────────────────────────────────────┘\n");
}

// Simula envio de dados para o Board component
void send_to_board(const BoardData* data) {
    if (!data) return;

    printf("\n📤 Enviando dados para o Board component...\n");
    printf("----------------------------------------\n");

    // Formata dados para envio
    printf("Dados enviados:\n");
    printf("- Temperatura: %.1f °C\n", data->temperature);
    printf("- Humidade: %.1f %%\n", data->humidity);
    printf("- Timestamp: %s\n", data->timestamp);
    printf("- Trens ativos: %d\n", data->active_trains);

    for (int i = 0; i < 10; i++) {
        printf("- Trilho %d: ", i+1);
        switch (data->track_status[i]) {
            case 0: printf("LIVRE\n"); break;
            case 1: printf("ATRIBUIDO\n"); break;
            case 2: printf("OCUPADO\n"); break;
            case 3: printf("INOPERATIVO\n"); break;
        }
    }

    printf("----------------------------------------\n");
    printf("✅ Dados enviados com sucesso!\n");
}