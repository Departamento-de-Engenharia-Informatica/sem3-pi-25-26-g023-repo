#include <stdio.h>
#include <string.h>
#include "asm.h"

int main(void) {
    char in[] = "ABZ"; /* example uppercase string */
    int key = 1; /* example key */
    char out[64];

    memset(out, 0, sizeof out);

    int res = encrypt_data(in, key, out);

    printf("encrypt_data returned: %d\n", res);
    printf("input:  '%s'\n", in);
    printf("output: '%s'\n", out);

    return (res ? 0 : 1);
}
