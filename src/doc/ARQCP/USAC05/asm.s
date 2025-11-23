.text
.globl enqueue_value

# int enqueue_value(int* buffer, int length, int *nelem,
#                   int* tail, int* head, int value)
#
# a0 = buffer
# a1 = length
# a2 = nelem (ptr)
# a3 = tail  (ptr)  -> aponta para o elemento mais antigo
# a4 = head  (ptr)  -> aponta para próxima posição de inserção
# a5 = value
#
# returns a0 = 1 if buffer full after insertion, 0 otherwise

enqueue_value:
    addi sp, sp, -32
    sw ra, 28(sp)
    sw s0, 24(sp)
    sw s1, 20(sp)
    sw s2, 16(sp)
    sw s3, 12(sp)
    sw s4, 8(sp)

    mv s0, a0          # s0 = buffer
    mv s1, a1          # s1 = length
    mv s2, a2          # s2 = nelem*
    mv s3, a3          # s3 = tail*
    mv s4, a4          # s4 = head*
    # a5 = value stays in a5

    # load current nelem
    lw t0, 0(s2)       # t0 = *nelem

    # check if buffer is full
    bne t0, s1, not_full

full_case:
    # Buffer cheio - sobrescrever elemento mais antigo
    # Avançar tail (remove elemento mais antigo)
    lw t1, 0(s3)       # t1 = *tail (índice do mais antigo)

    # Avançar tail circularmente
    addi t1, t1, 1
    blt t1, s1, tail_ok_full
    li t1, 0           # wrap around
tail_ok_full:
    sw t1, 0(s3)       # atualizar tail

    # nelem permanece o mesmo (substituímos, não adicionamos)
    j insert_value

not_full:
    # Buffer não cheio - incrementar nelem
    addi t0, t0, 1
    sw t0, 0(s2)

insert_value:
    # Inserir valor na posição head atual
    lw t2, 0(s4)       # t2 = *head (próxima posição de inserção)

    # Calcular endereço: buffer[head]
    slli t3, t2, 2     # offset = head * 4
    add t3, s0, t3     # &buffer[head]
    sw a5, 0(t3)       # buffer[head] = value

    # Avançar head circularmente
    addi t2, t2, 1
    blt t2, s1, head_ok
    li t2, 0           # wrap around
head_ok:
    sw t2, 0(s4)       # atualizar head

    # Verificar se após inserção o buffer está cheio
    lw t0, 0(s2)       # recarregar nelem
    bne t0, s1, not_full_after
    li a0, 1           # return 1 (buffer cheio)
    j finish

not_full_after:
    li a0, 0           # return 0 (buffer não cheio)

finish:
    lw ra, 28(sp)
    lw s0, 24(sp)
    lw s1, 20(sp)
    lw s2, 16(sp)
    lw s3, 12(sp)
    lw s4, 8(sp)
    addi sp, sp, 32
    ret