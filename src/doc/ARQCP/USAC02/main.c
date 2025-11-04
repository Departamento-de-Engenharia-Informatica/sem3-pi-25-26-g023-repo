#include <stdio.h>
#include <string.h>

/*
 * C prototype for the external assembly function.
 * This tells the C compiler that the 'decrypt_data' function
 * exists and will be linked in later.
 */
extern int decrypt_data(char* in, int key, char *out);

/*
 * Test runner helper function
 */
void run_test(char* id, char* in, int key, char* expected_out, int expected_ret) {
    char out_buffer[100]; // Buffer for the assembly function to write to
    int actual_ret;

    // Call the RISC-V assembly function
    actual_ret = decrypt_data(in, key, out_buffer);

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
    printf("Running tests for USAC02 (decrypt_data)...\n\n");

    // Test 1: Simple success
    run_test("Simple Success", "KHOOR", 3, "HELLO", 1);

    // Test 2: Success with wrap-around (C -> Z)
    run_test("Wrap-around Success", "CDE", 5, "XYZ", 1);

    // Test 3: Failure (Invalid character)
    run_test("Fail (Invalid Char)", "ABC.DEF", 3, "", 0);

    // Test 4: Failure (Key too low)
    run_test("Fail (Key < 1)", "ABC", 0, "", 0);

    // Test 5: Failure (Key too high)
    run_test("Fail (Key > 26)", "ABC", 27, "", 0);

    // Test 6: Success (Key 26, no change)
    run_test("Success (Key 26)", "TEST", 26, "TEST", 1);

    // Test 7: Success (Wrap 'A' with key 1)
    run_test("Success (Wrap 'A')", "A", 1, "Z", 1);

    printf("Tests completed.\n");
    return 0;
}