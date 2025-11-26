.text
.globl format_command

# int format_command(char* op, int n, char *cmd)
# Formats a command string according to specifications
# a0 = op* (input command string)
# a1 = n (integer parameter)
# a2 = cmd* (output buffer)
# Returns: 1 on success, 0 on failure
format_command:
    # =============================================
    # PROLOGO - SALVAR REGISTOS NA STACK
    # =============================================
    addi sp, sp, -32      # Reserva 32 bytes na stack (8 palavras de 4 bytes)
    sw ra, 28(sp)         # Salva endereço de retorno (ra)
    sw s0, 24(sp)         # Salva s0 - será usado para guardar op pointer
    sw s1, 20(sp)         # Salva s1 - será usado para guardar n value
    sw s2, 16(sp)         # Salva s2 - será usado para guardar cmd buffer
    sw s3, 12(sp)         # Salva s3 - será usado para processamento de string

    # =============================================
    # INICIALIZAÇÃO - GUARDAR PARÂMETROS
    # =============================================
    mv s0, a0      # s0 = op pointer (guarda string de input)
    mv s1, a1      # s1 = n value (guarda número inteiro)
    mv s2, a2      # s2 = cmd output buffer (onde escrever resultado)

    # Inicializa cmd como string vazia (por segurança)
    sb zero, 0(s2)  # Coloca byte NULL no início do buffer de output

    # =============================================
    # VERIFICAÇÃO DE INPUT VAZIO
    # =============================================
    lb t0, 0(s0)        # Carrega primeiro caractere da string op
    beqz t0, format_fail # Se for zero (fim de string), falha imediatamente

    # =============================================
    # SKIP LEADING SPACES - IGNORAR ESPAÇOS INICIAIS
    # =============================================
    mv s3, s0      # s3 = ponteiro atual para processamento (copia de s0)

skip_leading_spaces:
    lb t0, 0(s3)         # Carrega caractere atual
    beqz t0, format_fail # Se fim de string, falha
    li t1, ' '           # Carrega caractere espaço em t1
    beq t0, t1, skip_space # Se for espaço, salta para skip
    li t1, '\t'          # Carrega tab em t1
    beq t0, t1, skip_space # Se for tab, salta para skip
    j process_command    # Se não é espaço nem tab, começa processamento

skip_space:
    addi s3, s3, 1       # Avança ponteiro para próximo caractere
    j skip_leading_spaces # Volta a verificar

    # =============================================
    # PROCESSAMENTO DO COMANDO - CONVERSÃO PARA MAIÚSCULAS
    # =============================================
process_command:
    mv t4, s3      # t4 = ponteiro temporário (começa onde paramos os espaços)
    li t5, 0       # t5 = contador de caracteres (inicializa a 0)

load_and_convert:
    lb t0, 0(t4)               # Carrega caractere atual
    beqz t0, check_loaded_chars # Se fim de string, vai verificar o que carregou

    # Verifica se é espaço/tab (fim do comando)
    li t1, ' '
    beq t0, t1, check_loaded_chars # Espaço termina o comando
    li t1, '\t'
    beq t0, t1, check_loaded_chars # Tab termina o comando

    # =============================================
    # CONVERSÃO PARA MAIÚSCULAS
    # =============================================
    li t1, 'a'     # t1 = 'a' (97 em ASCII)
    li t2, 'z'     # t2 = 'z' (122 em ASCII)
    blt t0, t1, store_char  # Se < 'a', não é minúscula, salta
    bgt t0, t2, store_char  # Se > 'z', não é minúscula, salta
    addi t0, t0, -32        # Converte para maiúscula (diferença ASCII)

store_char:
    # Armazena caractere na stack (buffer temporário)
    addi sp, sp, -1  # Faz espaço na stack para 1 byte
    sb t0, 0(sp)     # Armazena caractere convertido na stack
    addi t5, t5, 1   # Incrementa contador de caracteres
    addi t4, t4, 1   # Avança para próximo caractere na string
    j load_and_convert # Continua loop

    # =============================================
    # VERIFICAÇÃO DOS CARACTERES CARREGADOS
    # =============================================
check_loaded_chars:
    # Agora temos t5 caracteres na stack, todos em maiúsculas
    beqz t5, format_fail_cleanup # Se não carregou caracteres, falha

    # =============================================
    # VERIFICA COMANDO DE 3 CARACTERES (GTH)
    # =============================================
    li t0, 3
    bne t5, t0, check_2char # Se não tem 3 chars, verifica 2 chars

    # Pop 3 caracteres da stack para registos
    lb t3, 0(sp)   # t3 = terceiro caractere
    addi sp, sp, 1  # Liberta espaço na stack
    lb t2, 0(sp)   # t2 = segundo caractere
    addi sp, sp, 1  # Liberta espaço na stack
    lb t1, 0(sp)   # t1 = primeiro caractere
    addi sp, sp, 1  # Liberta espaço na stack

    # Verifica se é "GTH"
    li t0, 'G'
    bne t1, t0, check_2char_after_pop # Primeiro char não é 'G'
    li t0, 'T'
    bne t2, t0, check_2char_after_pop # Segundo char não é 'T'
    li t0, 'H'
    bne t3, t0, check_2char_after_pop # Terceiro char não é 'H'

    # É GTH - armazena no output
    li t0, 'G'
    sb t0, 0(s2)    # Escreve 'G' no buffer
    li t0, 'T'
    sb t0, 1(s2)    # Escreve 'T' no buffer
    li t0, 'H'
    sb t0, 2(s2)    # Escreve 'H' no buffer
    li t0, 0
    sb t0, 3(s2)    # NULL terminator
    li a0, 1        # Return 1 (sucesso)
    j format_exit   # Salta para fim

    # =============================================
    # VERIFICA COMANDOS DE 2 CARACTERES
    # =============================================
check_2char:
    li t0, 2
    bne t5, t0, format_fail_cleanup # Se não tem 2 chars, falha

    # Pop 2 caracteres da stack
    lb t2, 0(sp)   # t2 = segundo caractere
    addi sp, sp, 1  # Liberta stack
    lb t1, 0(sp)   # t1 = primeiro caractere
    addi sp, sp, 1  # Liberta stack

check_2char_after_pop:
    # =============================================
    # VERIFICA TODOS OS COMANDOS VÁLIDOS DE 2 CHARS
    # =============================================

    # Verifica "RE"
    li t0, 'R'
    bne t1, t0, check_ye # Se primeiro não é 'R', verifica YE
    li t0, 'E'
    bne t2, t0, check_ye # Se segundo não é 'E', verifica YE
    j valid_2char        # É "RE"

check_ye:
    # Verifica "YE"
    li t0, 'Y'
    bne t1, t0, check_ge # Se primeiro não é 'Y', verifica GE
    li t0, 'E'
    bne t2, t0, check_ge # Se segundo não é 'E', verifica GE
    j valid_2char        # É "YE"

check_ge:
    # Verifica "GE"
    li t0, 'G'
    bne t1, t0, check_rb # Se primeiro não é 'G', verifica RB
    li t0, 'E'
    bne t2, t0, check_rb # Se segundo não é 'E', verifica RB
    j valid_2char        # É "GE"

check_rb:
    # Verifica "RB"
    li t0, 'R'
    bne t1, t0, format_fail # Se primeiro não é 'R', falha
    li t0, 'B'
    bne t2, t0, format_fail # Se segundo não é 'B', falha
    # É "RB" - continua para valid_2char

    # =============================================
    # COMANDO VÁLIDO - VALIDA PARÂMETRO n
    # =============================================
valid_2char:
    # Verifica se n está no range [0, 99]
    li t0, 0
    blt s1, t0, format_fail # Se n < 0, falha
    li t0, 99
    bgt s1, t0, format_fail # Se n > 99, falha

    # =============================================
    # FORMATA OUTPUT: "CMD,XX"
    # =============================================
    sb t1, 0(s2)    # Escreve primeiro caractere do comando
    sb t2, 1(s2)    # Escreve segundo caractere do comando
    li t0, ','
    sb t0, 2(s2)    # Escreve vírgula separadora

    # Converte n para string de 2 dígitos
    mv a0, s1       # a0 = n value (parâmetro para int_to_2digit)
    addi a1, s2, 3  # a1 = posição no buffer após "CMD,"
    jal int_to_2digit # Chama função helper

    # Adiciona NULL terminator
    addi t0, s2, 5  # t0 = s2 + 5 (fim da string "CMD,XX")
    sb zero, 0(t0)  # Coloca NULL terminator

    li a0, 1        # Return 1 (sucesso)
    j format_exit   # Salta para fim

    # =============================================
    # TRATAMENTO DE ERROS
    # =============================================
format_fail_cleanup:
    # Limpa stack se tivermos pushado caracteres
    beqz t5, format_fail # Se não há caracteres, só falha
cleanup_loop:
    beqz t5, format_fail # Quando contador chega a 0, falha
    addi sp, sp, 1       # Liberta 1 byte da stack
    addi t5, t5, -1      # Decrementa contador
    j cleanup_loop       # Continua loop

format_fail:
    li a0, 0        # Return 0 (falha)
    sb zero, 0(s2)  # Garante que output é string vazia

    # =============================================
    # EPILOGO - RESTAURA REGISTOS E RETORNA
    # =============================================
format_exit:
    # Restaura registos salvos da stack
    lw ra, 28(sp)   # Restaura endereço de retorno
    lw s0, 24(sp)   # Restaura s0
    lw s1, 20(sp)   # Restaura s1
    lw s2, 16(sp)   # Restaura s2
    lw s3, 12(sp)   # Restaura s3
    addi sp, sp, 32 # Liberta espaço da stack
    ret             # Retorna para caller

    # =============================================
    # FUNÇÃO HELPER: CONVERSÃO INT PARA 2 DÍGITOS
    # =============================================
# Helper function: int_to_2digit
# Converts integer (0-99) to 2-digit ASCII string
# a0 = integer value (0-99)
# a1 = output buffer (2 characters)
int_to_2digit:
    li t0, 10          # t0 = 10 (para divisão)
    div t1, a0, t0     # t1 = a0 / 10 (dígito das dezenas)
    rem t2, a0, t0     # t2 = a0 % 10 (dígito das unidades)

    # Converte para ASCII
    addi t1, t1, '0'   # t1 = t1 + '0' (converte para ASCII)
    addi t2, t2, '0'   # t2 = t2 + '0' (converte para ASCII)

    # Armazena dígitos no buffer de output
    sb t1, 0(a1)       # Armazena dígito das dezenas
    sb t2, 1(a1)       # Armazena dígito das unidades
    ret                # Retorna