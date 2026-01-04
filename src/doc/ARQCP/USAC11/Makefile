CC       = riscv32-linux-gcc
CFLAGS   = -Wall -Wextra -fanalyzer -g
ARCHFLAGS= -march=rv32imd -mabi=ilp32d
LIBRV32  = ${HOME}/bin/bootlin/riscv32-buildroot-linux-gnu/sysroot

SRCS = main.c manager_setup.c
ASMS = encrypt_data.s
OBJS = main.o manager_setup.o encrypt_data.o

prog.elf: $(OBJS)
	$(CC) $(CFLAGS) $(ARCHFLAGS) $(OBJS) -o prog.elf

%.o: %.c
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c $<

%.o: %.s
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c $<

run: prog.elf
	qemu-riscv32 -L $(LIBRV32) ./prog.elf

clean:
	rm -f *.o *.elf
