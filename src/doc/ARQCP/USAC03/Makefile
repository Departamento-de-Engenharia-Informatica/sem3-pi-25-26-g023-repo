CC = riscv32-linux-gcc
CFLAGS = -Wall -Wextra -g -O0
ARCHFLAGS = -march=rv32imd -mabi=ilp32d
LIBRV32 = ${HOME}/bin/bootlin/riscv32-buildroot-linux-gnu/sysroot

.PHONY: clean run debug

program.elf: main.o asm.o
	$(CC) $(CFLAGS) $(ARCHFLAGS) main.o asm.o -o program.elf

main.o: main.c asm.h
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c main.c -o main.o

asm.o: asm.s
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c asm.s -o asm.o

clean:
	rm -f *.o *.elf

run: program.elf
	qemu-riscv32 -L $(LIBRV32) ./program.elf