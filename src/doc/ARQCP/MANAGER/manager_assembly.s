# ============================================
# manager_process_sensor_data_asm
# Função assembly OBRIGATÓRIA com struct como parâmetro
# ============================================

.section .text
.global manager_process_sensor_data_asm

# manager_process_sensor_data_asm(StationManager* manager, const char* sensor_str)
# a0 = ponteiro para StationManager
# a1 = ponteiro para string do sensor
manager_process_sensor_data_asm:
    # Prologue
    addi sp, sp, -32
    sw ra, 28(sp)
    sw s0, 24(sp)
    sw s1, 20(sp)
    sw s2, 16(sp)

    mv s0, a0           # s0 = manager
    mv s1, a1           # s1 = sensor_str

    # Verifica se pointers são válidos
    beqz s0, error_exit
    beqz s1, error_exit

    # Carrega contador de logs do manager
    lw t0, 64(s0)       # offset de log_count na struct
    li t1, 1000         # MAX_LOGS
    bge t0, t1, error_exit

    # Adiciona log de processamento de sensor
    la a0, sensor_log_msg
    call add_log_entry   # Função helper (implementar separadamente)

    # Processa string do sensor
    mv a0, s1
    la a1, temp_token
    la a2, unit_buffer
    la a3, value_buffer
    call extract_data    # USAC03

    # Se extraiu temperatura com sucesso
    bnez a0, process_temp
    j check_humidity

process_temp:
    # Atualiza temperatura no manager
    lw t2, value_buffer
    fcvt.s.w ft0, t2
    fsw ft0, 80(s0)     # offset de temperature na struct

check_humidity:
    mv a0, s1
    la a1, hum_token
    la a2, unit_buffer
    la a3, value_buffer
    call extract_data

    bnez a0, process_hum
    j exit_success

process_hum:
    # Atualiza humidade no manager
    lw t2, value_buffer
    fcvt.s.w ft0, t2
    fsw ft0, 84(s0)     # offset de humidity na struct

exit_success:
    li a0, 1
    j epilogue

error_exit:
    li a0, 0

epilogue:
    lw ra, 28(sp)
    lw s0, 24(sp)
    lw s1, 20(sp)
    lw s2, 16(sp)
    addi sp, sp, 32
    ret

.section .rodata
temp_token:    .string "TEMP"
hum_token:     .string "HUM"
sensor_log_msg: .string "Sensor data processed"

.section .bss
unit_buffer:   .space 20
value_buffer:  .space 4