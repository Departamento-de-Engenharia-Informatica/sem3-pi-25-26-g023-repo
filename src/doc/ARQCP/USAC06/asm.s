.text
.globl dequeue_value

# int dequeue_value(int* buffer, int length, int *nelem,
#                   int* tail, int* head, int *value)
#
# a0 = buffer
# a1 = length
# a2 = nelem*
# a3 = tail*
# a4 = head*
# a5 = value*
#
# returns: 1 if success, 0 if empty

dequeue_value:
    addi sp, sp, -32
    sw ra, 28(sp)
    sw s0, 24(sp)
    sw s1, 20(sp)
    sw s2, 16(sp)
    sw s3, 12(sp)
    sw s4, 8(sp)

    mv s0, a0      # buffer
    mv s1, a1      # length
    mv s2, a2      # nelem*
    mv s3, a3      # tail*
    mv s4, a4      # head*
    mv t6, a5      # value*

    # load nelem
    lw t0, 0(s2)
    beqz t0, empty_case   # if nelem == 0 → cannot dequeue

    # read head index
    lw t1, 0(s4)          # t1 = head

    # buffer[head]
    slli t2, t1, 2        # t2 = head * 4
    add t2, t2, s0        # &buffer[head]
    lw t3, 0(t2)          # t3 = buffer[head]

    # store into *value
    sw t3, 0(t6)

    # decrease nelem
    addi t0, t0, -1
    sw t0, 0(s2)

    # advance head
    addi t1, t1, 1
    rem t1, t1, s1
    sw t1, 0(s4)

    # success → return 1
    li a0, 1
    j finish

empty_case:
    li a0, 0       # failure

finish:
    lw ra, 28(sp)
    lw s0, 24(sp)
    lw s1, 20(sp)
    lw s2, 16(sp)
    lw s3, 12(sp)
    lw s4, 8(sp)
    addi sp, sp, 32
    ret
