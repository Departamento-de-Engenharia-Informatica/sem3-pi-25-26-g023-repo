#include <stdio.h>
#include <string.h>
#include "asm.h"

static void print_state(int *buffer, int length, int nelem, int head, int tail) {
    printf("nelem=%d head=%d tail=%d buffer=[", nelem, head, tail);
    for (int i = 0; i < length; i++) {
        printf("%d", buffer[i]);
        if (i != length-1) printf(" ");
    }
    printf("]\n");
}

int main(void) {
    int length = 5;
    int buffer[5];
    int nelem = 0, head = 0, tail = 0;

    memset(buffer, 0, sizeof(buffer));

    printf("=== Enqueue test ===\n");


    for (int v = 1; v <= 5; v++) {
        int res = enqueue_value(buffer, length, &nelem, &tail, &head, v);
        printf("enqueue(%d) -> res=%d\n", v, res);
        print_state(buffer, length, nelem, head, tail);
    }

    printf("\n=== Overwrite oldest when full ===\n");

    for (int v = 6; v <= 8; v++) {
        int res = enqueue_value(buffer, length, &nelem, &tail, &head, v);
        printf("enqueue(%d) -> res=%d\n", v, res);
        print_state(buffer, length, nelem, head, tail);
    }

    return 0;
}
