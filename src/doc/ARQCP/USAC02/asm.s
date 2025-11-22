.section .text
.global decrypt_data

# Register Convention (ABI):
# a0: char* in (input string pointer)
# a1: int key (decryption key)
# a2: char* out (output buffer pointer)
# a0: int (return value, 1 for success, 0 for fail)
#
# Callee-saved registers used:
# s0: char* out (saved original 'out' pointer for failure case)
#
# Caller-saved (temporary) registers used:
# a2: (re-purposed) holds current 'in' pointer
# a3: (re-purposed) holds 'key'
# t0: current character being processed (char_in)
# t1: temporary for comparisons ('A', 'Z', 1, 26)
# t2: constant 26 (for modulo operation)

decrypt_data:
    # --- Prologue ---
    # Save callee-saved registers we will modify
    addi sp, sp, -16
    sw   ra, 12(sp)   # Save Return Address (word store for RV32)
    sw   s0, 8(sp)    # Save s0

    # --- Setup ---
    mv   s0, a2     # s0 = save original 'out' pointer
    mv   a2, a0     # a2 = use as 'in' pointer (frees a0 for return value)
    mv   a3, a1     # a3 = use as 'key'

    # If input is empty string, succeed immediately (ignore key)
    lbu  t0, 0(a2)    # load first byte
    beq  t0, zero, loop_end

    # --- Failure Check 1: Key (a3) must be in [1, 26] ---
    li   t1, 1
    blt  a3, t1, fail  # if (key < 1) goto fail
    li   t1, 26
    bgt  a3, t1, fail  # if (key > 26) goto fail
    li   t2, 26        # t2 = 26 (constant for modulo)

loop_start:
    # 1. Load current character from input
    lbu  t0, 0(a2)  # t0 = *in (load byte unsigned)

    # 2. Check for end of string
    beq  t0, zero, loop_end # if (t0 == '\0') goto loop_end

    # --- Failure Check 2: Character (t0) must be in ['A', 'Z'] ---
    li   t1, 'A'
    blt  t0, t1, fail  # if (char < 'A') goto fail
    li   t1, 'Z'
    bgt  t0, t1, fail  # if (char > 'Z') goto fail

    # --- Caesar Cipher Decryption Logic ---

    # 3. Normalize: (char - 'A') -> (0 to 25)
    li   t1, 'A'
    sub  t0, t0, t1

    # 4. Apply inverse key: (char - key)
    sub  t0, t0, a3

    # 5. Modulo 26 (handle negative wrap-around)
    # If (t0 < 0), we must add 26. (e.g., 'A' - 1 -> -1 -> 25 -> 'Z')
    blt  t0, zero, add_mod
    j    skip_mod
add_mod:
    add  t0, t0, t2    # t0 = t0 + 26 (handles wrap-around)
skip_mod:

    # 6. De-normalize: (char + 'A')
    li   t1, 'A'
    add  t0, t0, t1

    # 7. Store decrypted char in output
    sb   t0, 0(s0)     # *out = t0 (store byte)

    # 8. Increment pointers
    addi a2, a2, 1     # in++
    addi s0, s0, 1     # out++

    # 9. Repeat
    j    loop_start

loop_end:
    # --- Success Path ---
    # 1. Add null terminator to output string
    sb   zero, 0(s0)   # *out = '\0'

    # 2. Set return value to 1 (success)
    li   a0, 1
    j    restore_and_ret

fail:
    # --- Failure Path ---
    # 1. Set 'out' to an empty string (as required by PDF)
    #    (s0 still points to the *start* of the out buffer)
    sb   zero, 0(s0)   # *out = '\0'

    # 2. Set return value to 0 (failure)
    li   a0, 0
    # (fall through to restore and return)

restore_and_ret:
    # --- Epilogue ---
    # Restore callee-saved registers
    lw   s0, 8(sp)
    lw   ra, 12(sp)
    addi sp, sp, 16

    ret                # Return to caller
