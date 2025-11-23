.text
.globl format_command

# int format_command(char* op, int n, char *cmd)
# Formats a command string according to specifications
# a0 = op* (input command string)
# a1 = n (integer parameter)
# a2 = cmd* (output buffer)
# Returns: 1 on success, 0 on failure
format_command:
    addi sp, sp, -32
    sw ra, 28(sp)
    sw s0, 24(sp)
    sw s1, 20(sp)
    sw s2, 16(sp)
    sw s3, 12(sp)

    mv s0, a0      # Save op pointer
    mv s1, a1      # Save n value
    mv s2, a2      # Save cmd output buffer

    # Initialize cmd as empty string first
    sb zero, 0(s2)

    # Check for empty input string
    lb t0, 0(s0)
    beqz t0, format_fail

    # Skip leading spaces and find start of command
    mv s3, s0      # current pointer for processing
skip_leading_spaces:
    lb t0, 0(s3)
    beqz t0, format_fail  # end of string reached
    li t1, ' '
    beq t0, t1, skip_space
    li t1, '\t'
    beq t0, t1, skip_space
    j process_command
skip_space:
    addi s3, s3, 1
    j skip_leading_spaces

process_command:
    # Load characters and convert to uppercase
    mv t4, s3      # temp pointer
    li t5, 0       # character counter

load_and_convert:
    lb t0, 0(t4)
    beqz t0, check_loaded_chars  # end of string

    # Skip trailing spaces - stop at first space after command
    li t1, ' '
    beq t0, t1, check_loaded_chars
    li t1, '\t'
    beq t0, t1, check_loaded_chars

    # Convert to uppercase if lowercase
    li t1, 'a'
    li t2, 'z'
    blt t0, t1, store_char
    bgt t0, t2, store_char
    addi t0, t0, -32

store_char:
    # Store in temporary buffer (use stack)
    addi sp, sp, -1
    sb t0, 0(sp)
    addi t5, t5, 1  # increment counter
    addi t4, t4, 1
    j load_and_convert

check_loaded_chars:
    # Now we have t5 characters on stack, all uppercase
    # Check command length and type

    # Restore characters from stack to registers
    beqz t5, format_fail_cleanup  # no characters

    # For 3-character command (GTH)
    li t0, 3
    bne t5, t0, check_2char

    # Pop 3 characters from stack
    lb t3, 0(sp)   # third char
    addi sp, sp, 1
    lb t2, 0(sp)   # second char
    addi sp, sp, 1
    lb t1, 0(sp)   # first char
    addi sp, sp, 1

    # Check if it's GTH
    li t0, 'G'
    bne t1, t0, check_2char_after_pop
    li t0, 'T'
    bne t2, t0, check_2char_after_pop
    li t0, 'H'
    bne t3, t0, check_2char_after_pop

    # It's GTH - store in output
    li t0, 'G'
    sb t0, 0(s2)
    li t0, 'T'
    sb t0, 1(s2)
    li t0, 'H'
    sb t0, 2(s2)
    li t0, 0
    sb t0, 3(s2)
    li a0, 1
    j format_exit

check_2char:
    # For 2-character commands
    li t0, 2
    bne t5, t0, format_fail_cleanup

    # Pop 2 characters from stack
    lb t2, 0(sp)   # second char
    addi sp, sp, 1
    lb t1, 0(sp)   # first char
    addi sp, sp, 1

check_2char_after_pop:
    # Check valid 2-char commands: RE, YE, GE, RB

    # Check RE command
    li t0, 'R'
    bne t1, t0, check_ye
    li t0, 'E'
    bne t2, t0, check_ye
    j valid_2char

check_ye:
    # Check YE command
    li t0, 'Y'
    bne t1, t0, check_ge
    li t0, 'E'
    bne t2, t0, check_ge
    j valid_2char

check_ge:
    # Check GE command
    li t0, 'G'
    bne t1, t0, check_rb
    li t0, 'E'
    bne t2, t0, check_rb
    j valid_2char

check_rb:
    # Check RB command
    li t0, 'R'
    bne t1, t0, format_fail
    li t0, 'B'
    bne t2, t0, format_fail

valid_2char:
    # Check n range [0, 99]
    li t0, 0
    blt s1, t0, format_fail
    li t0, 99
    bgt s1, t0, format_fail

    # Format output: "CMD,XX"
    sb t1, 0(s2)    # First character
    sb t2, 1(s2)    # Second character
    li t0, ','
    sb t0, 2(s2)    # Comma separator

    # Convert n to 2-digit string
    mv a0, s1       # n value
    addi a1, s2, 3  # output position after "CMD,"
    jal int_to_2digit

    # Add null terminator
    addi t0, s2, 5
    sb zero, 0(t0)

    li a0, 1        # Success
    j format_exit

format_fail_cleanup:
    # Clean up stack if we pushed characters
    beqz t5, format_fail
cleanup_loop:
    beqz t5, format_fail
    addi sp, sp, 1
    addi t5, t5, -1
    j cleanup_loop

format_fail:
    li a0, 0        # Failure
    sb zero, 0(s2)  # Ensure empty string

format_exit:
    # Restore saved registers and return
    lw ra, 28(sp)
    lw s0, 24(sp)
    lw s1, 20(sp)
    lw s2, 16(sp)
    lw s3, 12(sp)
    addi sp, sp, 32
    ret

# Helper function: int_to_2digit
# Converts integer (0-99) to 2-digit ASCII string
# a0 = integer value (0-99)
# a1 = output buffer (2 characters)
int_to_2digit:
    li t0, 10
    div t1, a0, t0  # tens digit
    rem t2, a0, t0  # ones digit

    # Convert to ASCII
    addi t1, t1, '0'
    addi t2, t2, '0'

    # Store digits in output buffer
    sb t1, 0(a1)
    sb t2, 1(a1)
    ret