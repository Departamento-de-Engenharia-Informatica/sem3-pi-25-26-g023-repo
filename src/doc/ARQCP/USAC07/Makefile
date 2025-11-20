CC = riscv32-linux-gcc
CFLAGS = -Wall -Wextra -fanalyzer -g
ARCHFLAGS = -march=rv32imd -mabi=ilp32d
LIBRV32 = ${HOME}/bin/bootlin/riscv32-buildroot-linux-gnu/sysroot

.PHONY: clean run debug

program.elf: main.o asm.o 
	$(CC) $(CFLAGS) $(ARCHFLAGS) main.o asm.o -o program.elf

main.o: main.c asm.h
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c main.c -o main.o

asm.o: asm.s asm.h
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c asm.s -o asm.o

clean:
	rm -f *.o *.elf

run: clean program.elf
	qemu-riscv32 -L $(LIBRV32) ./program.elf

debug: program.elf
	@echo "On other terminal window do:"
	@echo "riscv32-linux-gdb-tui ./program.elf "
	qemu-riscv32 -L $(LIBRV32) -g 1234 program.elf -S
