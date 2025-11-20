#include <stdio.h>
#include "asm.h"

static void print_array(int *a, int n) {
    printf("{");
    for (int i = 0; i < n; ++i) {
        if (i) printf(", ");
        printf("%d", a[i]);
    }
    printf("}\n");
}

int main(void) {
    int asc[] = {2,1,1,1,1};
    int desc[] = {1,1,1,1,2};
    int n = 5;

    printf("Original asc array: "); print_array(asc, n);
    int r1 = sort_array(asc, n, 1);
    printf("sort_array returned: %d\n", r1);
    printf("Sorted ascending: "); print_array(asc, n);

    printf("\nOriginal desc array: "); print_array(desc, n);
    int r2 = sort_array(desc, n, 0);
    printf("sort_array returned: %d\n", r2);
    printf("Sorted descending: "); print_array(desc, n);

    return ((r1 && r2) ? 0 : 1);
}
