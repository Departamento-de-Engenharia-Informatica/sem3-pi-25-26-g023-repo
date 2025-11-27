.text


.globl dequeue_value


# int dequeue_value(int* buffer, int length, int *nelem,
#                   int* tail, int* head, int *value)
# a0 = buffer
# a1 = length
# a2 = nelem*
# a3 = tail*
# a4 = head*  (não usado no dequeue)
# a5 = value*
# retorna: 1 se removeu com sucesso, 0 se buffer vazio

dequeue_value:
    addi sp, sp, -32
    # Reserva 32 bytes na stack para guardar registos

    sw ra, 28(sp)
    # Guarda return address

    sw s0, 24(sp)
    sw s1, 20(sp)
    sw s2, 16(sp)
    sw s3, 12(sp)
    # Guarda registos callee-saved s0..s3

    mv s0, a0      # buffer
    # Copia pointer do buffer para s0

    mv s1, a1      # length
    # Guarda o tamanho (número de posições do buffer)

    mv s2, a2      # nelem*
    # Guarda pointer para número de elementos

    mv s3, a3      # tail*
    # Guarda pointer para tail (índice do elemento mais antigo)

    # a4 = head* (não é necessário no dequeue, ignorado)

    mv s4, a5      # value*
    # Guarda pointer onde escrever o valor removido

    # Verificar se buffer está vazio
    lw t0, 0(s2)           # t0 = *nelem
    # Carrega número de elementos; se 0, está vazio

    beq t0, zero, empty_case
    # Se nelem == 0 → saltar para o caso vazio

    # Ler da posição TAIL atual
    lw t1, 0(s3)           # t1 = *tail
    # Carrega índice do elemento mais antigo (tail)

    # Calcular endereço correto
    slli t2, t1, 2         # t2 = tail * 4
    # Multiplica índice por 4 para obter offset em bytes (int = 4 bytes)

    add t2, s0, t2         # t2 = &buffer[tail]
    # Soma endereço base do buffer com offset -> &buffer[tail]

    lw t3, 0(t2)           # t3 = buffer[tail]
    # Lê o valor armazenado nessa posição

    # Escrever valor no ponteiro de saída
    sw t3, 0(s4)           # *value = buffer[tail]
    # Escreve o valor removido para o pointer passado pela função

    # Avançar TAIL circularmente
    addi t1, t1, 1         # tail++
    # Incrementa índice do elemento mais antigo

    blt t1, s1, tail_ok    # if (tail < length) goto tail_ok
    # Se ainda dentro do limite, mantém; caso contrário faz wrap

    li t1, 0               # tail = 0 (wrap around)
    # Se passou do fim, volta a 0

tail_ok:
    sw t1, 0(s3)           # *tail = novo tail
    # Guarda novo tail

    # Decrementar nelem
    lw t0, 0(s2)           # recarregar nelem
    # Recarrega *nelem

    addi t0, t0, -1
    # Decrementa número de elementos no buffer

    sw t0, 0(s2)           # *nelem = nelem - 1
    # Guarda novo número de elementos

    li a0, 1               # return 1 (sucesso)
    # Coloca 1 em a0 → operação bem-sucedida

    j finish
    # Salta para restauração da stack e return

empty_case:
    li a0, 0               # return 0 (buffer vazio)
    # Se nelem era 0 → retorna 0 a indicar falha

finish:
    lw ra, 28(sp)
    # Restaura return address

    lw s0, 24(sp)
    lw s1, 20(sp)
    lw s2, 16(sp)
    lw s3, 12(sp)
    # Restaura registos callee-saved

    addi sp, sp, 32
    # Liberta frame da stack

    ret
    # Retorna ao chamador
