.text
.globl format_command

format_command:
    addi sp, sp, -32
    sw ra, 28(sp)
    sw s0, 24(sp)
    sw s1, 20(sp)
    sw s2, 16(sp)
    sw s3, 12(sp)

    mv s0, a0
    mv s1, a1
    mv s2, a2

    li t0, 0
    sb t0, 0(s2)

    mv t1, s0
    mv s3, s2

trim_loop:
    lb t0, 0(t1)
    beqz t0, trim_end
    li t2, ' '
    beq t0, t2, trim_next

    li t2, 'a'
    blt t0, t2, store_trim
    li t2, 'z'
    bgt t0, t2, store_trim
    addi t0, t0, -32

store_trim:
    sb t0, 0(s3)
    addi s3, s3, 1

trim_next:
    addi t1, t1, 1
    j trim_loop

trim_end:
    sb zero, 0(s3)

    mv a0, s2
    la a1, cmd_re
    jal ra, strcmp_impl
    beqz a0, cmd_with_num

    mv a0, s2
    la a1, cmd_ye
    jal ra, strcmp_impl
    beqz a0, cmd_with_num

    mv a0, s2
    la a1, cmd_ge
    jal ra, strcmp_impl
    beqz a0, cmd_with_num

    mv a0, s2
    la a1, cmd_rb
    jal ra, strcmp_impl
    beqz a0, cmd_with_num

    mv a0, s2
    la a1, cmd_gth
    jal ra, strcmp_impl
    beqz a0, cmd_without_num

    j format_error

cmd_with_num:
    bltz s1, format_error
    li t0, 99
    bgt s1, t0, format_error

    mv t1, s2
    mv t2, s2

copy_cmd:
    lb t0, 0(t1)
    beqz t0, add_comma
    sb t0, 0(t2)
    addi t1, t1, 1
    addi t2, t2, 1
    j copy_cmd

add_comma:
    li t0, ','
    sb t0, 0(t2)
    addi t2, t2, 1

    li t3, 10
    blt s1, t3, one_digit

    div t4, s1, t3
    rem t5, s1, t3

    addi t4, t4, '0'
    sb t4, 0(t2)
    addi t2, t2, 1

    addi t5, t5, '0'
    sb t5, 0(t2)
    addi t2, t2, 1
    j finish_num

one_digit:
    li t4, '0'
    sb t4, 0(t2)
    addi t2, t2, 1

    addi t5, s1, '0'
    sb t5, 0(t2)
    addi t2, t2, 1

finish_num:
    sb zero, 0(t2)
    li a0, 1
    j finish

cmd_without_num:
    la t1, cmd_gth
    mv t2, s2

copy_gth_loop:
    lb t0, 0(t1)
    beqz t0, gth_end
    sb t0, 0(t2)
    addi t1, t1, 1
    addi t2, t2, 1
    j copy_gth_loop

gth_end:
    sb zero, 0(t2)
    li a0, 1
    j finish

format_error:
    li t0, 0
    sb t0, 0(s2)
    li a0, 0

finish:
    lw ra, 28(sp)
    lw s0, 24(sp)
    lw s1, 20(sp)
    lw s2, 16(sp)
    lw s3, 12(sp)
    addi sp, sp, 32
    ret

strcmp_impl:
    mv t0, a0
    mv t1, a1

strcmp_loop:
    lb t2, 0(t0)
    lb t3, 0(t1)
    bne t2, t3, cmp_diff
    beqz t2, cmp_eq
    addi t0, t0, 1
    addi t1, t1, 1
    j strcmp_loop

cmp_eq:
    li a0, 0
    ret

cmp_diff:
    li a0, 1
    ret

.section .rodata
cmd_re:  .string "RE"
cmd_ye:  .string "YE"
cmd_ge:  .string "GE"
cmd_rb:  .string "RB"
cmd_gth: .string "GTH"
