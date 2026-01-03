# Makefile Final - ARQCP Sprint 3
CC = riscv32-linux-gcc
CFLAGS = -Wall -Wextra -std=c99 -I. -Iui -Iboard -Imanager -Icommon -Iassembly
LDFLAGS = -lm

# Diretórios
UI_DIR = ui
BOARD_DIR = board
MANAGER_DIR = manager
COMMON_DIR = common
ASM_DIR = assembly

# Arquivos fonte
SRC = station_main.c \
      $(UI_DIR)/ui.c \
      $(BOARD_DIR)/board.c \
      $(MANAGER_DIR)/manager.c \
      $(MANAGER_DIR)/manager_integration.c \
      $(COMMON_DIR)/utils.c

# Assembly (Sprint 2 + Manager)
ASM_SRC = $(ASM_DIR)/usac01_encrypt_data.s \
          $(ASM_DIR)/usac02_decrypt_data.s \
          $(ASM_DIR)/usac03_extract_data.s \
          $(ASM_DIR)/usac04_format_command.s \
          $(ASM_DIR)/usac05_enqueue_value.s \
          $(ASM_DIR)/usac06_dequeue_value.s \
          $(ASM_DIR)/usac09_median.s \
          $(MANAGER_DIR)/manager_assembly.s

# Output
TARGET = stationMngt
OBJ = $(SRC:.c=.o) $(ASM_SRC:.s=.o)

.PHONY: all clean run test

all: $(TARGET)

$(TARGET): $(OBJ)
	$(CC) $(CFLAGS) -o $@ $^ $(LDFLAGS)

%.o: %.c
	$(CC) $(CFLAGS) -c $< -o $@

%.o: %.s
	riscv32-unknown-elf-as $< -o $@

run: $(TARGET)
	@echo "🚀 Executando no QEMU RISC-V..."
	qemu-riscv32 -L ${HOME}/bin/bootlin/riscv32-buildroot-linux-gnu/sysroot ./$(TARGET)

clean:
	rm -f $(OBJ) $(TARGET)
	rm -f *.log *.txt

test: $(TARGET)
	@echo "🧪 Executando testes..."
	./$(TARGET) --test

help:
	@echo "Comandos disponíveis:"
	@echo "  make all    - Compila sistema completo"
	@echo "  make run    - Executa no QEMU RISC-V"
	@echo "  make test   - Executa testes"
	@echo "  make clean  - Limpa tudo"