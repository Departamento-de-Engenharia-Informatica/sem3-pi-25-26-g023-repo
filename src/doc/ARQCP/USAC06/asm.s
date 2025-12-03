    .text
    .globl dequeue_value
    .globl enqueue_value

# int dequeue_value(int* array, int length, int* nelem, int* read, int* write, int* value)
dequeue_value:
    # a0 = array
    # a1 = length
    # a2 = *nelem
    # a3 = *read
    # a4 = *write
    # a5 = *value

    # Verificar se a fila está vazia (read == write)
    lw t0, 0(a3)          # t0 = *read
    lw t1, 0(a4)          # t1 = *write
    beq t0, t1, empty     # se read == write, fila vazia

    # FILA NÃO VAZIA - remover elemento
    # 1. Ler valor na posição read
    slli t2, t0, 2        # t2 = read * 4
    add t2, a0, t2        # t2 = &array[read]
    lw t3, 0(t2)          # t3 = array[read]

    # 2. Guardar valor no ponteiro fornecido
    sw t3, 0(a5)          # *value = array[read]

    # 3. Atualizar read: (read + 1) % length
    addi t0, t0, 1        # read++
    blt t0, a1, no_wrap_r
    li t0, 0              # read = 0
no_wrap_r:
    sw t0, 0(a3)          # *read = t0

    # 4. Atualizar nelem: nelem - 1
    lw t0, 0(a2)          # t0 = *nelem
    addi t0, t0, -1       # nelem--
    sw t0, 0(a2)          # *nelem = t0

    # 5. Retornar 1 (sucesso)
    li a0, 1
    ret

empty:
    # Fila vazia - retornar 0
    li a0, 0
    ret

# int enqueue_value(int* array, int length, int* nelem, int* read, int* write, int value)
enqueue_value:
    # a0 = array
    # a1 = length
    # a2 = *nelem
    # a3 = *read
    # a4 = *write
    # a5 = value

    # 1. Carregar write atual
    lw t0, 0(a4)          # t0 = *write

    # 2. Inserir valor na posição write
    slli t1, t0, 2        # t1 = write * 4
    add t1, a0, t1        # t1 = &array[write]
    sw a5, 0(t1)          # array[write] = value

    # 3. Atualizar write: (write + 1) % length
    addi t0, t0, 1        # write++
    blt t0, a1, no_wrap_w
    li t0, 0              # write = 0
no_wrap_w:
    sw t0, 0(a4)          # *write = t0

    # 4. Verificar se read == write (buffer cheio)
    lw t1, 0(a3)          # t1 = *read
    bne t0, t1, not_full  # se write != read, não está cheio

    # BUFFER CHEIO - atualizar read também
    addi t1, t1, 1        # read++
    blt t1, a1, no_wrap_r2
    li t1, 0              # read = 0
no_wrap_r2:
    sw t1, 0(a3)          # *read = t1

    # NELEM mantém-se igual
    li a0, 1              # retornar 1
    ret

not_full:
    # Buffer não está cheio - atualizar nelem
    lw t0, 0(a2)          # t0 = *nelem
    addi t0, t0, 1        # nelem++
    sw t0, 0(a2)          # *nelem = t0

    # Verificar se agora ficou cheio
    bne t0, a1, not_full_now

    # Ficou cheio agora - retornar 1
    li a0, 1
    ret

not_full_now:
    # Não está cheio - retornar 0
    li a0, 0
    ret

# Newline no final para evitar warning