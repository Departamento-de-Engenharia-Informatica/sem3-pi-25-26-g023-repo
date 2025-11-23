#include <stdio.h>
#include <string.h>
#include "usac08_asm.h"

#define ANSI_GREEN "\x1b[32m"
#define ANSI_RED "\x1b[31m"
#define ANSI_RESET "\x1b[0m"

// Função auxiliar para imprimir arrays
void print_array(int* arr, int len) {
    printf("[");
    for (int i = 0; i < len; i++) {
        printf("%d%s", arr[i], (i < len - 1 ? ", " : ""));
    }
    printf("]");
}

void test_usac08(int* vec, int len, char order, int expected_res, const char* description) {
    // Cria uma cópia para preservar o original (se necessário) e para manipulação
    int vec_copy[len];
    memcpy(vec_copy, vec, len * sizeof(int));

    int result = sort_array(vec_copy, len, order);

    const char* order_str = (order == 1) ? "ASC" : (order == 0 ? "DESC" : "INVALID");

    printf("\n--- Test: %s (%s) ---\n", description, order_str);
    printf("  Input Array: ");
    print_array(vec, len);
    printf("\n");

    int success = (result == expected_res);
    printf("  Result Code: %s%d%s (Expected: %d)\n",
           (success ? ANSI_GREEN : ANSI_RED), result, ANSI_RESET, expected_res);

    if (result == 1) {
        printf("  Sorted Array: ");
        print_array(vec_copy, len);
        printf("\n");
        // Nota: A verificação de conteúdo exata deve ser feita manualmente ou noutra função.
    }
}

int main(void) {
    printf("\n=== USAC08: sort_array (Bubble Sort) ===\n");
    int vec1[] = {5, 2, 8, 1, 9, 4};
    int len1 = 6;
    int vec2[] = {10};
    int len2 = 1;
    int vec3[] = {3, 3, 1, 2};
    int len3 = 4;

    // Teste 1: Ascendente (Order=1)
    test_usac08(vec1, len1, 1, 1, "Array genérico"); // Espera: [1, 2, 4, 5, 8, 9]

    // Teste 2: Descendente (Order=0)
    test_usac08(vec1, len1, 0, 1, "Array genérico"); // Espera: [9, 8, 5, 4, 2, 1]

    // Teste 3: Array de um elemento
    test_usac08(vec2, len2, 1, 1, "Array de 1 elemento"); // Espera: [10]

    // Teste 4: Elementos repetidos
    test_usac08(vec3, len3, 1, 1, "Elementos repetidos"); // Espera: [1, 2, 3, 3]

    // Teste 5: Falha - Comprimento <= 0
    int vec_fail[] = {0};
    test_usac08(vec_fail, 0, 1, 0, "Comprimento zero");

    // Teste 6: Falha - Order inválida (ex: 2)
    test_usac08(vec1, len1, 2, 0, "Order inválida");

    return 0;
}