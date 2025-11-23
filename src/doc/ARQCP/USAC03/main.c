#include <stdio.h>
#include <string.h>
#include "asm.h"

void run_test(char* str, char* tok, int exp_res, char* exp_unit, int exp_value) {
    int value;
    char unit[100];
    int res;

    memset(unit, 0, sizeof(unit));
    value = -999;

    res = extract_data(str, tok, unit, &value);

    printf("Test '%s': ", tok);
    if (res == exp_res) {
        if (exp_res == 1) {
            if (strcmp(unit, exp_unit) == 0 && value == exp_value) {
                printf("✅ PASS\n");
            } else {
                printf("❌ FAIL (unit='%s', value=%d)\n", unit, value);
            }
        } else {
            printf("✅ PASS\n");
        }
    } else {
        printf("❌ FAIL (result=%d)\n", res);
    }
}

int main() {
    printf("=== USAC03 Final Tests ===\n\n");

    // Required tests from professor
    run_test("", "", 0, "", 0);
    run_test("TEMP&unit::celsius&value::20#HUM&unit::percentage&value::80", "TEMP", 1, "celsius", 20);
    run_test("TEMP&unit::celsius&value::20#HUM&unit::percentage&value::80", "HUM", 1, "percentage", 80);
    run_test("TEMP&unit::celsius&value::20#HUM&unit::percentage&value::80", "LEN", 0, "", 0);
    run_test("TEMP&unit::celsius&value::20#HUM&unit::percentage&value::80", "EMP", 0, "", 0);
    run_test("TEMP&unit::celsius&value::20#HUM&unit::percentage&value::80", "UM", 0, "", 0);

    printf("\n=== All tests completed ===\n");
    return 0;
}