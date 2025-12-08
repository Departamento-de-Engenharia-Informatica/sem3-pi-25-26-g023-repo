# sort_array(int* ptr, int num, char order)

# Ordena um array de inteiros usando Bubble Sort.

#  ptr   → ponteiro para o array
#  num   → número de elementos
#  order → 0 = descendente, !=0 = ascendente

# Retorna:
#   0 se num == 0
#   1 se num == 1
#   num se array foi ordenado

    .text
    .globl sort_array


# int sort_array(int* ptr, int num, char order)

sort_array:

    # Se num == 0 → nada a ordenar → retorna 0
    beqz a1, return_zero

    # Se num == 1 → array já está ordenado → retorna 1
    li t0, 1
    beq a1, t0, return_one


    # Preparação da stack e salvar registos

    addi sp, sp, -32
    sw ra, 28(sp)
    sw s0, 24(sp)
    sw s1, 20(sp)
    sw s2, 16(sp)
    sw s3, 12(sp)
    sw s4, 8(sp)

    # Guardar argumentos
    mv s0, a0              # s0 = ptr (array)
    mv s1, a1              # s1 = num (tamanho)
    mv s2, a2              # s2 = order (0=desc, !=0=asc)

    li s3, 0               # s3 = i = 0 (contador do ciclo externo)


#   CICLO EXTERNO DO BUBBLE SORT

outer_loop:
    addi t0, s1, -1        # t0 = num - 1
    bge s3, t0, end_sort   # se i >= num-1 → fim

    li s4, 0               # s4 = j = 0 (contador ciclo interno)

    sub t0, s1, s3         # t0 = num - i
    addi t0, t0, -1        # t0 = num - i - 1 (limite do inner loop)


#   CICLO INTERNO DO BUBBLE SORT

inner_loop:
    bge s4, t0, end_inner  # se j >= num-i-1 → termina ciclo interno

    # Calcular endereço de ptr[j]
    slli t1, s4, 2         # t1 = j * 4 (bytes)
    add t2, s0, t1         # t2 = &ptr[j]
    addi t3, t2, 4         # t3 = &ptr[j+1]

    # Ler valores
    lw t4, 0(t2)           # t4 = ptr[j]
    lw t5, 0(t3)           # t5 = ptr[j+1]


    #      COMPARAÇÃO BASEADA NA ORDEM

    beqz s2, check_desc    # order == 0 → descendente


    # ASCENDENTE → troca se ptr[j] > ptr[j+1]

    ble t4, t5, no_swap    # se ptr[j] <= ptr[j+1], não troca
    j do_swap


# DESCENDENTE → troca se ptr[j] < ptr[j+1]

check_desc:
    bge t4, t5, no_swap    # se ptr[j] >= ptr[j+1], não troca


# TROCAR VALORES

do_swap:
    sw t5, 0(t2)           # ptr[j]   = ptr[j+1]
    sw t4, 0(t3)           # ptr[j+1] = ptr[j]


# Próxima iteração de j

no_swap:
    addi s4, s4, 1         # j++
    j inner_loop


# Final do ciclo interno: adiciona i

end_inner:
    addi s3, s3, 1         # i++
    j outer_loop


# FIM DO SORT → restaurar stack

end_sort:
    lw ra, 28(sp)
    lw s0, 24(sp)
    lw s1, 20(sp)
    lw s2, 16(sp)
    lw s3, 12(sp)
    lw s4, 8(sp)
    addi sp, sp, 32        # liberta stack

    # Retorna num (array ordenado com sucesso)
    mv a0, s1
    ret


# Casos especiais: num == 1 e num == 0

return_one:
    li a0, 1
    ret

return_zero:
    li a0, 0
    ret
