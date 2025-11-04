#include <stdio.h>
#include <string.h>

/*
 * C prototype for the external assembly function.
 * This tells the C compiler that the 'encrypt_data' function
 * exists and will be linked in later.
 */
extern int encrypt_data(char* in, int key, char *out);

/*
 * Test runner helper function
 */
void run_test(char* id, char* in, int key, char* expected_out, int expected_ret) {
    char out_buffer[100]; // Buffer for the assembly function to write to
    int actual_ret;

    // Call the RISC-V assembly function
    actual_ret = encrypt_data(in, key, out_buffer);

    printf("--- [Test: %s] ---\n", id);
    printf("  Input:    '%s', Key: %d\n", in, key);
    printf("  Expected: Ret=%d, Out='%s'\n", expected_ret, expected_out);
    printf("  Received: Ret=%d, Out='%s'\n", actual_ret, out_buffer);

    // Check if the actual results match the expected results
    if (actual_ret == expected_ret && strcmp(out_buffer, expected_out) == 0) {
        printf("  Status:   PASSED\n\n");
    } else {
        printf("  Status:   FAILED\n\n");
    }
}

int main() {
    printf("Running tests for USAC01 (encrypt_data)...\n\n");

    // Test 1: Simple success
    run_test("Simple Success", "HELLO", 3, "KHOOR", 1);

    // Test 2: Success with wrap-around (Z -> A)
    run_test("Wrap-around Success", "XYZ", 5, "CDE", 1);

    // Test 3: Failure (Invalid character)
    run_test("Fail (Invalid Char)", "ABC.DEF", 3, "", 0);

    // Test 4: Failure (Key too low)
    run_test("Fail (Key < 1)", "ABC", 0, "", 0);

    // Test 5: Failure (Key too high)
    run_test("Fail (Key > 26)", "ABC", 27, "", 0);

    // Test 6: Success (Key 26, no change)
    run_test("Success (Key 26)", "TEST", 26, "TEST", 1);

    printf("Tests completed.\n");
    return 0;
}