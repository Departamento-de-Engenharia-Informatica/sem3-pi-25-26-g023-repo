.text
.globl encrypt_data

encrypt_data:

li t0,1
blt a1,t0,invalid_key
li t0,25
bgt a1,t0,invalid_key

mv t1,a0
mv t2,a2

loop:

lb t3,0(t1)

beqz t3,success

li t4,65
blt t3,t4,invalid_in
li t4,90
bgt t3,t4,invalid_in

li t4,65
sub t3,t3,t4
add t3,t3,a1

li t4,26
rem t3,t3,t4

li t4,65
add t3,t3,t4

sb t3,0(t2)

addi t1,t1,1
addi t2,t2,1

j loop

invalid_in:
invalid_key:

sb zero,0(a2)
li a0,0
ret

success:

sb zero, 0(t2)
li a0,1
ret


