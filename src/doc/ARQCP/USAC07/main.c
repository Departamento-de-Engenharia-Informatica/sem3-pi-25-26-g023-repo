#include <stdio.h>
#include "asm.h"

static void print_array(int *a, int n) {
    printf("{");
    for (int i = 0; i < n; i++) {
        if (i) printf(", ");
        printf("%d", a[i]);
    }
    printf("}\n");
}

int main(void) {

    int buffer[6] = {10, 20, 30, 40, 50, 60};
    int buffer_length = 6;
    int num_elements = 4;  // valid elements
    int head = 1;           // oldest element index (20)
    int tail = 5;           // next write index
    int n = 3;              // move 3 elements
    int output_array[6] = {0};

    printf("Initial buffer: "); print_array(buffer, buffer_length);
    printf("head = %d, tail = %d, num_elements = %d\n", head, tail, num_elements);

    int result = move_n_to_array(buffer, buffer_length, &num_elements, &tail, &head, n, output_array);

    printf("\nmove_n_to_array returned: %d\n", result);
    printf("Output array:   "); print_array(output_array, n);

    printf("Buffer after:   ");
    for (int i = 0; i < num_elements; i++) {
        int idx = (head + i) % buffer_length;
        printf("%d ", buffer[idx]);
    }
    printf("\n");

    printf("New head = %d\n", head);
    printf("New tail = %d\n", tail);
    printf("New num_elements = %d\n", num_elements);

    return 0;
}
