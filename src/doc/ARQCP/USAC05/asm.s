# asm.s - VERSÃO FINAL DEFINITIVA
    .text
    .globl enqueue_value

enqueue_value:
    # a0 = array
    # a1 = length
    # a2 = *nelem
    # a3 = *read
    # a4 = *write
    # a5 = value

    # 1. Carregar nelem atual
    lw t0, 0(a2)          # t0 = *nelem

    # 2. Verificar se está cheio (nelem == length)
    beq t0, a1, buffer_cheio

    # 3. BUFFER NÃO CHEIO - inserir normalmente
    # Carregar write
    lw t1, 0(a4)          # t1 = *write

    # Calcular endereço
    slli t2, t1, 2        # t2 = write * 4
    add t2, a0, t2        # t2 = &array[write]

    # Inserir valor
    sw a5, 0(t2)

    # Atualizar write: (write + 1) % length
    addi t1, t1, 1
    blt t1, a1, no_wrap_w
    li t1, 0              # wrap
no_wrap_w:
    sw t1, 0(a4)          # *write = t1

    # Atualizar nelem: nelem + 1
    addi t0, t0, 1        # nelem++
    sw t0, 0(a2)          # *nelem = t0

    # 4. VERIFICAR: Após inserção, ficou cheio?
    # t0 já tem o novo nelem
    bne t0, a1, not_full_now

    # FICOU CHEIO! Retornar 1
    li a0, 1
    ret

not_full_now:
    # Não ficou cheio, retornar 0
    li a0, 0
    ret

buffer_cheio:
    # 5. BUFFER JÁ ESTAVA CHEIO - fazer overwrite
    # Carregar read
    lw t1, 0(a3)          # t1 = *read

    # Calcular endereço do elemento mais antigo
    slli t2, t1, 2        # t2 = read * 4
    add t2, a0, t2        # t2 = &array[read]

    # Sobrescrever
    sw a5, 0(t2)

    # Atualizar read: (read + 1) % length
    addi t1, t1, 1
    blt t1, a1, no_wrap_r
    li t1, 0              # wrap
no_wrap_r:
    sw t1, 0(a3)          # *read = t1

    # Atualizar write: (write + 1) % length
    lw t1, 0(a4)          # t1 = *write
    addi t1, t1, 1
    blt t1, a1, no_wrap_w2
    li t1, 0              # wrap
no_wrap_w2:
    sw t1, 0(a4)          # *write = t1

    # NELEM MANTÉM-SE (já está no máximo)

    # Retornar 1 (já estava cheio)
    li a0, 1
    ret