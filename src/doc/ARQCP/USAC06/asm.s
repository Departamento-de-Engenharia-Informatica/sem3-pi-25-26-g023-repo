.text
.globl dequeue_value

# int dequeue_value(int* buffer, int length, int *nelem,
#                   int* tail, int* head, int *value)
#
# a0 = buffer
# a1 = length
# a2 = nelem*
# a3 = tail*    ← elemento mais antigo
# a4 = head*    ← próxima posição de inserção
# a5 = value*
#
# returns: 1 if success, 0 if empty

dequeue_value:
    addi sp, sp, -32
    sw ra, 28(sp)
    sw s0, 24(sp)
    sw s1, 20(sp)
    sw s2, 16(sp)
    sw s3, 12(sp)

    mv s0, a0      # buffer
    mv s1, a1      # length
    mv s2, a2      # nelem*
    mv s3, a3      # tail*
    # a4 = head* (não usado no dequeue)
    mv s4, a5      # value*

    # Verificar se buffer está vazio
    lw t0, 0(s2)           # t0 = *nelem
    beq t0, zero, empty_case

    # Ler da posição TAIL atual
    lw t1, 0(s3)           # t1 = *tail (índice do elemento mais antigo)

    # Calcular endereço correto
    slli t2, t1, 2         # t2 = tail * 4 (offset em bytes)
    add t2, s0, t2         # t2 = &buffer[tail]
    lw t3, 0(t2)           # t3 = buffer[tail] (valor a ser removido)

    # Escrever valor no ponteiro de saída
    sw t3, 0(s4)           # *value = buffer[tail]

    # Avançar TAIL circularmente
    addi t1, t1, 1         # tail++
    blt t1, s1, tail_ok    # if (tail < length) goto tail_ok
    li t1, 0               # tail = 0 (wrap around)
tail_ok:
    sw t1, 0(s3)           # *tail = novo tail

    # Decrementar nelem
    lw t0, 0(s2)           # recarregar nelem
    addi t0, t0, -1
    sw t0, 0(s2)           # *nelem = nelem - 1

    li a0, 1               # return 1 (sucesso)
    j finish

empty_case:
    li a0, 0               # return 0 (buffer vazio)

finish:
    lw ra, 28(sp)
    lw s0, 24(sp)
    lw s1, 20(sp)
    lw s2, 16(sp)
    lw s3, 12(sp)
    addi sp, sp, 32
    ret