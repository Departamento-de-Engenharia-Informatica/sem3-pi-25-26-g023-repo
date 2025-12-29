# Makefile para USAC11
CC = riscv32-linux-gcc
CFLAGS = -Wall -Wextra -g -O0 -static -I./include
LIBRV32 = ${HOME}/bin/bootlin/riscv32-buildroot-linux-gnu/sysroot

SRCS = src/setup_manager.c src/file_parser.c src/main_test.c
OBJS = $(SRCS:.c=.o)
TARGET = usac11_test.elf

# Funções assembly do Sprint 2 (assumindo que estão numa pasta acima)
ASM_DIR = ../usac01-09/assembly
ASM_OBJS = $(ASM_DIR)/encrypt.o $(ASM_DIR)/decrypt.o \
           $(ASM_DIR)/extract.o $(ASM_DIR)/format_cmd.o \
           $(ASM_DIR)/buffer.o $(ASM_DIR)/sort.o

all: $(TARGET)

$(TARGET): $(OBJS) $(ASM_OBJS)
	$(CC) $(CFLAGS) -o $@ $^

%.o: %.c
	$(CC) $(CFLAGS) -c $< -o $@

run: $(TARGET)
	@echo "Criando estrutura de dados..."
	@mkdir -p data
	@cp ../usac11_data/*.txt data/ 2>/dev/null || true
	@echo "Executando teste..."
	qemu-riscv32 -L $(LIBRV32) ./$(TARGET)

clean:
	rm -f $(OBJS) $(TARGET) system_state.txt

test: run

.PHONY: all run clean test