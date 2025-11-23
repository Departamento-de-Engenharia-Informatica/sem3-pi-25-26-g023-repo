CC = riscv32-linux-gcc
CFLAGS = -Wall -Wextra -fanalyzer -g
ARCHFLAGS = -march=rv32imd -mabi=ilp32d
LIBRV32 = ${HOME}/bin/bootlin/riscv32-buildroot-linux-gnu/sysroot

.PHONY: clean run debug

program_usac03.elf: usac03_main.o usac03_asm.o
	$(CC) $(CFLAGS) $(ARCHFLAGS) usac03_main.o usac03_asm.o -o program_usac03.elf

usac03_main.o: usac03_main.c usac03_asm.h
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c usac03_main.c -o usac03_main.o

usac03_asm.o: usac03_asm.s usac03_asm.h
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c usac03_asm.s -o usac03_asm.o

clean:
	rm -f *.o *.elf

run: clean program_usac03.elf
	qemu-riscv32 -L $(LIBRV32) ./program_usac03.elf

debug: program_usac03.elf
	@echo "On other terminal window do:"
	@echo "riscv32-linux-gdb-tui ./program_usac03.elf "
	qemu-riscv32 -L $(LIBRV32) -g 1234 program_usac03.elf -S