#ifndef USAC03_ASM_H
#define USAC03_ASM_H

// USAC03: Extrai a unidade e o valor de uma string formatada de sensor.
// Retorna 1 se o token for encontrado, 0 caso contrário.
int extract_data(char* str, char* token, char* unit, int* value);

#endif