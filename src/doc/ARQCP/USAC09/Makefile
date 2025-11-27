CC = riscv32-linux-gcc
CFLAGS = -Wall -Wextra -g -O0 -static
LIBRV32 = ${HOME}/bin/bootlin/riscv32-buildroot-linux-gnu/sysroot

.PHONY: clean run debug

median_test.elf: main.c callfunc.s asm.s asm.h unity.c
	$(CC) $(CFLAGS) main.c callfunc.s asm.s unity.c -o median_test.elf

clean:
	rm -f *.o *.elf

run: median_test.elf
	qemu-riscv32 -L $(LIBRV32) ./median_test.elf

debug: median_test.elf
	qemu-riscv32 -L $(LIBRV32) -g 1234 ./median_test.elf