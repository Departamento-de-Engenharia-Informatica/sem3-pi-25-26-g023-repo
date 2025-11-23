.section .text
.global move_n_to_array

# Arguments (based on callfunc mapping from main.c):
# a0 = buffer
# a1 = length
# a2 = nelem* (pointer to valid elements)
# a3 = head* (pointer to read index) <-- CÓDIGO CORRIGIDO PARA USAR a3 AQUI
# a4 = tail* (pointer to write index - unused for dequeue)
# a5 = n      (number of elements to move)
# a6 = array  (destination array)

move_n_to_array:
    # Setup stack frame and save callee-saved registers
    addi sp, sp, -32
    sw s0, 28(sp)
    sw s1, 24(sp)
    sw s2, 20(sp)
    sw s3, 16(sp)
    sw s4, 12(sp)
    sw s5, 8(sp)
    sw ra, 4(sp)

    # Save argument registers to callee-saved registers
    mv s0, a0       # s0 = buffer
    mv s1, a1       # s1 = length
    mv s2, a2       # s2 = nelem* (pointer to element count)
    mv s3, a3       # s3 = head* (pointer to read index) <-- CORREÇÃO: Usa a3
    mv s4, a6       # s4 = array (destination)
    mv s5, a5       # s5 = n (number of elements to move)
    # Note: a4 (tail*) é ignorado/não guardado, pois não é necessário para esta operação

    # Load initial values
    lw t0, 0(s2)            # t0 = *nelem
    lw t1, 0(s3)            # t1 = head (read index)
    li t2, 0                # t2 = counter i = 0

    # Check for enough elements
    blt t0, s5, fail        # fail if not enough elements (*nelem < n)

copy_loop:
    bge t2, s5, copy_done   # Exit if i >= n

    # compute buffer index: (head + i) % length
    add t3, t1, t2          # t3 = head + i
    rem t3, t3, s1          # t3 = (head + i) % length

    # load element from buffer
    slli t4, t3, 2          # t4 = index * 4 (byte offset)
    add t4, s0, t4          # t4 = &buffer[index]
    lw t5, 0(t4)            # t5 = element value

    # store in array
    slli t6, t2, 2          # t6 = i * 4 (byte offset)
    add t6, s4, t6          # t6 = &array[i]
    sw t5, 0(t6)

    # Increment counter
    addi t2, t2, 1
    j copy_loop

copy_done:
    # Calculate new head = (head + n) % length
    add t1, t1, s5          # t1 = head + n
    rem t1, t1, s1          # t1 = (head + n) % length
    
    # Calculate new nelem = nelem - n
    sub t0, t0, s5          # t0 = *nelem - n

    # Store new head to head* (s3)
    sw t1, 0(s3) 

    # Store new nelem to nelem* (s2)
    sw t0, 0(s2)

    li a0, 1                # Success (return 1)
    j restore

fail:
    li a0, 0                # Failure (return 0)

restore:
    # Restore callee-saved registers and stack pointer
    lw ra, 4(sp)
    lw s5, 8(sp)
    lw s4, 12(sp)
    lw s3, 16(sp)
    lw s2, 20(sp)
    lw s1, 24(sp)
    lw s0, 28(sp)
    addi sp, sp, 32
    ret
