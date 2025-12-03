# Makefile para USAC08
CC = riscv32-linux-gcc
CFLAGS = -Wall -Wextra -g -O0 -static
LIBRV32 = ${HOME}/bin/bootlin/riscv32-buildroot-linux-gnu/sysroot
TARGET = usac08.elf

.PHONY: all run clean

all: $(TARGET)

$(TARGET): main.c callfunc.s asm.s unity.c
	$(CC) $(CFLAGS) main.c callfunc.s asm.s unity.c -o $(TARGET)

run: $(TARGET)
	qemu-riscv32 -L $(LIBRV32) ./$(TARGET)

clean:
	rm -f *.o $(TARGET)