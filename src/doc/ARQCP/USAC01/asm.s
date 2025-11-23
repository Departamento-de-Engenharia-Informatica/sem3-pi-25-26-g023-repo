.section .text
.global encrypt_data

# Register Convention (ABI):
# a0: char* in (input string pointer)
# a1: int key (encryption key)
# a2: char* out (output buffer pointer)
# a0: int (return value, 1 for success, 0 for fail)
#
# Callee-saved registers used:
# s0: char* out (pointer being incremented in the loop)
# s1: char* out_start (saved original 'out' pointer for failure case)
#
# Caller-saved (temporary) registers used:
# a2: (re-purposed) holds current 'in' pointer
# a3: (re-purposed) holds 'key'
# t0: current character being processed (char_in)
# t1: temporary for comparisons ('A', 'Z', 1, 26)
# t2: constant 26 (for modulo operation)

encrypt_data:
    # --- Prologue ---
    # Save callee-saved registers we will modify (s0, s1, ra)
    addi sp, sp, -20
    sw  ra, 16(sp)   # Save Return Address
    sw  s0, 12(sp)   # Save s0
    sw  s1, 8(sp)    # Save s1

    # --- Setup ---
    mv  s0, a2       # s0 = 'out' pointer (for loop, will be incremented)
    mv  s1, a2       # s1 = 'out_start' pointer (saved original 'out' pointer for failure)
    mv  a2, a0       # a2 = use as 'in' pointer
    mv  a3, a1       # a3 = use as 'key'
    li  t2, 26       # t2 = 26 (constant for modulo)

loop_start:
    # 1. Load current character from input
    lbu t0, 0(a2)    # t0 = *in (load byte unsigned)

    # 2. Check for end of string FIRST (CORREÇÃO: resolve test_None)
    beq t0, zero, loop_end # if (t0 == '\0') go to loop_end

    # -------------------------------------------------------------------
    # --- Failure Check 1: Key (a3) must be in [1, 26] (AGORA AQUI) ---
    # Esta verificação só é feita se houver um caractere para processar.
    # -------------------------------------------------------------------
    li  t1, 1
    blt a3, t1, fail # if (key < 1) go to fail (Apanha key=0 se string NÃO for vazia)
    li  t1, 26
    bgt a3, t1, fail # if (key > 26) go to fail

    # --- Failure Check 2: Character (t0) must be in ['A', 'Z'] ---
    li  t1, 'A'
    blt t0, t1, fail # if (char < 'A') go to fail
    li  t1, 'Z'
    bgt t0, t1, fail # if (char > 'Z') go to fail

    # --- Caesar Cipher Encryption Logic ---

    # 3. Normalize: (char - 'A') -> (0 to 25)
    li  t1, 'A'
    sub t0, t0, t1

    # 4. Add key: (char + key)
    add t0, t0, a3

    # 5. Modulo 26 (handle positive wrap-around)
    blt t0, t2, skip_mod
    sub t0, t0, t2   # t0 = t0 - 26
skip_mod:

    # 6. De-normalize: (char + 'A')
    li  t1, 'A'
    add t0, t0, t1

    # 7. Store encrypted char in output
    sb  t0, 0(s0)    # *out = t0 (store byte)

    # 8. Increment pointers
    addi a2, a2, 1   # in++
    addi s0, s0, 1   # out++

    # 9. Repeat
    j   loop_start

loop_end:
    # --- Success Path ---
    # 1. Add null terminator to output string
    sb  zero, 0(s0)  # *out = '\0'

    # 2. Set return value to 1 (success)
    li  a0, 1
    j   restore_and_ret

fail:
    # --- Failure Path ---
    # 1. Set 'out' to an empty string (using s1 = original 'out' pointer)
    sb  zero, 0(s1)  # *out_start = '\0'

    # 2. Set return value to 0 (failure)
    li  a0, 0
    # (fall through to restore and return)

restore_and_ret:
    # --- Epilogue ---
    # Restore callee-saved registers (s1, s0, ra)
    lw  s1, 8(sp)
    lw  s0, 12(sp)
    lw  ra, 16(sp)
    addi sp, sp, 20

    ret
