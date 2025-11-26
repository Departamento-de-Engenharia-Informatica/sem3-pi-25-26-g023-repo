CC = riscv32-linux-gcc
CFLAGS = -Wall -Wextra -g -O0 -static
LIBRV32 = ${HOME}/bin/bootlin/riscv32-buildroot-linux-gnu/sysroot

.PHONY: clean run debug

usac04_test.elf: main.c callfunc.s asm.s asm.h unity.c
	$(CC) $(CFLAGS) main.c callfunc.s asm.s unity.c -o usac04_test.elf

clean:
	rm -f *.o *.elf

run: usac04_test.elf
	qemu-riscv32 -L $(LIBRV32) ./usac04_test.elf

debug: usac04_test.elf
	qemu-riscv32 -L $(LIBRV32) -g 1234 ./usac04_test.elf