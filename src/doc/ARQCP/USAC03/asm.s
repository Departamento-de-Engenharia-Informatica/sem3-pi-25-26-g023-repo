#  extract_data(input, token, unit, value)

#  Procura um token dentro da string "input" (usando find_token),
#  extrai a unidade após "&unit:" e extrai o número após "&value:".
#  Retorna 1 se tudo foi encontrado com sucesso, senão 0.

    .text
    .globl extract_data
    .globl find_token
    .globl to_num

# int extract_data(char* input, char* token, char* unit, int* value)

extract_data:
    addi sp, sp, -32            # Reserva espaço na stack
    sw ra, 28(sp)               # Guarda o return address
    sw s0, 24(sp)               # Guarda registos salvos
    sw s1, 20(sp)
    sw s2, 16(sp)
    sw s3, 12(sp)

    mv s0, a0                   # s0 = input
    mv s1, a1                   # s1 = token
    mv s2, a2                   # s2 = unit (string onde copiar)
    mv s3, a3                   # s3 = value (ponteiro para int)

    sb zero, 0(s2)              # unit[0] = '\0' (limpa)
    sw zero, 0(s3)              # *value = 0   (limpa valor)

    lb t0, 0(s0)                # Verifica se input está vazio
    beqz t0, fail
    lb t0, 0(s1)                # Verifica se token está vazio
    beqz t0, fail

    mv a0, s0                   # Prepara argumentos
    mv a1, s1
    call find_token             # Chama find_token(input, token)

    beqz a0, fail               # Se não encontrou → falha
    mv s0, a0                   # s0 = posição após token

#     Procura "&unit:"

search_unit:
    lb t0, 0(s0)                # Lê caractere atual
    beqz t0, fail               # Se fim da string → falha
    li t1, '&'
    bne t0, t1, next1           # Procura caractere '&'

    # Verifica se segue a substring "unit:"
    lb t0, 1(s0); li t1, 'u'; bne t0, t1, next1
    lb t0, 2(s0); li t1, 'n'; bne t0, t1, next1
    lb t0, 3(s0); li t1, 'i'; bne t0, t1, next1
    lb t0, 4(s0); li t1, 't'; bne t0, t1, next1
    lb t0, 5(s0); li t1, ':'; bne t0, t1, next1

    addi t0, s0, 6              # t0 → início da unidade
    mv t1, s2                   # t1 → buffer unit


# Copiar unidade até encontrar '&'

copy_unit:
    lb t2, 0(t0)                # Lê caractere
    beqz t2, fail               # Se string acabou → erro
    li t3, '&'
    beq t2, t3, unit_done       # Para ao encontrar '&'
    sb t2, 0(t1)                # Copia caractere para unit
    addi t0, t0, 1
    addi t1, t1, 1
    j copy_unit

unit_done:
    sb zero, 0(t1)              # Termina unit com '\0'
    mv s0, t0                   # Avança na string


# Confirmar substring "&value:"

    lb t0, 1(s0); li t1, 'v'; bne t0, t1, fail
    lb t0, 2(s0); li t1, 'a'; bne t0, t1, fail
    lb t0, 3(s0); li t1, 'l'; bne t0, t1, fail
    lb t0, 4(s0); li t1, 'u'; bne t0, t1, fail
    lb t0, 5(s0); li t1, 'e'; bne t0, t1, fail
    lb t0, 6(s0); li t1, ':'; bne t0, t1, fail

    addi a0, s0, 7              # Aponta para número após ":"
    call to_num                 # Converte string → número

    sw a0, 0(s3)                # Guarda em *value
    li a0, 1                    # Sucesso
    j done


# Avança caractere se "&unit:" não apareceu

next1:
    addi s0, s0, 1
    j search_unit


# Falha

fail:
    li a0, 0                    # Retorna 0 (erro)


# Restore stack e return

done:
    lw ra, 28(sp)
    lw s0, 24(sp)
    lw s1, 20(sp)
    lw s2, 16(sp)
    lw s3, 12(sp)
    addi sp, sp, 32
    ret



# char* find_token(char* input, char* token)
#
# Procura "token" em input, mas só se começar após '#'
# ou no início da string. Retorna ponteiro logo após token.

find_token:
    mv t0, a0                   # t0 = input

search_loop:
    lb t1, 0(t0)
    beqz t1, not_found          # Fim → não encontrado
    beq t0, a0, try_match       # No começo → tenta
    lb t2, -1(t0)
    li t3, '#'
    beq t2, t3, try_match       # Após '#' → tenta
    addi t0, t0, 1
    j search_loop

try_match:
    mv t2, a1                   # t2 = token
    mv t3, t0                   # t3 = posição no input

match_loop:
    lb t4, 0(t2)
    beqz t4, check_end          # Token terminou
    lb t5, 0(t3)
    beqz t5, next_token         # String acabou
    bne t4, t5, next_token      # Mismatch
    addi t2, t2, 1
    addi t3, t3, 1
    j match_loop

check_end:
    lb t4, 0(t3)
    beqz t4, found
    li t5, '&'
    beq t4, t5, found
    li t5, '#'
    beq t4, t5, found

next_token:
    addi t0, t0, 1
    j search_loop

found:
    mv a0, t3                   # devolve posição após token
    ret

not_found:
    li a0, 0
    ret



# int to_num(char* str)
#
# Converte uma string num número inteiro (suporta negativos)

to_num:
    li t0, 0                    # acumulador do número
    li t1, 10                   # base decimal
    li t2, 0                    # flag negativo

    lb t3, 0(a0)
    li t4, '-'
    bne t3, t4, convert
    li t2, 1                    # marca negativo
    addi a0, a0, 1              # avança

convert:
    lb t3, 0(a0)
    beqz t3, done_num           # fim da string
    li t4, '0'
    blt t3, t4, done_num
    li t4, '9'
    bgt t3, t4, done_num
    addi t3, t3, -48            # converte char digit → número
    mul t0, t0, t1              # acumulador *= 10
    add t0, t0, t3              # adiciona dígito
    addi a0, a0, 1
    j convert

done_num:
    beqz t2, positive           # se flag=0 → positivo
    neg t0, t0                  # senão torna negativo

positive:
    mv a0, t0                   # retorna número
    ret


# (newline no final)

