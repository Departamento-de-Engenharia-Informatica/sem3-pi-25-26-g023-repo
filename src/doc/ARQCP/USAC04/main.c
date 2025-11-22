#include <stdio.h>
#include <string.h>
#include "asm.h"

int main(void) {
    char cmd[20];
    int res;

    printf("=== Simple Test ===\n");

    // Teste 1: GTH (mais simples)
    memset(cmd, 'X', sizeof(cmd)-1);
    cmd[19] = '\0';
    res = format_command("GTH", 0, cmd);
    printf("GTH test: res=%d, cmd='%s'\n", res, cmd);

    // Teste 2: RB com 5
    memset(cmd, 'X', sizeof(cmd)-1);
    cmd[19] = '\0';
    res = format_command("RB", 5, cmd);
    printf("RB test: res=%d, cmd='%s'\n", res, cmd);

    // Teste 3: Comando inválido
    memset(cmd, 'X', sizeof(cmd)-1);
    cmd[19] = '\0';
    res = format_command("INVALID", 10, cmd);
    printf("Invalid test: res=%d, cmd='%s'\n", res, cmd);

    return 0;
}