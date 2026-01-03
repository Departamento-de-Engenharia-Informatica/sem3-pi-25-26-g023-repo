#include "board.h"
#include <string.h>
#include <unistd.h>

// Inicializa board
void board_init(void) {
    printf("📟 Board Component inicializado\n");
    printf("💡 Pronto para exibir informações\n");
}

// Display normal (formato caixa)
void board_display(const BoardData* data) {
    if (!data) return;

    printf("\n");
    printf("┌────────────────────────────────────────────────┐\n");
    printf("│              PAINEL SINÓPTICO                  │\n");
    printf("├────────────────────────────────────────────────┤\n");

    // Timestamp
    char time_str[30];
    struct tm* tm_info = localtime(&data->timestamp);
    strftime(time_str, 30, "%Y-%m-%d %H:%M:%S", tm_info);
    printf("│  Data: %-36s │\n", time_str);

    // Sensores
    printf("│  🌡️  Temp: %5.1f°C    💧 Hum: %5.1f%%          │\n",
           data->temperature, data->humidity);

    // Mensagem
    printf("│  📢 %-42s │\n", data->message);

    printf("├────────────────────────────────────────────────┤\n");
    printf("│                 ESTADO TRILHOS                 │\n");
    printf("├────────────────────────────────────────────────┤\n");

    // Trilhos em 2 colunas
    for (int i = 0; i < 10; i += 2) {
        char track1[15], track2[15];

        // Formata trilho i
        switch (data->track_status[i]) {
            case 0: sprintf(track1, "T%02d: 🟢", i+1); break;
            case 1: sprintf(track1, "T%02d: 🟡", i+1); break;
            case 2: sprintf(track1, "T%02d: 🔴", i+1); break;
            case 3: sprintf(track1, "T%02d: ⚫", i+1); break;
            default: sprintf(track1, "T%02d: ❓", i+1);
        }

        // Formata trilho i+1 (se existir)
        if (i+1 < 10) {
            switch (data->track_status[i+1]) {
                case 0: sprintf(track2, "T%02d: 🟢", i+2); break;
                case 1: sprintf(track2, "T%02d: 🟡", i+2); break;
                case 2: sprintf(track2, "T%02d: 🔴", i+2); break;
                case 3: sprintf(track2, "T%02d: ⚫", i+2); break;
                default: sprintf(track2, "T%02d: ❓", i+2);
            }
            printf("│  %-15s      %-15s      │\n", track1, track2);
        } else {
            printf("│  %-42s │\n", track1);
        }
    }

    printf("└────────────────────────────────────────────────┘\n");
}

// Display divertido (como no enunciado)
void board_display_funny(const BoardData* data) {
    if (!data) return;

    printf("\n");
    printf("   ╔══════════════════════════════════════╗\n");
    printf("   ║     🚂🚂🚂 ESTAÇÃO FERROVIÁRIA 🚂🚂🚂    ║\n");
    printf("   ╠══════════════════════════════════════╣\n");
    printf("   ║                                      ║\n");

    // Linha sensores
    printf("   ║  🌡️ ");
    if (data->temperature < 10) printf("❄️ ");
    else if (data->temperature > 30) printf("🔥");
    else printf("  ");
    printf(" %4.1f°C  ", data->temperature);

    printf("💧");
    if (data->humidity > 80) printf("💦");
    printf(" %4.1f%%   ║\n", data->humidity);

    // Linha trens
    printf("   ║  ");
    if (data->active_trains > 0) {
        printf("🚆 Trens: %d ", data->active_trains);
        for (int i = 0; i < data->active_trains && i < 3; i++) {
            printf("🚂");
        }
        printf("            ║\n");
    } else {
        printf("📭 Nenhum trem ativo          ║\n");
    }

    printf("   ║                                      ║\n");

    // Trilhos
    printf("   ║  ");
    for (int i = 0; i < 10; i++) {
        if (i == 5) printf("    ║\n   ║  ");

        switch (data->track_status[i]) {
            case 0: printf("🟢"); break;
            case 1: printf("🟡"); break;
            case 2: printf("🔴"); break;
            case 3: printf("⚫"); break;
        }
        printf("%d ", i+1);
    }
    printf("   ║\n");

    // Mensagem
    printf("   ║                                      ║\n");
    printf("   ║  📢 ");
    if (strlen(data->message) > 30) {
        char short_msg[31];
        strncpy(short_msg, data->message, 30);
        short_msg[30] = '\0';
        printf("%-30s", short_msg);
    } else {
        printf("%-30s", data->message);
    }
    printf(" ║\n");

    printf("   ╚══════════════════════════════════════╝\n");

    // Efeito especial
    if (data->active_trains > 2) {
        printf("\n   🚂←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←🚂\n");
    }
}

// Limpa console (simulado)
void board_clear(void) {
    printf("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
    printf("==========================================\n");
    printf("          PAINEL LIMPO                    \n");
    printf("==========================================\n\n");
}

// Encerra board
void board_shutdown(void) {
    printf("\n📴 Board Component a encerrar...\n");
    board_clear();
    printf("✅ Board encerrado\n");
}