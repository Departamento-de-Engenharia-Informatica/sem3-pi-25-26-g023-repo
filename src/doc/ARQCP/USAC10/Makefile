# ============================================
# MAKEFILE PARA USAC10
# ============================================

CC = riscv32-linux-gcc
CFLAGS = -Wall -Wextra -std=c11 -g -O0
LDFLAGS = -lm
LIBRV32 = ${HOME}/bin/bootlin/riscv32-buildroot-linux-gnu/sysroot

# Targets principais
TARGETS = usac10_main usac10_test

# Diretórios
COMMON_DIR = ../common
ASM_DIR = ../assembly

# Arquivos fonte
MAIN_SRC = usac10_main.c $(COMMON_DIR)/utils.c
TEST_SRC = usac10_test.c

# Arquivos assembly
ASM_SOURCES = $(ASM_DIR)/usac03_extract_data.s \
              $(ASM_DIR)/usac04_format_command.s \
              $(ASM_DIR)/usac09_median.s

# Arquivos objeto
MAIN_OBJ = $(MAIN_SRC:.c=.o) $(ASM_SOURCES:.s=.o)
TEST_OBJ = $(TEST_SRC:.c=.o)

# Cabeçalhos
HEADERS = hardware/sensors/config.h \
          hardware/lightsigns/config.h \
          $(COMMON_DIR)/colors.h \
          $(COMMON_DIR)/utils.h \
          $(COMMON_DIR)/data_structures.h

.PHONY: all clean run test upload-sensors upload-lightsigns help

all: $(TARGETS)

# Compilar programa principal
usac10_main: $(MAIN_OBJ)
	$(CC) $(CFLAGS) -o $@ $^ $(LDFLAGS)

# Compilar testes
usac10_test: $(TEST_OBJ)
	$(CC) $(CFLAGS) -o $@ $^ $(LDFLAGS)

# Regras para arquivos .c
%.o: %.c $(HEADERS)
	$(CC) $(CFLAGS) -c $< -o $@

# Regras para arquivos .s
%.o: %.s
	$(CC) $(CFLAGS) -c $< -o $@

# Executar no QEMU
run: usac10_main
	@echo "Executando USAC10 no QEMU..."
	qemu-riscv32 -L $(LIBRV32) ./usac10_main

# Executar testes
test: usac10_test
	@echo "Executando testes USAC10..."
	qemu-riscv32 -L $(LIBRV32) ./usac10_test

# Upload para Arduino (requer arduino-cli instalado)
upload-sensors:
	@if command -v arduino-cli > /dev/null 2>&1; then \
		echo "Compilando sensors.ino..."; \
		arduino-cli compile --fqbn arduino:avr:uno hardware/sensors/; \
		echo "Uploading para Arduino..."; \
		arduino-cli upload -p /dev/ttyACM0 --fqbn arduino:avr:uno hardware/sensors/; \
	else \
		echo "Erro: arduino-cli não encontrado!"; \
		echo "Instale com: curl -fsSL https://raw.githubusercontent.com/arduino/arduino-cli/master/install.sh | sh"; \
	fi

upload-lightsigns:
	@if command -v arduino-cli > /dev/null 2>&1; then \
		echo "Compilando lightsigns.ino..."; \
		arduino-cli compile --fqbn arduino:avr:uno hardware/lightsigns/; \
		echo "Uploading para Arduino..."; \
		arduino-cli upload -p /dev/ttyACM1 --fqbn arduino:avr:uno hardware/lightsigns/; \
	else \
		echo "Erro: arduino-cli não encontrado!"; \
		echo "Instale com: curl -fsSL https://raw.githubusercontent.com/arduino/arduino-cli/master/install.sh | sh"; \
	fi

upload-all: upload-sensors upload-lightsigns

# Limpar arquivos compilados
clean:
	rm -f *.o usac10_main usac10_test
	rm -f $(COMMON_DIR)/*.o
	rm -f $(ASM_DIR)/*.o

