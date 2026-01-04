/* lightsigns_comm.c - USAC14 com integração Assembly */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <fcntl.h>
#include <termios.h>
#include "lightsigns_comm.h"

/* ========== DECLARAÇÕES DAS FUNÇÕES ASSEMBLY ========== */

/* Nova função Assembly específica para USAC14 */
extern int format_light_command(char* op, int track_id, char* output);

/* Função Assembly que decide comando baseado no estado */
extern char* control_light_asm(int fd, int track_id, int state);

/* ========== IMPLEMENTAÇÃO EM C QUE USA ASSEMBLY ========== */

const char* state_to_command(TrackState state) {
    switch (state) {
        case TRACK_FREE:    return "GE";
        case TRACK_ASSIGNED: return "YE";
        case TRACK_BUSY:    return "RE";
        case TRACK_NONOP:   return "RB";
        default:            return NULL;
    }
}

/* Versão em C que chama Assembly para formatação */
int format_light_command_c(char* op, int track_id, char* output) {
    /* Chama função Assembly */
    return format_light_command(op, track_id, output);
}

int send_light_command(int fd, const char *cmd) {
    if (fd < 0 || !cmd) {
        printf("[US14-ASM] Modo simulação: %s\n", cmd);
        return 1;
    }

    printf("[US14-ASM] Enviando comando (Assembly): %s\n", cmd);

    char buffer[32];
    int len = snprintf(buffer, sizeof(buffer), "%s\r\n", cmd);

    ssize_t written = write(fd, buffer, len);
    return (written == len) ? 1 : 0;
}

int control_track_light_asm(int fd, int track_id, TrackState state) {
    /* Validação básica em C */
    if (track_id < 1 || track_id > 99) {
        fprintf(stderr, "[US14-ASM] ERRO: ID de via inválido (%d)\n", track_id);
        return 0;
    }

    printf("[US14-ASM] Chamando Assembly: via=%d, estado=%d\n",
           track_id, state);

    /* CHAMA FUNÇÃO ASSEMBLY QUE DECIDE O COMANDO */
    char* cmd = control_light_asm(fd, track_id, (int)state);

    if (!cmd || strcmp(cmd, "ERROR") == 0) {
        fprintf(stderr, "[US14-ASM] ERRO: Assembly retornou erro\n");
        return 0;
    }

    printf("[US14-ASM] Comando gerado: %s\n", cmd);

    /* Enviar comando (parte em C) */
    return send_light_command(fd, cmd);
}

/* Função híbrida: formatação em Assembly, envio em C */
int control_track_light_hybrid(int fd, int track_id, TrackState state) {
    const char *cmd_op = state_to_command(state);
    if (!cmd_op) {
        fprintf(stderr, "[US14] ERRO: Estado inválido (%d)\n", state);
        return 0;
    }

    /* Formatação feita em Assembly */
    char formatted_cmd[20];
    int result = format_light_command((char*)cmd_op, track_id, formatted_cmd);

    if (!result) {
        fprintf(stderr, "[US14] ERRO: Falha na formatação (Assembly)\n");
        return 0;
    }

    printf("[US14-HYBRID] Via %02d → Estado %d → Comando: %s\n",
           track_id, state, formatted_cmd);

    /* Envio feito em C */
    return send_light_command(fd, formatted_cmd);
}

/* Função principal (pode escolher qual versão usar) */
int control_track_light(int fd, int track_id, TrackState state) {
    /* Usa a versão com mais Assembly */
    return control_track_light_asm(fd, track_id, state);
}

int update_all_lights(int fd, const Track *tracks, int num_tracks) {
    if (!tracks || num_tracks <= 0) {
        fprintf(stderr, "[US14] ERRO: Array de vias inválido\n");
        return 0;
    }

    printf("[US14-ASM] Atualizando %d vias usando Assembly...\n", num_tracks);

    int success_count = 0;
    for (int i = 0; i < num_tracks; i++) {
        if (control_track_light(fd, tracks[i].id, tracks[i].state)) {
            success_count++;

            /* Pequeno delay entre comandos */
            if (i < num_tracks - 1) {
                usleep(100000); /* 100ms */
            }
        } else {
            fprintf(stderr, "[US14] AVISO: Falha na via %d\n", tracks[i].id);
        }
    }

    printf("[US14-ASM] ✅ %d/%d vias atualizadas (via Assembly)\n",
           success_count, num_tracks);

    return (success_count == num_tracks) ? 1 : 0;
}