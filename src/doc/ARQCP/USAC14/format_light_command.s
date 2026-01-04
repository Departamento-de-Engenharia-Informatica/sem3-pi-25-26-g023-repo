# ==============================================
# USAC14 - Assembly Implementation
# format_light_command.s
# ==============================================

.section .text
.global format_light_command
.global control_light_asm

# ==============================================
# int format_light_command(char* op, int track_id, char* output)
#
# Formata comando para Arduino: "OP,TT"
# a0 = op string (2 chars: "GE", "YE", "RE", "RB")
# a1 = track_id (1-99)
# a2 = output buffer (min 6 bytes)
#
# Retorna: 1 se sucesso, 0 se erro
# ==============================================

format_light_command:
    # Prologue
    addi sp, sp, -32
    sw ra, 28(sp)
    sw s0, 24(sp)
    sw s1, 20(sp)
    sw s2, 16(sp)
    sw s3, 12(sp)

    mv s0, a0        # s0 = op pointer
    mv s1, a1        # s1 = track_id
    mv s2, a2        # s2 = output buffer

    # ========= VALIDAÇÃO DO TRACK_ID =========
    li t0, 1
    li t1, 99
    blt s1, t0, error_exit   # if track_id < 1
    bgt s1, t1, error_exit   # if track_id > 99

    # ========= VALIDAÇÃO DO OP CODE =========
    # Primeiro caractere deve ser G, Y, R
    lb t0, 0(s0)
    li t1, 'G'
    beq t0, t1, check_second
    li t1, 'Y'
    beq t0, t1, check_second
    li t1, 'R'
    beq t0, t1, check_second
    j error_exit

check_second:
    # Segundo caractere deve ser E ou B
    lb t0, 1(s0)
    li t1, 'E'
    beq t0, t1, valid_op
    li t1, 'B'
    beq t0, t1, valid_op
    j error_exit

valid_op:
    # ========= FORMATAR COMANDO =========
    # Copiar primeiro caractere
    lb t0, 0(s0)
    sb t0, 0(s2)

    # Copiar segundo caractere
    lb t0, 1(s0)
    sb t0, 1(s2)

    # Adicionar vírgula
    li t0, ','
    sb t0, 2(s2)

    # ========= CONVERTER TRACK_ID PARA 2 DÍGITOS =========
    mv a0, s1        # track_id
    mv a1, s2        # output buffer + 3
    addi a1, a1, 3
    call int_to_two_digits_asm

    # Adicionar null terminator
    addi t0, s2, 5
    sb zero, 0(t0)

    # Retornar sucesso
    li a0, 1
    j exit

error_exit:
    li a0, 0

exit:
    # Epilogue
    lw s3, 12(sp)
    lw s2, 16(sp)
    lw s1, 20(sp)
    lw s0, 24(sp)
    lw ra, 28(sp)
    addi sp, sp, 32
    ret

# ==============================================
# void int_to_two_digits_asm(int num, char* output)
#
# Converte número (0-99) para 2 dígitos ASCII
# a0 = número (0-99)
# a1 = buffer de saída (2 bytes)
# ==============================================

int_to_two_digits_asm:
    # Prologue
    addi sp, sp, -16
    sw ra, 12(sp)
    sw s0, 8(sp)

    mv s0, a1        # s0 = output buffer

    # Dividir por 10
    li t0, 10
    div t1, a0, t0   # t1 = dezenas
    rem t2, a0, t0   # t2 = unidades

    # Converter para ASCII
    addi t1, t1, '0'
    addi t2, t2, '0'

    # Armazenar dígitos
    sb t1, 0(s0)     # dígito das dezenas
    sb t2, 1(s0)     # dígito das unidades

    # Epilogue
    lw s0, 8(sp)
    lw ra, 12(sp)
    addi sp, sp, 16
    ret

# ==============================================
# int control_light_asm(int fd, int track_id, int state)
#
# Função assembly que decide qual comando enviar
# a0 = fd (file descriptor)
# a1 = track_id
# a2 = state (0=FREE, 1=ASSIGNED, 2=BUSY, 3=NONOP)
#
# Retorna: comando formatado em a0 (ponteiro para string estática)
# ==============================================

control_light_asm:
    # Prologue
    addi sp, sp, -32
    sw ra, 28(sp)
    sw s0, 24(sp)
    sw s1, 20(sp)
    sw s2, 16(sp)

    mv s0, a0        # s0 = fd
    mv s1, a1        # s1 = track_id
    mv s2, a2        # s2 = state

    # Buffer para comando
    addi a0, sp, 12   # usa espaço na stack para buffer
    mv a1, s2         # state como parâmetro
    call state_to_op_asm

    # a0 agora tem ponteiro para op string
    mv a1, s1         # track_id
    addi a2, sp, 0    # buffer de saída na stack

    call format_light_command_asm

    # Se sucesso, copiar para buffer estático
    beqz a0, control_error

    # Copiar resultado
    la a0, light_command_result
    addi t0, sp, 0
    lb t1, 0(t0)
    sb t1, 0(a0)
    lb t1, 1(t0)
    sb t1, 1(a0)
    lb t1, 2(t0)
    sb t1, 2(a0)
    lb t1, 3(t0)
    sb t1, 3(a0)
    lb t1, 4(t0)
    sb t1, 4(a0)
    sb zero, 5(a0)

    j control_exit

control_error:
    la a0, error_command

control_exit:
    # Epilogue
    lw s2, 16(sp)
    lw s1, 20(sp)
    lw s0, 24(sp)
    lw ra, 28(sp)
    addi sp, sp, 32
    ret

# ==============================================
# char* state_to_op_asm(char* buffer, int state)
#
# Converte estado numérico para string OP
# a0 = buffer de saída (3 bytes)
# a1 = state (0-3)
#
# Retorna: a0 (ponteiro para buffer)
# ==============================================

state_to_op_asm:
    # Mapeamento de estados para códigos
    li t0, 0
    beq a1, t0, state_free
    li t0, 1
    beq a1, t0, state_assigned
    li t0, 2
    beq a1, t0, state_busy
    li t0, 3
    beq a1, t0, state_nonop

    # Estado inválido - default para GE
state_free:
    li t1, 'G'
    sb t1, 0(a0)
    li t1, 'E'
    sb t1, 1(a0)
    sb zero, 2(a0)
    ret

state_assigned:
    li t1, 'Y'
    sb t1, 0(a0)
    li t1, 'E'
    sb t1, 1(a0)
    sb zero, 2(a0)
    ret

state_busy:
    li t1, 'R'
    sb t1, 0(a0)
    li t1, 'E'
    sb t1, 1(a0)
    sb zero, 2(a0)
    ret

state_nonop:
    li t1, 'R'
    sb t1, 0(a0)
    li t1, 'B'
    sb t1, 1(a0)
    sb zero, 2(a0)
    ret

.section .data
light_command_result: .space 6
error_command: .string "ERROR"

.section .rodata
state_names:
    .string "FREE"
    .string "ASSIGNED"
    .string "BUSY"
    .string "NONOP"