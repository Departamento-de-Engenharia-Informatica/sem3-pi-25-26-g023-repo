#include <stdio.h>
#include <string.h> // For memcpy

/*
 * C prototype for the external assembly function.
 */
extern int sort_array(int* vec, int length, char order);

/*
 * Helper function to print an integer array
 */
void print_array(char* title, int* vec, int length) {
    printf("  %s: [", title);
    for (int i = 0; i < length; i++) {
        printf("%d", vec[i]);
        if (i < length - 1) {
            printf(", ");
        }
    }
    printf("]\n");
}

/*
 * Test runner helper function
 */
void run_test(char* id, int* pristine_vec, int length, char order, int expected_ret) {
    int test_vec[20]; // Max test array size
    int actual_ret;

    // Copy the original array into the test array
    // This is crucial because the sort is in-place!
    memcpy(test_vec, pristine_vec, length * sizeof(int));

    printf("--- [Test: %s] ---\n", id);
    printf("  Order:    %s\n", (order == 1) ? "Ascending" : "Descending");
    print_array("Before", test_vec, length);

    // Call the RISC-V assembly function
    actual_ret = sort_array(test_vec, length, order);

    printf("  Expected: Ret=%d\n", expected_ret);
    printf("  Received: Ret=%d\n", actual_ret);
    print_array("After", test_vec, length);

    if (actual_ret == expected_ret) {
        printf("  Status:   PASSED\n\n");
    } else {
        printf("  Status:   FAILED\n\n");
    }
}

int main() {
    int my_array[] = {5, 1, 4, 2, 8};
    int len = 5;

    int empty_array[] = {};
    int len_empty = 0;

    int neg_len_array[] = {};
    int len_neg = -5;

    printf("Running tests for USAC07 (sort_array)...\n\n");

    // Test 1: Ascending order
    run_test("Simple Ascending", my_array, len, 1, 1);

    // Test 2: Descending order
    run_test("Simple Descending", my_array, len, 0, 1);

    // Test 3: Failure (length = 0)
    run_test("Fail (Length 0)", empty_array, len_empty, 1, 0);

    // Test 4: Failure (length < 0)
    run_test("Fail (Length -5)", neg_len_array, len_neg, 1, 0);

    // Test 5: Already sorted (Ascending)
    int sorted_asc[] = {1, 2, 3, 4, 5};
    run_test("Already Sorted (Asc)", sorted_asc, 5, 1, 1);

    // Test 6: Already sorted (Descending)
    int sorted_desc[] = {5, 4, 3, 2, 1};
    run_test("Already Sorted (Desc)", sorted_desc, 5, 0, 1);

    printf("Tests completed.\n");
    return 0;
}