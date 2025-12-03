#ifndef UNITY_MINIMAL_H
#define UNITY_MINIMAL_H

#include <string.h>
#include <stdlib.h>

#define UNITY_BEGIN() do { test_count = 0; fail_count = 0; } while(0)
#define UNITY_END() return (fail_count == 0) ? 0 : 1
#define RUN_TEST(test) do { test(); test_count++; } while(0)

#define TEST_ASSERT_EQUAL_INT(expected, actual) \
    if ((expected) != (actual)) { \
        printf("FAIL: %s:%d: Expected %d, got %d\n", __FILE__, __LINE__, (expected), (actual)); \
        fail_count++; \
    } else { \
        printf("PASS: %s\n", #test); \
    }

#define TEST_ASSERT_EQUAL_INT8(expected, actual) TEST_ASSERT_EQUAL_INT(expected, actual)
#define TEST_ASSERT_EQUAL_STRING(expected, actual) \
    if (strcmp((expected), (actual)) != 0) { \
        printf("FAIL: %s:%d: Expected \"%s\", got \"%s\"\n", __FILE__, __LINE__, (expected), (actual)); \
        fail_count++; \
    }

#define TEST_ASSERT_EQUAL_INT_ARRAY(expected, actual, len) \
    for (int i = 0; i < (len); i++) { \
        if ((expected)[i] != (actual)[i]) { \
            printf("FAIL: %s:%d: Array[%d] - Expected %d, got %d\n", __FILE__, __LINE__, i, (expected)[i], (actual)[i]); \
            fail_count++; \
            break; \
        } \
    }

extern int test_count;
extern int fail_count;

#endif