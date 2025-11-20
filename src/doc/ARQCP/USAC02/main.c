#include <stdio.h>
#include <string.h>
#include "asm.h"

int main(void) {
    char in[] = "BC A"; /* placeholder: tests expect only uppercase A-Z, change as needed */
    /* Use an example encrypted string produced by Caesar with key 1: "BCD" -> decrypt to "ABC" */
    char enc[] = "BCD";
    int key = 1;
    char out[64];

    memset(out, 0, sizeof out);

    int res = decrypt_data(enc, key, out);

    printf("decrypt_data returned: %d\n", res);
    printf("input (enc):  '%s'\n", enc);
    printf("output (dec): '%s'\n", out);

    return (res ? 0 : 1);
}
