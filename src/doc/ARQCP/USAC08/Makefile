CC = riscv32-linux-gcc
CFLAGS = -Wall -Wextra -fanalyzer -g
ARCHFLAGS = -march=rv32imd -mabi=ilp32d
LIBRV32 = ${HOME}/bin/bootlin/riscv32-buildroot-linux-gnu/sysroot

.PHONY: clean run debug

program_usac08.elf: usac08_main.o usac08_asm.o
	$(CC) $(CFLAGS) $(ARCHFLAGS) usac08_main.o usac08_asm.o -o program_usac08.elf

usac08_main.o: usac08_main.c usac08_asm.h
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c usac08_main.c -o usac08_main.o

usac08_asm.o: usac08_asm.s usac08_asm.h
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c usac08_asm.s -o usac08_asm.o

clean:
	rm -f *.o *.elf

run: clean program_usac08.elf
	qemu-riscv32 -L $(LIBRV32) ./program_usac08.elf

debug: program_usac08.elf
	@echo "On other terminal window do:"
	@echo "riscv32-linux-gdb-tui ./program_usac08.elf "
	qemu-riscv32 -L $(LIBRV32) -g 1234 program_usac08.elf -S