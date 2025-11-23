.text
.globl enqueue_value

# int enqueue_value(int* buffer, int length, int *nelem,
#                   int* tail, int* head, int value)
#
# a0 = buffer
# a1 = length
# a2 = nelem (ptr)
# a3 = tail  (ptr)
# a4 = head  (ptr)
# a5 = value
#
# returns a0 = 1 if buffer full after insertion, 0 otherwise

enqueue_value:
    addi sp, sp, -32
    sw ra, 28(sp)
    sw s0, 24(sp)
    sw s1, 20(sp)
    sw s2, 16(sp)
    sw s3, 12(sp)
    sw s4, 8(sp)

    mv s0, a0          # s0 = buffer
    mv s1, a1          # s1 = length
    mv s2, a2          # s2 = nelem*
    mv s3, a3          # s3 = tail*
    mv s4, a4          # s4 = head*
    # a5 = value stays in a5

    # load current nelem
    lw t0, 0(s2)       # t0 = *nelem

    # check if full: t0 == length ?
    beq t0, s1, full_case

not_full_case:
    addi t0, t0, 1     # nelem++
    sw t0, 0(s2)
    j insert_value

full_case:
    # advance head to drop oldest
    lw t1, 0(s4)       # t1 = *head
    addi t1, t1, 1
    rem t1, t1, s1     # t1 = (head+1) % length
    sw t1, 0(s4)
    # nelem stays == length

insert_value:
    # tail index
    lw t2, 0(s3)       # t2 = *tail

    # store value at buffer[tail]
    slli t3, t2, 2     # offset = tail * 4
    add t3, s0, t3     # &buffer[tail]
    sw a5, 0(t3)

    # advance tail
    addi t2, t2, 1
    rem t2, t2, s1     # tail = (tail+1) % length
    sw t2, 0(s3)

    # return 1 if full after insertion
    lw t0, 0(s2)       # reload nelem
    beq t0, s1, ret_full
    li a0, 0
    j finish

ret_full:
    li a0, 1

finish:
    lw ra, 28(sp)
    lw s0, 24(sp)
    lw s1, 20(sp)
    lw s2, 16(sp)
    lw s3, 12(sp)
    lw s4, 8(sp)
    addi sp, sp, 32
    ret
