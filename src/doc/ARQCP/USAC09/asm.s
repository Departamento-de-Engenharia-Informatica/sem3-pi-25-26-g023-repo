.section .text
.globl median
.globl sort_array

# int median(int* vec, int num, int *me)
# a0 = vec, a1 = num, a2 = me
median:

    # PROLOGO - SALVAR REGISTOS NA STACK

    addi sp, sp, -20      # Reserva 20 bytes na stack (5 registos)
    sw ra, 16(sp)         # Salva endereço de retorno
    sw s0, 12(sp)         # Salva s0 - será usado para vec pointer
    sw s1, 8(sp)          # Salva s1 - será usado para num value
    sw s2, 4(sp)          # Salva s2 - será usado para me pointer
    sw s3, 0(sp)          # Salva s3 - uso temporário

    # VERIFICAÇÃO DE INPUT VÁLIDO

    li t0, 0
    ble a1, t0, median_error  # Se num <= 0, vai para erro

    # GUARDAR PARÂMETROS NOS REGISTOS S

    mv s0, a0             # s0 = vec (array de inteiros)
    mv s1, a1             # s1 = num (tamanho do array)
    mv s2, a2             # s2 = me (ponteiro para resultado)


    # ORDENAR O ARRAY (CHAMADA A sort_array)

    mv a0, s0             # a0 = vec
    mv a1, s1             # a1 = num
    li a2, 1              # a2 = 1 (ordenar por ordem crescente)
    call sort_array        # Chama função de ordenação

    # Verifica se a ordenação foi bem sucedida
    li t0, 1
    bne a0, t0, median_error  # Se sort_array retornou != 1, erro


    # CALCULAR A MEDIANA

    srli t0, s1, 1        # t0 = num / 2 (divisão inteira)
    andi t1, s1, 1        # t1 = num % 2 (verifica se é par ou ímpar)
    bnez t1, median_odd    # Se num é ímpar, salta para median_odd

median_even:
    # CASO PAR: mediana = (vec[num/2 - 1] + vec[num/2]) / 2
    addi t1, t0, -1       # t1 = num/2 - 1
    slli t2, t1, 2        # t2 = (num/2 - 1) * 4 (offset em bytes)
    add t2, s0, t2        # t2 = vec + offset
    lw t3, 0(t2)          # t3 = vec[num/2 - 1]

    slli t4, t0, 2        # t4 = (num/2) * 4 (offset em bytes)
    add t4, s0, t4        # t4 = vec + offset
    lw t5, 0(t4)          # t5 = vec[num/2]

    # Calcula a média dos dois elementos do meio
    add t6, t3, t5        # t6 = vec[num/2 - 1] + vec[num/2]
    srai t3, t6, 1        # t3 = soma / 2 (usa srai para divisão com sinal)

    j median_store        # Salta para armazenar resultado

median_odd:
    # CASO ÍMPAR: mediana = vec[num/2]
    slli t1, t0, 2        # t1 = (num/2) * 4 (offset em bytes)
    add t1, s0, t1        # t1 = vec + offset
    lw t3, 0(t1)          # t3 = vec[num/2] (elemento do meio)

median_store:

    # ARMAZENAR RESULTADO E RETORNAR SUCESSO

    sw t3, 0(s2)          # Armazena mediana no endereço apontado por me
    li a0, 1              # Retorna 1 (sucesso)
    j median_end          # Salta para epílogo

median_error:

    # TRATAMENTO DE ERRO

    li t0, -1
    sw t0, 0(a2)          # Armazena -1 no resultado
    li a0, 0              # Retorna 0 (erro)

median_end:

    # EPILOGO - RESTAURA REGISTOS E RETORNA

    lw ra, 16(sp)         # Restaura endereço de retorno
    lw s0, 12(sp)         # Restaura s0
    lw s1, 8(sp)          # Restaura s1
    lw s2, 4(sp)          # Restaura s2
    lw s3, 0(sp)          # Restaura s3
    addi sp, sp, 20       # Liberta espaço da stack
    ret                   # Retorna para caller


# int sort_array(int* vec, int length, char order)
# a0 = vec, a1 = length, a2 = order (1=asc, 0=desc)
sort_array:

    # PROLOGO - SALVAR REGISTOS NA STACK

    addi sp, sp, -24      # Reserva 24 bytes na stack (6 registos)
    sw ra, 20(sp)         # Salva endereço de retorno
    sw s0, 16(sp)         # Salva s0 - será usado para vec pointer
    sw s1, 12(sp)         # Salva s1 - será usado para length
    sw s2, 8(sp)          # Salva s2 - será usado para order
    sw s3, 4(sp)          # Salva s3 - será i (outer loop)
    sw s4, 0(sp)          # Salva s4 - será j (inner loop)


    # VERIFICAÇÃO DE INPUT VÁLIDO

    li t0, 0
    ble a1, t0, sort_error  # Se length <= 0, vai para erro


    # GUARDAR PARÂMETROS NOS REGISTOS S

    mv s0, a0             # s0 = vec (array de inteiros)
    mv s1, a1             # s1 = length (tamanho do array)
    mv s2, a2             # s2 = order (1=ascendente, 0=descendente)


    # ALGORITMO BUBBLE SORT

    # s3 = i (variável do loop externo) = length - 1
    addi s3, s1, -1       # i = length - 1
    li t0, 0              # t0 = flag de troca (inicialmente 0)

sort_outer:
    # LOOP EXTERNO: controla número de passagens
    blt s3, zero, sort_success  # Se i < 0, array está ordenado
    li s4, 0              # j = 0 (reinicia inner loop)

sort_inner:
    # LOOP INTERNO: compara elementos adjacentes
    bge s4, s3, sort_inner_end  # Se j >= i, termina loop interno


    # CARREGA ELEMENTOS vec[j] E vec[j+1]

    slli t1, s4, 2        # t1 = j * 4 (offset em bytes)
    add t1, s0, t1        # t1 = vec + offset (endereço de vec[j])
    lw t2, 0(t1)          # t2 = vec[j]

    addi t3, s4, 1        # t3 = j + 1
    slli t3, t3, 2        # t3 = (j + 1) * 4 (offset em bytes)
    add t3, s0, t3        # t3 = vec + offset (endereço de vec[j+1])
    lw t4, 0(t3)          # t4 = vec[j+1]


    # DECIDE SE PRECISA TROCAR (SWAP)

    beqz s2, sort_desc     # Se order == 0 (descendente), salta

sort_asc:
    # ORDEM ASCENDENTE: troca se vec[j] > vec[j+1]
    ble t2, t4, sort_no_swap  # Se vec[j] <= vec[j+1], não troca
    j sort_swap            # Senão, salta para trocar

sort_desc:
    # ORDEM DESCENDENTE: troca se vec[j] < vec[j+1]
    bge t2, t4, sort_no_swap  # Se vec[j] >= vec[j+1], não troca

sort_swap:

    # EFETUA A TROCA (SWAP) DOS ELEMENTOS

    sw t4, 0(t1)          # vec[j] = vec[j+1]
    sw t2, 0(t3)          # vec[j+1] = vec[j] original
    li t0, 1              # flag de troca = 1 (houve troca)

sort_no_swap:

    # INCREMENTA LOOP INTERNO

    addi s4, s4, 1        # j = j + 1
    j sort_inner           # Volta para início do loop interno

sort_inner_end:

    # INCREMENTA LOOP EXTERNO

    addi s3, s3, -1       # i = i - 1
    j sort_outer           # Volta para início do loop externo

sort_success:

    # RETORNAR SUCESSO

    li a0, 1              # Retorna 1 (sucesso)
    j sort_end            # Salta para epílogo

sort_error:

    # RETORNAR ERRO

    li a0, 0              # Retorna 0 (erro)

sort_end:

    # EPILOGO - RESTAURA REGISTOS E RETORNA

    lw ra, 20(sp)         # Restaura endereço de retorno
    lw s0, 16(sp)         # Restaura s0
    lw s1, 12(sp)         # Restaura s1
    lw s2, 8(sp)          # Restaura s2
    lw s3, 4(sp)          # Restaura s3
    lw s4, 0(sp)          # Restaura s4
    addi sp, sp, 24       # Liberta espaço da stack
    ret                   # Retorna para caller