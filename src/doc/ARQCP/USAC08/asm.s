.section .text
.global sort_array

# -------------------------------------------------------------------
# USAC08: sort_array (Bubble Sort)
# a0: int* vec, a1: int length, a2: char order
# Retorna: 1 (sucesso), 0 (falha)
#
# order = 1 (ascendente), 0 (descendente)
# -------------------------------------------------------------------
sort_array:
    # --- Prologue ---
    addi sp, sp, -28
    sw ra, 24(sp)
    sw s0, 20(sp) # s0 = vec (array pointer)
    sw s1, 16(sp) # s1 = length
    sw s2, 12(sp) # s2 = order (1: ASC, 0: DESC)
    sw s3, 8(sp)  # s3 = i (outer loop counter)
    sw s4, 4(sp)  # s4 = j (inner loop counter)
    sw s5, 0(sp)  # s5 = temp (for swap)

    # --- Setup and Validation ---
    mv s0, a0   # s0 = vec
    mv s1, a1   # s1 = length

    # Falha se length <= 0
    bgt s1, zero, length_ok
    li a0, 0    # Retorna 0 (falha)
    j restore_and_ret_sort

length_ok:
    # order validation (only 0 or 1 is valid)
    li t0, 1
    bne a2, zero, check_order_one # if order != 0, check if order == 1
    # order is 0 (DESC)
    mv s2, a2   # s2 = 0
    j start_sort

check_order_one:
    li t0, 1
    beq a2, t0, start_sort # if order == 1, start sort
    # order is invalid (> 1)
    li a0, 0    # Retorna 0 (falha)
    j restore_and_ret_sort

start_sort:
    mv s2, a2   # s2 = order (Valid 0 or 1)

    # --- Outer Loop (i from 0 to length - 2) ---
    mv s3, zero      # s3 = i = 0
    addi t0, s1, -1  # t0 = length - 1 (Outer loop limit)

outer_loop:
    bge s3, t0, sort_done # if (i >= length - 1) go to sort_done

    # --- Inner Loop (j from 0 to length - 2 - i) ---
    mv s4, zero      # s4 = j = 0
    sub t1, s1, s3   # t1 = length - i
    addi t1, t1, -1  # t1 = length - i - 1 (Inner loop limit)

inner_loop:
    bge s4, t1, inner_loop_end # if (j >= length - i - 1) go to inner_loop_end

    # --- Swap Logic (if needed) ---
    # Calcular endereços: vec[j] e vec[j+1]
    slli t2, s4, 2    # t2 = j * 4 (offset for vec[j])
    add t3, s0, t2    # t3 = &vec[j]
    addi t4, t3, 4    # t4 = &vec[j+1]

    # Carregar valores
    lw a3, 0(t3)      # a3 = vec[j]
    lw a4, 0(t4)      # a4 = vec[j+1]

    # Comparar (vec[j] e vec[j+1])
    # Se order=1 (ASC): swap se a3 > a4
    # Se order=0 (DESC): swap se a3 < a4

    bne s2, zero, check_asc_swap # se order != 0 (ASC), ir para check_asc_swap

    # order = 0 (DESC): swap se vec[j] < vec[j+1]
    blt a3, a4, swap_needed # if (a3 < a4) go to swap_needed
    j swap_continue

check_asc_swap:
    # order = 1 (ASC): swap se vec[j] > vec[j+1]
    bgt a3, a4, swap_needed # if (a3 > a4) go to swap_needed
    j swap_continue

swap_needed:
    # Swap: { temp = a3; a3 = a4; a4 = temp; }
    mv s5, a3         # s5 = temp = vec[j]
    mv a3, a4         # vec[j] = vec[j+1]
    mv a4, s5         # vec[j+1] = temp

    # Armazenar de volta
    sw a3, 0(t3)      # vec[j] = a3
    sw a4, 0(t4)      # vec[j+1] = a4

swap_continue:
    # Incrementar j
    addi s4, s4, 1    # j++
    j inner_loop

inner_loop_end:
    # Incrementar i
    addi s3, s3, 1    # i++
    j outer_loop

sort_done:
    li a0, 1 # Retorna 1 (sucesso)

restore_and_ret_sort:
    # --- Epilogue ---
    lw s5, 0(sp)
    lw s4, 4(sp)
    lw s3, 8(sp)
    lw s2, 12(sp)
    lw s1, 16(sp)
    lw s0, 20(sp)
    lw ra, 24(sp)
    addi sp, sp, 28
    ret