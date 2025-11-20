.section .text
.global sort_array

# Register Convention (ABI):
# a0: int* vec (base address of the array)
# a1: int length
# a2: char order (1 = ascending, 0 = descending)
# a0: int (return value, 1 for success, 0 for fail)
#
# Callee-saved registers used:
# s0: int* vec (base address)
# s1: int length
# s2: char order
# s3: int i (outer loop counter)
# s4: int j (inner loop counter)
# s5: int n_minus_1 (outer loop limit)
# s6: int inner_limit (n-1-i)
#
# Temporary registers used:
# t0: address of vec[j]
# t1: value of vec[j]
# t2: value of vec[j+1]
# t3: address of vec[j+1]

sort_array:
    # --- Prologue ---
    # Save all callee-saved registers we will modify
    addi sp, sp, -64
    sw   ra, 56(sp)
    sw   s0, 48(sp)
    sw   s1, 40(sp)
    sw   s2, 32(sp)
    sw   s3, 24(sp)
    sw   s4, 16(sp)
    sw   s5, 8(sp)
    sw   s6, 0(sp)

    # --- Setup ---
    mv   s0, a0     # s0 = vec
    mv   s1, a1     # s1 = length
    mv   s2, a2     # s2 = order

    # --- Failure Check: if (length <= 0) ---
    ble  s1, zero, fail

    # --- Bubble Sort Initialization ---
    # s5 = n - 1 (outer loop limit)
    addi s5, s1, -1

    # s3 = 0 (i = 0)
    li   s3, 0

outer_loop_start:
    # Check outer loop condition: if (i >= n-1) goto loop_end
    bge  s3, s5, loop_end

    # s4 = 0 (j = 0)
    li   s4, 0

    # s6 = (n - 1) - i (inner loop limit)
    sub  s6, s5, s3

inner_loop_start:
    # Check inner loop condition: if (j >= n-1-i) goto outer_loop_inc
    bge  s4, s6, outer_loop_inc

    # --- Element Comparison ---
    # 1. Get address of vec[j]
    #    t0 = j * 4
    slli t0, s4, 2
    #    t0 = vec + (j * 4)
    add  t0, s0, t0

    # 2. Load vec[j] and vec[j+1]
    lw   t1, 0(t0)  # t1 = vec[j]
    lw   t2, 4(t0)  # t2 = vec[j+1] (at offset +4 bytes)

    # 3. Check order
    #    if (order == 0) goto desc_check
    beq  s2, zero, desc_check

    # --- Ascending Order Check (order == 1) ---
    #    if (vec[j] <= vec[j+1]) goto inner_loop_inc (no swap)
    ble  t1, t2, inner_loop_inc
    j    swap

desc_check:
    # --- Descending Order Check (order == 0) ---
    #    if (vec[j] >= vec[j+1]) goto inner_loop_inc (no swap)
    bge  t1, t2, inner_loop_inc
    #    (otherwise, fall through to swap)

swap:
    # Swap vec[j] and vec[j+1]
    sw   t2, 0(t0)  # vec[j] = t2
    sw   t1, 4(t0)  # vec[j+1] = t1

inner_loop_inc:
    # j++
    addi s4, s4, 1
    j    inner_loop_start

outer_loop_inc:
    # i++
    addi s3, s3, 1
    j    outer_loop_start

loop_end:
    # --- Success Path ---
    li   a0, 1 # Return 1
    j    restore_and_ret

fail:
    # --- Failure Path ---
    li   a0, 0 # Return 0

restore_and_ret:
    # --- Epilogue ---
    # Restore all callee-saved registers
    lw   ra, 56(sp)
    lw   s0, 48(sp)
    lw   s1, 40(sp)
    lw   s2, 32(sp)
    lw   s3, 24(sp)
    lw   s4, 16(sp)
    lw   s5, 8(sp)
    lw   s6, 0(sp)
    addi sp, sp, 64

    ret # Return to caller