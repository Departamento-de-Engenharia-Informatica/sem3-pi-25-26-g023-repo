#include <string.h>
#include <stdio.h>
#include "asm.h"

int callfunc ( int (*f)(char* str,char* tok, char* unit, int* value),
               char* str,char *tok, char* unit, int* value);

void run_test(char * str, char* tok, int exp_res, char * exp_unit, int exp_value, char* test_name)
{
    int vec[3]={0x55555555,0x55555555,0x55555555};
    char unit[100];
    int  res;
    int passed = 1;

    memset(unit,'@',sizeof(unit));
    res=callfunc(extract_data,str,tok,&unit[1],&vec[1]);

    // Verificar resultado
    if (res != exp_res) {
        printf("FAIL %s: Result - Expected %d, got %d\n", test_name, exp_res, res);
        passed = 0;
    }

    // Verificar sentinelas
    if (vec[2] != 0x55555555 || vec[0] != 0x55555555) {
        printf("FAIL %s: Sentinels corrupted\n", test_name);
        passed = 0;
    }

    // Verificar valor
    if (exp_res == 1 && vec[1] != exp_value) {
        printf("FAIL %s: Value - Expected %d, got %d\n", test_name, exp_value, vec[1]);
        passed = 0;
    }

    // Verificar unit
    if (exp_res == 1 && strcmp(&unit[1], exp_unit) != 0) {
        printf("FAIL %s: Unit - Expected '%s', got '%s'\n", test_name, exp_unit, &unit[1]);
        passed = 0;
    }

    // Verificar sentinelas da unit
    if (unit[0] != '@' || unit[strlen(exp_unit)+2] != '@') {
        printf("FAIL %s: Unit sentinels corrupted\n", test_name);
        passed = 0;
    }

    if (passed) {
        printf("PASS %s\n", test_name);
    }
}

int main()
{
    printf("=== Testing extract_data ===\n");

    run_test("","",0,"",0, "test_Null");
    run_test("TEMP&unit:celsius&value:20#HUM&unit:percentage&value:80","TEMP",1,"celsius",20, "test_One");
    run_test("TEMP&unit:celsius&value:20#HUM&unit:percentage&value:80","HUM",1,"percentage",80, "test_Zero");
    run_test("TEMP&unit:celsius&value:20#HUM&unit:percentage&value:80","LEN",0,"",0, "test_Three");
    run_test("TEMP&unit:celsius&value:20#HUM&unit:percentage&value:80","EMP",0,"",0, "test_Four");
    run_test("TEMP&unit:celsius&value:20#HUM&unit:percentage&value:80","UM",0,"",0, "test_Five");

    printf("=== Tests completed ===\n");
    return 0;
}