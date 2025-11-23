#include <stdio.h>
#include <string.h>
#include "asm.h"

/**
 * Run a single test case for format_command function
 * @param input: Input command string
 * @param n: Integer parameter
 * @param exp_res: Expected return value (1 success, 0 failure)
 * @param exp_cmd: Expected output string (if success)
 */
void run_test(char *input, int n, int exp_res, char *exp_cmd) {
    int res;
    char cmd_out[100];

    // Initialize output buffer with sentinel values
    memset(cmd_out, '@', sizeof(cmd_out));

    // Call the format_command function
    res = format_command(input, n, &cmd_out[1]);

    // Print test result
    printf("Input: '%s', n: %d -> res: %d, cmd: '%s'", input, n, res, &cmd_out[1]);

    // Check if result matches expected
    if (res == exp_res) {
        if (exp_res == 1 && strcmp(&cmd_out[1], exp_cmd) == 0) {
            printf(" ✅ PASS\n");
        } else if (exp_res == 0) {
            printf(" ✅ PASS\n");
        } else {
            printf(" ❌ FAIL - Expected: '%s'\n", exp_cmd);
        }
    } else {
        printf(" ❌ FAIL - Expected res: %d\n", exp_res);
    }
}

/**
 * Main function to test USAC04 format_command implementation
 * Tests various command formats and edge cases
 */
int main(void) {
    printf("=== USAC04 Format Command Tests ===\n\n");

    // Test basic command formats
    run_test("rb", 5, 1, "RB,05");
    run_test("Ye", 25, 1, "YE,25");
    run_test("gTh", 25, 1, "GTH");
    run_test("Ge", 0, 1, "GE,00");
    run_test("Ge", 99, 1, "GE,99");

    // Test commands with whitespace
    run_test("  re  ", 7, 1, "RE,07");
    run_test("Rb      ", 15, 1, "RB,15");

    // Test failure cases
    run_test("Ye", 125, 0, "");      // n out of range
    run_test("aaa", 25, 0, "");      // invalid command
    run_test(" cmD ", 11, 0, "");    // invalid command
    run_test("", 0, 0, "");          // empty string
    run_test("Off", 7, 0, "");       // invalid command

    // Test edge cases
    run_test("RE", 0, 1, "RE,00");
    run_test("YE", 99, 1, "YE,99");
    run_test("GE", 50, 1, "GE,50");
    run_test("RB", 9, 1, "RB,09");

    printf("\n=== Testing completed ===\n");

    return 0;
}