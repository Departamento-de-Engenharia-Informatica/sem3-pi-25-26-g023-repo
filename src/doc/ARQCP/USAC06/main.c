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
    int value;

    memset(buffer, 0, sizeof(buffer));

    printf("=== Dequeue test ===\n");

    printf("\nDequeue empty buffer:\n");
    int r = dequeue_value(buffer, length, &nelem, &tail, &head, &value);
    printf("res=%d (expected 0)\n", r);

    printf("\nFill buffer with 10,20,30:\n");
    buffer[0] = 10;
    buffer[1] = 20;
    buffer[2] = 30;
    nelem = 3; head = 0; tail = 3;
    print_state(buffer, length, nelem, head, tail);

    printf("\nDequeue values:\n");
    for (int i = 0; i < 4; i++) {
        r = dequeue_value(buffer, length, &nelem, &tail, &head, &value);
        printf("dequeue -> res=%d, value=%d\n", r, value);
        print_state(buffer, length, nelem, head, tail);
    }

    return 0;
}
