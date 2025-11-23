.section .text
.global extract_data

# -------------------------------------------------------------------
# USAC03: extract_data
# a0: char* str, a1: char* token, a2: char* unit, a3: int* value
# Retorna: 1 (sucesso), 0 (falha)
#
# tokens esperados: TEMP ou HUM (ambos com 4 caracteres)
# -------------------------------------------------------------------
extract_data:
    # --- Prologue ---
    addi sp, sp, -24
    sw ra, 20(sp)
    sw s0, 16(sp) # s0 = str pointer (current position)
    sw s1, 12(sp) # s1 = token pointer
    sw s2, 8(sp)  # s2 = unit pointer
    sw s3, 4(sp)  # s3 = value pointer (address)

    mv s0, a0   # s0 = str (current position in the input string)
    mv s1, a1   # s1 = token
    mv s2, a2   # s2 = unit (output string pointer)
    mv s3, a3   # s3 = value (output integer pointer address)

    li a0, 0    # Default return value is 0 (failure)

# -------------------------------------------------------------------
# FASE 1: Loop para procurar o TOKEN
# -------------------------------------------------------------------
search_token_loop:
    # 1. Carrega o caractere atual
    lbu t0, 0(s0)   # t0 = *s0 (current char)

    # Paragem 1: Fim da string, falha
    beq t0, zero, fail_usac03

    # Paragem 2: Se for '#', avança para o próximo bloco
    li t1, '#'
    beq t0, t1, advance_block

    # Tentar match do token (usando a0/a1 para check_token)
    mv a0, s0        # a0 = Ptr para a posição atual (para check_token)
    mv a1, s1        # a1 = Ptr para o token de pesquisa
    call check_token # Chama check_token (retorna a0=1 se match)
    mv t3, a0        # t3 = resultado do check_token

    li t4, 1
    beq t3, t4, token_match # Se t3 == 1 (match), avançar

    # Avançar para o próximo caractere
    addi s0, s0, 1
    j search_token_loop

advance_block:
    # Se encontrou '#', avança para o próximo bloco (depois de '#')
    addi s0, s0, 1
    j search_token_loop

token_match:
    # s0 aponta para o 1º caractere do TOKEN.
    # 1. Avançar s0 pelo TOKEN (comprimento 4)
    addi s0, s0, 4

    # 2. Procurar por "&unit:" (deve ter 5 bytes: '&', 'u', 'n', 'i', 't', ':')
    # O ponteiro s0 está agora em '&'
    addi s0, s0, 6 # s0 aponta para o início da unidade

    # -------------------------------------------------------------------
    # FASE 2: Extrair UNIT
    # -------------------------------------------------------------------
extract_unit_loop:
    lbu t0, 0(s0) # t0 = char da unidade
    li t1, '&'
    beq t0, t1, unit_end # Fim da unidade
    beq t0, zero, fail_usac03 # Fim da string (erro)

    sb t0, 0(s2)    # *s2 = t0 (copia char)
    addi s0, s0, 1  # s0++
    addi s2, s2, 1  # s2++
    j extract_unit_loop

unit_end:
    sb zero, 0(s2)  # Adiciona terminador nulo à unidade

    # -------------------------------------------------------------------
    # FASE 3: Procurar por "value:" e extrair VALUE
    # -------------------------------------------------------------------

    # s0 aponta para 'v' de "value"
    addi s0, s0, 6 # s0 aponta para o início do valor (número), pulando "value:"

    # Conversão de ASCII para inteiro
    li t0, 0 # t0 = valor_convertido (inicializado a 0)

convert_value_loop:
    lbu t1, 0(s0) # t1 = char
    li t2, '#'    # Paragem 1: Fim do bloco
    beq t1, t2, value_end
    beq t1, zero, value_end # Paragem 2: Fim da string

    # Verificar se é um dígito ('0' a '9')
    li t3, '0'
    blt t1, t3, fail_usac03 # Não é dígito
    li t3, '9'
    bgt t1, t3, fail_usac03 # Não é dígito

    # t0 = t0 * 10 + (t1 - '0')
    li t3, 10
    mul t0, t0, t3       # t0 = t0 * 10
    addi t1, t1, -'0'    # t1 = t1 - '0'
    add t0, t0, t1       # t0 = t0 + digito

    addi s0, s0, 1
    j convert_value_loop

value_end:
    # Armazenar o valor (t0) no ponteiro de saída (s3)
    sw t0, 0(s3)

    li a0, 1 # Retorna 1 (sucesso)
    j restore_and_ret_usac03

# -------------------------------------------------------------------
# Sub-rotina: check_token
# Verifica se a substring em a0 (str) é igual ao token em a1 (token)
# Usa os registradores temporários do chamador (a0, a1)
# Retorna em a0: 1 se match, 0 se não match
# -------------------------------------------------------------------
check_token:
    # Salvar registradores temporários antes da chamada
    addi sp, sp, -8
    sw t5, 4(sp)
    sw t6, 0(sp)

    mv t5, a0 # t5 = str (posição atual)
    mv t6, a1 # t6 = token de pesquisa

    li a0, 1 # Assume match

    # Comparar 4 bytes (TOKEN = TEMP ou HUM)
    li t1, 4 # Contador
token_cmp_loop:
    beq t1, zero, token_cmp_end # Se contador = 0, match

    lbu t2, 0(t5) # char da str
    lbu t3, 0(t6) # char do token

    bne t2, t3, token_cmp_fail # Diferentes, não há match

    addi t5, t5, 1
    addi t6, t6, 1
    addi t1, t1, -1
    j token_cmp_loop

token_cmp_fail:
    li a0, 0 # Retorna 0 (não match)

token_cmp_end:
    # Restaurar registradores temporários
    lw t6, 0(sp)
    lw t5, 4(sp)
    addi sp, sp, 8
    ret

fail_usac03:
    # Limpar a saída em caso de falha
    li t0, 0
    sw t0, 0(s3) # *value = 0
    sb zero, 0(s2) # *unit = '\0'

    li a0, 0 # Retorna 0 (falha)

restore_and_ret_usac03:
    # --- Epilogue ---
    lw s3, 4(sp)
    lw s2, 8(sp)
    lw s1, 12(sp)
    lw s0, 16(sp)
    lw ra, 20(sp)
    addi sp, sp, 24
    ret