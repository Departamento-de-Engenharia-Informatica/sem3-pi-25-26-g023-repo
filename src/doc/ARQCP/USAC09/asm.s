# asm.s - Implementação das funções median e sort_array para USAC09

.section .text
.globl median
.globl sort_array

# int median(int* vec, int num, int *me)
# a0 = vec, a1 = num, a2 = me

median:
    addi sp, sp, -20
    sw ra, 16(sp)
    sw s0, 12(sp)
    sw s1, 8(sp)
    sw s2, 4(sp)
    sw s3, 0(sp)

    li t0, 0
    ble a1, t0, median_error

    mv s0, a0
    mv s1, a1
    mv s2, a2

    mv a0, s0
    mv a1, s1
    li a2, 1
    call sort_array

    li t0, 1
    bne a0, t0, median_error

    srli t0, s1, 1
    andi t1, s1, 1
    bnez t1, median_odd

median_even:
    addi t1, t0, -1
    slli t2, t1, 2
    add t2, s0, t2
    lw t3, 0(t2)        # t3 = vec[num/2 - 1]
    slli t4, t0, 2
    add t4, s0, t4
    lw t5, 0(t4)        # t5 = vec[num/2]

    # srai em vez de srli para preservar o sinal
    add t6, t3, t5      # Soma
    srai t3, t6, 1      # Divide por 2 preservando sinal (srai)

    j median_store

median_odd:
    slli t1, t0, 2
    add t1, s0, t1
    lw t3, 0(t1)

median_store:
    sw t3, 0(s2)
    li a0, 1
    j median_end

median_error:
    li t0, -1
    sw t0, 0(a2)
    li a0, 0

median_end:
    lw ra, 16(sp)
    lw s0, 12(sp)
    lw s1, 8(sp)
    lw s2, 4(sp)
    lw s3, 0(sp)
    addi sp, sp, 20
    ret

# int sort_array(int* vec, int length, char order)
# a0 = vec, a1 = length, a2 = order (1=asc, 0=desc)
sort_array:
    addi sp, sp, -24
    sw ra, 20(sp)
    sw s0, 16(sp)
    sw s1, 12(sp)
    sw s2, 8(sp)
    sw s3, 4(sp)
    sw s4, 0(sp)

    li t0, 0
    ble a1, t0, sort_error

    mv s0, a0
    mv s1, a1
    mv s2, a2

    addi s3, s1, -1
    li t0, 0

sort_outer:
    blt s3, zero, sort_success
    li s4, 0

sort_inner:
    bge s4, s3, sort_inner_end

    slli t1, s4, 2
    add t1, s0, t1
    lw t2, 0(t1)        # t2 = vec[j]

    addi t3, s4, 1
    slli t3, t3, 2
    add t3, s0, t3
    lw t4, 0(t3)        # t4 = vec[j+1]

    beqz s2, sort_desc

sort_asc:
    ble t2, t4, sort_no_swap
    j sort_swap

sort_desc:
    bge t2, t4, sort_no_swap

sort_swap:
    sw t4, 0(t1)
    sw t2, 0(t3)
    li t0, 1

sort_no_swap:
    addi s4, s4, 1
    j sort_inner

sort_inner_end:
    addi s3, s3, -1
    j sort_outer

sort_success:
    li a0, 1
    j sort_end

sort_error:
    li a0, 0

sort_end:
    lw ra, 20(sp)
    lw s0, 16(sp)
    lw s1, 12(sp)
    lw s2, 8(sp)
    lw s3, 4(sp)
    lw s4, 0(sp)
    addi sp, sp, 24
    ret

