.text
.globl update_sensor_buffer

# int update_sensor_buffer(SensorData *data, int temp_value, int hum_value)
update_sensor_buffer:
    # Verificar NULL
    beqz a0, error

    # Prólogo
    addi sp, sp, -32
    sw ra, 28(sp)
    sw s0, 24(sp)  # pointer data
    sw s1, 20(sp)  # temp_value
    sw s2, 16(sp)  # hum_value

    mv s0, a0
    mv s1, a1
    mv s2, a2

    # --- Atualizar Temperatura ---
    lw a0, 0(s0)       # data->temp_buffer (offset 0)
    beqz a0, error_restore

    lw a1, 4(s0)       # data->temp_length (offset 4)
    addi a2, s0, 8     # &data->temp_nelem (offset 8)
    addi a3, s0, 12    # &data->temp_tail  (offset 12)
    addi a4, s0, 16    # &data->temp_head  (offset 16)
    mv a5, s1          # valor temperatura

    call enqueue_value # Chama função externa (Sprint 2)

    sw s1, 40(s0)      # data->temp_current = temp_value (offset 40)

    # --- Atualizar Humidade ---
    lw a0, 20(s0)      # data->hum_buffer (offset 20)
    beqz a0, error_restore

    lw a1, 24(s0)      # data->hum_length (offset 24)
    addi a2, s0, 28    # &data->hum_nelem (offset 28)
    addi a3, s0, 32    # &data->hum_tail  (offset 32)
    addi a4, s0, 36    # &data->hum_head  (offset 36)
    mv a5, s2          # valor humidade

    call enqueue_value # Chama função externa

    sw s2, 44(s0)      # data->hum_current = hum_current (offset 44)

    # Sucesso
    li a0, 1
    j restore

error_restore:
    li a0, 0

restore:
    lw s2, 16(sp)
    lw s1, 20(sp)
    lw s0, 24(sp)
    lw ra, 28(sp)
    addi sp, sp, 32
    ret

error:
    li a0, 0
    ret