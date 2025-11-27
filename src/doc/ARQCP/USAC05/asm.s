.text

.globl enqueue_value
# torna o símbolo enqueue_value público (linkável)

# int enqueue_value(int* buffer, int length, int *nelem,
#                   int* tail, int* head, int value)
# argumentos em a0..a5, return em a0 (1 se ficar cheio após inserção, 0 caso contrário)

enqueue_value:
    addi sp, sp, -32
    # reserva 32 bytes na stack para guardar registadores salvos/local vars

    sw ra, 28(sp)
    # guarda o return address (ra) em sp+28

    sw s0, 24(sp)
    sw s1, 20(sp)
    sw s2, 16(sp)
    sw s3, 12(sp)
    sw s4, 8(sp)
    # guarda os registos s0..s4 na stack (callee-saved)

    mv s0, a0          # s0 = buffer
    # copia pointer do buffer (a0) para s0 para uso local

    mv s1, a1          # s1 = length
    # copia length (número de slots do buffer) para s1

    mv s2, a2          # s2 = nelem*
    # copia pointer para nelem (número de elementos atuais) para s2

    mv s3, a3          # s3 = tail*
    # copia pointer para tail (índice do elemento mais antigo) para s3

    mv s4, a4          # s4 = head*
    # copia pointer para head (próxima posição de inserção) para s4

    # a5 = value stays in a5
    # valor a inserir permanece em a5

    # load current nelem
    lw t0, 0(s2)       # t0 = *nelem
    # carrega o valor atual de *nelem para t0

    # check if buffer is full
    bne t0, s1, not_full
    # se nelem != length -> não está cheio -> saltar para not_full
    # se nelem == length -> buffer cheio -> continua (entra em full_case)

full_case:
    # Buffer cheio - sobrescrever elemento mais antigo
    # Avançar tail (remove elemento mais antigo)

    lw t1, 0(s3)       # t1 = *tail (índice do mais antigo)
    # carrega índice do tail atual para t1

    # Avançar tail circularmente
    addi t1, t1, 1
    # incrementa o índice do tail

    blt t1, s1, tail_ok_full
    # se t1 < length, continua; caso contrário tem de fazer wrap

    li t1, 0           # wrap around
    # se t1 >= length, volta para 0 (circular)

tail_ok_full:
    sw t1, 0(s3)       # atualizar tail
    # grava o novo valor de tail de volta para *tail

    # nelem permanece o mesmo (substituímos, não adicionamos)
    j insert_value
    # salta para rotina de inserção (não incrementa nelem porque substituiu)

not_full:
    # Buffer não cheio - incrementar nelem
    addi t0, t0, 1
    # incrementa nelem em t0 (porque vamos inserir um novo elemento)

    sw t0, 0(s2)
    # grava o novo *nelem (atualizado)

insert_value:
    # Inserir valor na posição head atual
    lw t2, 0(s4)       # t2 = *head (próxima posição de inserção)
    # carrega índice head atual para t2

    # Calcular endereço: buffer[head]
    slli t3, t2, 2     # offset = head * 4
    # multiplica head por 4 (tamanho de int, assumido 4 bytes) para obter offset em bytes

    add t3, s0, t3     # &buffer[head]
    # soma base do buffer (s0) com offset para obter endereço onde escrever

    sw a5, 0(t3)       # buffer[head] = value
    # escreve o value (a5) no endereço calculado (buffer[head])

    # Avançar head circularmente
    addi t2, t2, 1
    # incrementa índice head

    blt t2, s1, head_ok
    # se head < length continua; senão faz wrap

    li t2, 0           # wrap around
    # se ultrapassou length-1, volta para 0 (circular)

head_ok:
    sw t2, 0(s4)       # atualizar head
    # grava o novo head em *head

    # Verificar se após inserção o buffer está cheio
    lw t0, 0(s2)       # recarregar nelem
    # recarrega *nelem para saber se agora ficou cheio

    bne t0, s1, not_full_after
    # se nelem != length -> não está cheio -> saltar para not_full_after
    # se nelem == length -> está cheio -> seguir para marcar retorno = 1

    li a0, 1           # return 1 (buffer cheio)
    # coloca 1 em a0 (valor de retorno)

    j finish
    # salta para rotina de limpeza e retorno

not_full_after:
    li a0, 0           # return 0 (buffer não cheio)
    # coloca 0 em a0 indicando que não ficou cheio após a inserção

finish:
    lw ra, 28(sp)
    # restaura return address

    lw s0, 24(sp)
    lw s1, 20(sp)
    lw s2, 16(sp)
    lw s3, 12(sp)
    lw s4, 8(sp)
    # restaura registadores s0..s4

    addi sp, sp, 32
    # desaloca stack frame (restitui sp)

    ret
    # retorna ao caller (ra já restaurado)
