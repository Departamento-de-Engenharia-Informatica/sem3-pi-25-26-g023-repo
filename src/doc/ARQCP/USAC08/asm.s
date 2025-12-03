    .text
    .globl sort_array

# int sort_array(int* ptr, int num, char order)
sort_array:
    # Se num == 0, retornar 0
    beqz a1, return_zero

    # Se num == 1, retornar 1
    li t0, 1
    beq a1, t0, return_one

    # Bubble sort para num > 1
    addi sp, sp, -32
    sw ra, 28(sp)
    sw s0, 24(sp)
    sw s1, 20(sp)
    sw s2, 16(sp)
    sw s3, 12(sp)
    sw s4, 8(sp)

    mv s0, a0              # array
    mv s1, a1              # num
    mv s2, a2              # order (0=desc, !=0=asc)

    li s3, 0               # i = 0

outer_loop:
    addi t0, s1, -1        # num-1
    bge s3, t0, end_sort   # i >= num-1

    li s4, 0               # j = 0
    sub t0, s1, s3         # num-i
    addi t0, t0, -1        # num-i-1

inner_loop:
    bge s4, t0, end_inner  # j >= num-i-1

    slli t1, s4, 2         # j * 4
    add t2, s0, t1         # &ptr[j]
    addi t3, t2, 4         # &ptr[j+1]

    lw t4, 0(t2)           # ptr[j]
    lw t5, 0(t3)           # ptr[j+1]

    # Verificar se precisa trocar baseado na ordem
    beqz s2, check_desc    # se order == 0, descendente

    # Ascendente: trocar se ptr[j] > ptr[j+1]
    ble t4, t5, no_swap
    j do_swap

check_desc:
    # Descendente: trocar se ptr[j] < ptr[j+1]
    bge t4, t5, no_swap

do_swap:
    # Trocar valores
    sw t5, 0(t2)
    sw t4, 0(t3)

no_swap:
    addi s4, s4, 1         # j++
    j inner_loop

end_inner:
    addi s3, s3, 1         # i++
    j outer_loop

end_sort:
    lw ra, 28(sp)
    lw s0, 24(sp)
    lw s1, 20(sp)
    lw s2, 16(sp)
    lw s3, 12(sp)
    lw s4, 8(sp)
    addi sp, sp, 32

return_one:
    li a0, 1
    ret

return_zero:
    li a0, 0
    ret