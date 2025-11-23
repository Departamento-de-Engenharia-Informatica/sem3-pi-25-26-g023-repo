.text
.globl extract_data

# int extract_data(char* input, char* token, char* unit, int* value)
extract_data:
    addi sp, sp, -32
    sw ra, 28(sp)
    sw s0, 24(sp)  # input
    sw s1, 20(sp)  # token
    sw s2, 16(sp)  # unit
    sw s3, 12(sp)  # value
    sw s4, 8(sp)   # current pointer

    mv s0, a0  # input
    mv s1, a1  # token
    mv s2, a2  # unit
    mv s3, a3  # value

    # Initialize outputs
    sb zero, 0(s2)
    sw zero, 0(s3)

    # Start search from beginning
    mv s4, s0

search_loop:
    lb t0, 0(s4)
    beqz t0, not_found

    # Check if current position matches token (4 characters)
    mv t1, s4
    mv t2, s1
    li t3, 4

check_loop:
    beqz t3, check_structure
    lb t4, 0(t1)
    lb t5, 0(t2)
    bne t4, t5, next_char
    addi t1, t1, 1
    addi t2, t2, 1
    addi t3, t3, -1
    j check_loop

next_char:
    addi s4, s4, 1
    j search_loop

check_structure:
    # Token matched, check if followed by valid structure
    addi t0, s4, 4  # position after token

    # Check for "&unit::"
    lb t1, 0(t0)   # '&'
    li t2, '&'
    bne t1, t2, next_char
    lb t1, 1(t0)   # 'u'
    li t2, 'u'
    bne t1, t2, next_char
    lb t1, 2(t0)   # 'n'
    li t2, 'n'
    bne t1, t2, next_char
    lb t1, 3(t0)   # 'i'
    li t2, 'i'
    bne t1, t2, next_char
    lb t1, 4(t0)   # 't'
    li t2, 't'
    bne t1, t2, next_char
    lb t1, 5(t0)   # ':'
    li t2, ':'
    bne t1, t2, next_char
    lb t1, 6(t0)   # ':'
    li t2, ':'
    bne t1, t2, next_char

    # Valid structure found - extract data
    addi s4, s4, 4  # skip token
    addi s4, s4, 7  # skip "&unit::"

    # Extract unit
    mv t0, s2
unit_loop:
    lb t1, 0(s4)
    li t2, '&'
    beq t1, t2, unit_done
    beqz t1, not_found
    sb t1, 0(t0)
    addi s4, s4, 1
    addi t0, t0, 1
    j unit_loop

unit_done:
    sb zero, 0(t0)

    # Skip '&' and check "value::"
    addi s4, s4, 1

    lb t1, 0(s4)   # 'v'
    li t2, 'v'
    bne t1, t2, not_found
    lb t1, 1(s4)   # 'a'
    li t2, 'a'
    bne t1, t2, not_found
    lb t1, 2(s4)   # 'l'
    li t2, 'l'
    bne t1, t2, not_found
    lb t1, 3(s4)   # 'u'
    li t2, 'u'
    bne t1, t2, not_found
    lb t1, 4(s4)   # 'e'
    li t2, 'e'
    bne t1, t2, not_found
    lb t1, 5(s4)   # ':'
    li t2, ':'
    bne t1, t2, not_found
    lb t1, 6(s4)   # ':'
    li t2, ':'
    bne t1, t2, not_found

    addi s4, s4, 7  # skip "value::"

    # Convert number
    mv t0, s4
    li t1, 0
    li t2, 10

convert_loop:
    lb t3, 0(t0)
    beqz t3, convert_done
    li t4, '#'
    beq t3, t4, convert_done
    li t4, '0'
    blt t3, t4, convert_done
    li t4, '9'
    bgt t3, t4, convert_done

    mul t1, t1, t2
    addi t4, zero, '0'
    sub t5, t3, t4
    add t1, t1, t5

    addi t0, t0, 1
    j convert_loop

convert_done:
    sw t1, 0(s3)
    li a0, 1
    j exit

not_found:
    li a0, 0

exit:
    lw ra, 28(sp)
    lw s0, 24(sp)
    lw s1, 20(sp)
    lw s2, 16(sp)
    lw s3, 12(sp)
    lw s4, 8(sp)
    addi sp, sp, 32
    ret