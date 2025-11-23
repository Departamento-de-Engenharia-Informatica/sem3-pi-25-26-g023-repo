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

    printf("\n1. Dequeue empty buffer:\n");
    int r = dequeue_value(buffer, length, &nelem, &tail, &head, &value);
    printf("res=%d (expected 0)\n", r);

    printf("\n2. Fill buffer with 10,20,30 (tail=0, head=3):\n");
    buffer[0] = 10;
    buffer[1] = 20;
    buffer[2] = 30;
    nelem = 3;
    head = 3;   // próxima posição de inserção
    tail = 0;   // elemento mais antigo está na posição 0
    print_state(buffer, length, nelem, head, tail);

    printf("\n3. Dequeue values (should get 10,20,30):\n");
    for (int i = 0; i < 4; i++) {
        r = dequeue_value(buffer, length, &nelem, &tail, &head, &value);
        printf("dequeue -> res=%d, value=%d", r, value);
        if (r == 1) {
            int expected_values[] = {10, 20, 30};
            printf(" (expected %d)", expected_values[i]);
        }
        printf("\n");
        print_state(buffer, length, nelem, head, tail);
    }

    printf("\n4. Test circular behavior:\n");
    // Reset e preencher com wrap-around
    memset(buffer, 0, sizeof(buffer));
    buffer[3] = 40;
    buffer[4] = 50;
    buffer[0] = 60;  // wrap-around
    nelem = 3;
    head = 1;   // próxima inserção em [1]
    tail = 3;   // elemento mais antigo em [3] (40)
    print_state(buffer, length, nelem, head, tail);

    printf("Dequeue with wrap-around:\n");
    r = dequeue_value(buffer, length, &nelem, &tail, &head, &value);
    printf("dequeue -> res=%d, value=%d (expected 40)\n", r, value);
    print_state(buffer, length, nelem, head, tail);

    r = dequeue_value(buffer, length, &nelem, &tail, &head, &value);
    printf("dequeue -> res=%d, value=%d (expected 50)\n", r, value);
    print_state(buffer, length, nelem, head, tail);

    r = dequeue_value(buffer, length, &nelem, &tail, &head, &value);
    printf("dequeue -> res=%d, value=%d (expected 60)\n", r, value);
    print_state(buffer, length, nelem, head, tail);

    return 0;
}