    .text
    .globl extract_data

extract_data:
    mv      t0, a0
    mv      t1, a1
    mv      t2, a2
    mv      t3, a3

    beq     t0, zero, fail
    beq     t1, zero, fail
    beq     t2, zero, fail
    beq     t3, zero, fail

outer_loop:
skip_ws_hash:
    lb      t4, 0(t0)
    beq     t4, zero, not_found

    li      t5, ' '
    beq     t4, t5, adv_outer

    li      t5, '#'
    beq     t4, t5, adv_outer

    j       try_match

adv_outer:
    addi    t0, t0, 1
    j       skip_ws_hash

try_match:
    mv      t4, t0
    mv      t5, t1

token_loop:
    lb      a4, 0(t5)
    beq     a4, zero, token_match_end

    lb      a5, 0(t4)
    beq     a5, zero, token_mismatch
    bne     a5, a4, token_mismatch

    addi    t4, t4, 1
    addi    t5, t5, 1
    j       token_loop

token_match_end:
    mv      t0, t4
    j       parse_unit_and_value

token_mismatch:
skip_to_hash:
    lb      t4, 0(t0)
    beq     t4, zero, not_found

    li      t5, '#'
    beq     t4, t5, skip_hash_and_continue

    addi    t0, t0, 1
    j       skip_to_hash

skip_hash_and_continue:
    addi    t0, t0, 1
    j       outer_loop

parse_unit_and_value:
skip_spaces1:
    lb      t4, 0(t0)
    beq     t4, zero, fail

    li      t5, ' '
    bne     t4, t5, check_amp_unit
    addi    t0, t0, 1
    j       skip_spaces1

check_amp_unit:
    li      t5, '&'
    bne     t4, t5, fail
    addi    t0, t0, 1

    lb      t4, 0(t0)
    li      t5, 'u'
    bne     t4, t5, fail
    addi    t0, t0, 1

    lb      t4, 0(t0)
    li      t5, 'n'
    bne     t4, t5, fail
    addi    t0, t0, 1

    lb      t4, 0(t0)
    li      t5, 'i'
    bne     t4, t5, fail
    addi    t0, t0, 1

    lb      t4, 0(t0)
    li      t5, 't'
    bne     t4, t5, fail
    addi    t0, t0, 1

skip_spaces2:
    lb      t4, 0(t0)
    beq     t4, zero, fail

    li      t5, ' '
    bne     t4, t5, expect_colon_unit
    addi    t0, t0, 1
    j       skip_spaces2

expect_colon_unit:
    li      t5, ':'
    bne     t4, t5, fail
    addi    t0, t0, 1

skip_spaces3:
    lb      t4, 0(t0)
    beq     t4, zero, fail

    li      t5, ' '
    bne     t4, t5, unit_copy_begin
    addi    t0, t0, 1
    j       skip_spaces3

unit_copy_begin:
    mv      t4, t2

unit_copy_loop:
    lb      t5, 0(t0)
    beq     t5, zero, unit_copy_end

    li      t6, '&'
    beq     t5, t6, unit_copy_end

    li      t6, '#'
    beq     t5, t6, unit_copy_end

    sb      t5, 0(t4)
    addi    t4, t4, 1
    addi    t0, t0, 1
    j       unit_copy_loop

unit_copy_end:
    sb      zero, 0(t4)

skip_spaces4:
    lb      t5, 0(t0)
    beq     t5, zero, fail

    li      t6, ' '
    bne     t5, t6, check_amp_value
    addi    t0, t0, 1
    j       skip_spaces4

check_amp_value:
    li      t6, '&'
    bne     t5, t6, fail
    addi    t0, t0, 1

    lb      t5, 0(t0)
    li      t6, 'v'
    bne     t5, t6, fail
    addi    t0, t0, 1

    lb      t5, 0(t0)
    li      t6, 'a'
    bne     t5, t6, fail
    addi    t0, t0, 1

    lb      t5, 0(t0)
    li      t6, 'l'
    bne     t5, t6, fail
    addi    t0, t0, 1

    lb      t5, 0(t0)
    li      t6, 'u'
    bne     t5, t6, fail
    addi    t0, t0, 1

    lb      t5, 0(t0)
    li      t6, 'e'
    bne     t5, t6, fail
    addi    t0, t0, 1

skip_spaces5:
    lb      t5, 0(t0)
    beq     t5, zero, fail

    li      t6, ' '
    bne     t5, t6, expect_colon_value
    addi    t0, t0, 1
    j       skip_spaces5

expect_colon_value:
    li      t6, ':'
    bne     t5, t6, fail
    addi    t0, t0, 1

skip_spaces6:
    lb      t5, 0(t0)
    beq     t5, zero, fail

    li      t6, ' '
    bne     t5, t6, digits_start
    addi    t0, t0, 1
    j       skip_spaces6

digits_start:
    li      t4, 0
    li      t6, 0

digit_loop:
    lb      t5, 0(t0)
    beq     t5, zero, end_digits

    li      a4, '0'
    blt     t5, a4, end_digits

    li      a5, '9'
    bgt     t5, a5, end_digits

    li      a4, 10
    mul     t4, t4, a4

    li      a4, '0'
    sub     t5, t5, a4
    add     t4, t4, t5

    addi    t0, t0, 1
    addi    t6, t6, 1
    j       digit_loop

end_digits:
    beq     t6, zero, fail

    sw      t4, 0(t3)
    li      a0, 1
    ret

not_found:
    j       fail

fail:
    beq     t2, zero, skip_unit_fail
    sb      zero, 0(t2)
skip_unit_fail:
    beq     t3, zero, skip_value_fail
    sw      zero, 0(t3)
skip_value_fail:
    li      a0, 0
    ret
