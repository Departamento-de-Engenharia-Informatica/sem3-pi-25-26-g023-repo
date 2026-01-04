# USAC14/Makefile - Com Assembly obrigatório
CC       = riscv32-linux-gcc
AS       = riscv32-unknown-elf-as
CFLAGS   = -Wall -Wextra -g -O0
ARCHFLAGS = -march=rv32imd -mabi=ilp32d
LIBRV32  = ${HOME}/bin/bootlin/riscv32-buildroot-linux-gnu/sysroot

# Ficheiros
ASM_SRC = format_light_command.s
C_SRC = lightsigns_comm.c test_usac14.c
OBJS = $(ASM_SRC:.s=.o) $(C_SRC:.c=.o)

TARGET = usac14_test.elf

all: $(TARGET)

# Linkar tudo
$(TARGET): $(OBJS)
	$(CC) $(CFLAGS) $(ARCHFLAGS) -o $@ $(OBJS)
	@echo "✅ USAC14 com Assembly compilada!"

# Compilar Assembly
%.o: %.s
	$(AS) $< -o $@
	@echo "🔨 Compilado Assembly: $<"

# Compilar C
%.o: %.c
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c $< -o $@

# Executar
run: $(TARGET)
	@echo "🚀 Executando USAC14 com Assembly..."
	qemu-riscv32 -L $(LIBRV32) ./$(TARGET)

# Limpar
clean:
	rm -f *.o *.elf
	@echo "🧹 Ficheiros limpos"

.PHONY: all run clean