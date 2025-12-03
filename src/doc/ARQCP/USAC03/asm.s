    .text
    .globl extract_data
    .globl find_token
    .globl to_num

# int extract_data(char* input, char* token, char* unit, int* value)
extract_data:
    addi sp, sp, -32
    sw ra, 28(sp)
    sw s0, 24(sp)
    sw s1, 20(sp)
    sw s2, 16(sp)
    sw s3, 12(sp)

    mv s0, a0
    mv s1, a1
    mv s2, a2
    mv s3, a3

    sb zero, 0(s2)
    sw zero, 0(s3)

    lb t0, 0(s0)
    beqz t0, fail
    lb t0, 0(s1)
    beqz t0, fail

    mv a0, s0
    mv a1, s1
    call find_token

    beqz a0, fail
    mv s0, a0

search_unit:
    lb t0, 0(s0)
    beqz t0, fail
    li t1, '&'
    bne t0, t1, next1

    lb t0, 1(s0)
    li t1, 'u'
    bne t0, t1, next1
    lb t0, 2(s0)
    li t1, 'n'
    bne t0, t1, next1
    lb t0, 3(s0)
    li t1, 'i'
    bne t0, t1, next1
    lb t0, 4(s0)
    li t1, 't'
    bne t0, t1, next1
    lb t0, 5(s0)
    li t1, ':'
    bne t0, t1, next1

    addi t0, s0, 6
    mv t1, s2

copy_unit:
    lb t2, 0(t0)
    beqz t2, fail
    li t3, '&'
    beq t2, t3, unit_done
    sb t2, 0(t1)
    addi t0, t0, 1
    addi t1, t1, 1
    j copy_unit

unit_done:
    sb zero, 0(t1)
    mv s0, t0

    lb t0, 1(s0)
    li t1, 'v'
    bne t0, t1, fail
    lb t0, 2(s0)
    li t1, 'a'
    bne t0, t1, fail
    lb t0, 3(s0)
    li t1, 'l'
    bne t0, t1, fail
    lb t0, 4(s0)
    li t1, 'u'
    bne t0, t1, fail
    lb t0, 5(s0)
    li t1, 'e'
    bne t0, t1, fail
    lb t0, 6(s0)
    li t1, ':'
    bne t0, t1, fail

    addi a0, s0, 7
    call to_num

    sw a0, 0(s3)
    li a0, 1
    j done

next1:
    addi s0, s0, 1
    j search_unit

fail:
    li a0, 0

done:
    lw ra, 28(sp)
    lw s0, 24(sp)
    lw s1, 20(sp)
    lw s2, 16(sp)
    lw s3, 12(sp)
    addi sp, sp, 32
    ret

# char* find_token(char* input, char* token)
find_token:
    mv t0, a0

search_loop:
    lb t1, 0(t0)
    beqz t1, not_found
    beq t0, a0, try_match
    lb t2, -1(t0)
    li t3, '#'
    beq t2, t3, try_match
    addi t0, t0, 1
    j search_loop

try_match:
    mv t2, a1
    mv t3, t0

match_loop:
    lb t4, 0(t2)
    beqz t4, check_end
    lb t5, 0(t3)
    beqz t5, next_token
    bne t4, t5, next_token
    addi t2, t2, 1
    addi t3, t3, 1
    j match_loop

check_end:
    lb t4, 0(t3)
    beqz t4, found
    li t5, '&'
    beq t4, t5, found
    li t5, '#'
    beq t4, t5, found

next_token:
    addi t0, t0, 1
    j search_loop

found:
    mv a0, t3
    ret

not_found:
    li a0, 0
    ret

# int to_num(char* str)
to_num:
    li t0, 0
    li t1, 10
    li t2, 0

    lb t3, 0(a0)
    li t4, '-'
    bne t3, t4, convert
    li t2, 1
    addi a0, a0, 1

convert:
    lb t3, 0(a0)
    beqz t3, done_num
    li t4, '0'
    blt t3, t4, done_num
    li t4, '9'
    bgt t3, t4, done_num
    addi t3, t3, -48
    mul t0, t0, t1
    add t0, t0, t3
    addi a0, a0, 1
    j convert

done_num:
    beqz t2, positive
    neg t0, t0

positive:
    mv a0, t0
    ret

# Newline no final