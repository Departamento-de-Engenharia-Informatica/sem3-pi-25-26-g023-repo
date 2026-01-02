# ============================================
# MAKEFILE PARA USAC11
# ============================================

CC = riscv32-linux-gcc
CFLAGS = -Wall -Wextra -std=c11 -g -O0
LDFLAGS = -lm
LIBRV32 = ${HOME}/bin/bootlin/riscv32-buildroot-linux-gnu/sysroot

# Targets principais
TARGETS = usac11_main usac11_test

# Diretórios
COMMON_DIR = ../common
ASM_DIR = ../assembly
CONFIG_DIR = config_files

# Arquivos fonte
MAIN_SRC = usac11_main.c \
           config_loader.c \
           station_manager.c \
           sensor_handler.c \
           lightsigns_controller.c \
           board_display.c \
           ui_interface.c \
           $(COMMON_DIR)/utils.c \
           $(COMMON_DIR)/logging.c

TEST_SRC = usac11_test.c

# Arquivos assembly
ASM_SOURCES = $(ASM_DIR)/usac01_encrypt_data.s \
              $(ASM_DIR)/usac02_decrypt_data.s \
              $(ASM_DIR)/usac03_extract_data.s \
              $(ASM_DIR)/usac04_format_command.s \
              $(ASM_DIR)/usac05_enqueue_value.s \
              $(ASM_DIR)/usac06_dequeue_value.s \
              $(ASM_DIR)/usac07_move_n_to_array.s \
              $(ASM_DIR)/usac08_sort_array.s \
              $(ASM_DIR)/usac09_median.s

# Arquivos objeto
MAIN_OBJ = $(MAIN_SRC:.c=.o) $(ASM_SOURCES:.s=.o)
TEST_OBJ = $(TEST_SRC:.c=.o)

# Cabeçalhos
HEADERS = config_loader.h \
          station_manager.h \
          sensor_handler.h \
          lightsigns_controller.h \
          board_display.h \
          ui_interface.h \
          $(COMMON_DIR)/colors.h \
          $(COMMON_DIR)/utils.h \
          $(COMMON_DIR)/logging.h \
          $(COMMON_DIR)/data_structures.h \
          $(ASM_DIR)/assembly_utils.h

# Arquivos de configuração
CONFIG_FILES = $(CONFIG_DIR)/station_config.txt \
               $(CONFIG_DIR)/sample_config.txt \
               $(CONFIG_DIR)/users_config.txt \
               $(CONFIG_DIR)/tracks_config.txt \
               $(CONFIG_DIR)/sensors_config.txt

.PHONY: all clean run test configs setup help

all: $(TARGETS)

# Compilar programa principal
usac11_main: $(MAIN_OBJ)
	$(CC) $(CFLAGS) -o $@ $^ $(LDFLAGS)

# Compilar testes
usac11_test: $(TEST_OBJ)
	$(CC) $(CFLAGS) -o $@ $^ $(LDFLAGS)

# Regras para arquivos .c
%.o: %.c $(HEADERS)
	$(CC) $(CFLAGS) -c $< -o $@

# Regras para arquivos .s
%.o: %.s
	$(CC) $(CFLAGS) -c $< -o $@

# Executar no QEMU
run: usac11_main configs
	@echo "Executando USAC11 no QEMU..."
	qemu-riscv32 -L $(LIBRV32) ./usac11_main

# Executar testes
test: usac11_test
	@echo "Executando testes USAC11..."
	qemu-riscv32 -L $(LIBRV32) ./usac11_test

# Criar arquivos de configuração padrão
configs:
	@echo "Criando arquivos de configuração..."
	@mkdir -p $(CONFIG_DIR)

	@if [ ! -f "$(CONFIG_DIR)/station_config.txt" ]; then \
		echo "# ============================================" > $(CONFIG_DIR)/station_config.txt; \
		echo "# CONFIGURAÇÃO DA ESTAÇÃO FERROVIÁRIA" >> $(CONFIG_DIR)/station_config.txt; \
		echo "# Sprint 3 ARQCP - USAC11" >> $(CONFIG_DIR)/station_config.txt; \
		echo "# ============================================" >> $(CONFIG_DIR)/station_config.txt; \
		echo "" >> $(CONFIG_DIR)/station_config.txt; \
		echo "# USUÁRIOS DO SISTEMA" >> $(CONFIG_DIR)/station_config.txt; \
		echo "USER=Administrador Sistema:admin:ADMIN:3:ADMIN" >> $(CONFIG_DIR)/station_config.txt; \
		echo "USER=Operador Estação:operator:OPERATOR:5:OPERATOR" >> $(CONFIG_DIR)/station_config.txt; \
		echo "" >> $(CONFIG_DIR)/station_config.txt; \
		echo "# TRILHOS DA ESTAÇÃO" >> $(CONFIG_DIR)/station_config.txt; \
		echo "TRACK=1:Trilho Principal 1:FREE" >> $(CONFIG_DIR)/station_config.txt; \
		echo "TRACK=2:Trilho Principal 2:FREE" >> $(CONFIG_DIR)/station_config.txt; \
		echo "" >> $(CONFIG_DIR)/station_config.txt; \
		echo "# CONFIGURAÇÃO DOS SENSORES" >> $(CONFIG_DIR)/station_config.txt; \
		echo "SENSOR=TEMPERATURE:10:5:-20.0:60.0" >> $(CONFIG_DIR)/station_config.txt; \
		echo "SENSOR=HUMIDITY:10:5:0.0:100.0" >> $(CONFIG_DIR)/station_config.txt; \
		echo "Arquivo criado: $(CONFIG_DIR)/station_config.txt"; \
	fi

	@if [ ! -f "$(CONFIG_DIR)/sample_config.txt" ]; then \
		echo "# ============================================" > $(CONFIG_DIR)/sample_config.txt; \
		echo "# CONFIGURAÇÃO DE EXEMPLO - TESTES" >> $(CONFIG_DIR)/sample_config.txt; \
		echo "# ============================================" >> $(CONFIG_DIR)/sample_config.txt; \
		echo "" >> $(CONFIG_DIR)/sample_config.txt; \
		echo "# Configuração mínima para testes" >> $(CONFIG_DIR)/sample_config.txt; \
		echo "" >> $(CONFIG_DIR)/sample_config.txt; \
		echo "# Usuários de teste" >> $(CONFIG_DIR)/sample_config.txt; \
		echo "USER=Test Admin:testadmin:TEST:1:ADMIN" >> $(CONFIG_DIR)/sample_config.txt; \
		echo "USER=Test Operator:testop:TEST:3:OPERATOR" >> $(CONFIG_DIR)/sample_config.txt; \
		echo "" >> $(CONFIG_DIR)/sample_config.txt; \
		echo "# Trilhos de teste" >> $(CONFIG_DIR)/sample_config.txt; \
		echo "TRACK=1:Trilho Teste 1:FREE" >> $(CONFIG_DIR)/sample_config.txt; \
		echo "TRACK=2:Trilho Teste 2:FREE" >> $(CONFIG_DIR)/sample_config.txt; \
		echo "" >> $(CONFIG_DIR)/sample_config.txt; \
		echo "# Sensores de teste" >> $(CONFIG_DIR)/sample_config.txt; \
		echo "SENSOR=TEMPERATURE:5:3:0.0:40.0" >> $(CONFIG_DIR)/sample_config.txt; \
		echo "SENSOR=HUMIDITY:5:3:0.0:100.0" >> $(CONFIG_DIR)/sample_config.txt; \
		echo "Arquivo criado: $(CONFIG_DIR)/sample_config.txt"; \
	fi

	@if [ ! -f "$(CONFIG_DIR)/users_config.txt" ]; then \
		echo "# ============================================" > $(CONFIG_DIR)/users_config.txt; \
		echo "# CONFIGURAÇÃO DE USUÁRIOS" >> $(CONFIG_DIR)/users_config.txt; \
		echo "# ============================================" >> $(CONFIG_DIR)/users_config.txt; \
		echo "" >> $(CONFIG_DIR)/users_config.txt; \
		echo "# Administradores" >> $(CONFIG_DIR)/users_config.txt; \
		echo "USER=João Silva:jsilva:PASSWORD:3:ADMIN" >> $(CONFIG_DIR)/users_config.txt; \
		echo "USER=Maria Santos:msantos:SECRET:5:ADMIN" >> $(CONFIG_DIR)/users_config.txt; \
		echo "" >> $(CONFIG_DIR)/users_config.txt; \
		echo "# Operadores" >> $(CONFIG_DIR)/users_config.txt; \
		echo "USER=Carlos Oliveira:coliveira:OPERATOR:7:OPERATOR" >> $(CONFIG_DIR)/users_config.txt; \
		echo "USER=Ana Pereira:apereira:STATION:9:OPERATOR" >> $(CONFIG_DIR)/users_config.txt; \
		echo "Arquivo criado: $(CONFIG_DIR)/users_config.txt"; \
	fi

	@echo "Configuração concluída."

# Setup inicial
setup: configs
	@echo "Setup inicial USAC11 concluído."
	@echo "Use 'make all' para compilar."
	@echo "Use 'make run' para executar."

# Upload para Arduino (se aplicável)
upload-sensors:
	@echo "USAC11 não requer upload para Arduino"
	@echo "O código assembly é executado no RISC-V"

upload-lightsigns:
	@echo "USAC11 não requer upload para Arduino"
	@echo "O código assembly é executado no RISC-V"

upload-all: upload-sensors upload-lightsigns

# Limpar arquivos compilados
clean:
	@echo "Limpando arquivos..."
	rm -f *.o usac11_main usac11_test
	rm -f $(COMMON_DIR)/*.o
	rm -f $(ASM_DIR)/*.o
	@echo "Limpeza concluída."

# Limpar completamente (inclui configurações)
clean-all: clean
	@echo "Limpando configurações..."
	rm -rf $(CONFIG_DIR)
	@echo "Limpeza completa."

# Ajuda
help:
	@echo "==========================================="
	@echo "MAKEFILE - USAC11 (Configuração Inicial)"
	@echo "==========================================="
	@echo ""
	@echo "Comandos disponíveis:"
	@echo "  make all       - Compilar tudo"
	@echo "  make run       - Compilar e executar no QEMU"
	@echo "  make test      - Executar testes"
	@echo "  make configs   - Criar arquivos de configuração"
	@echo "  make setup     - Setup inicial (configs + info)"
	@echo "  make clean     - Limpar arquivos compilados"
	@echo "  make clean-all - Limpar tudo (inclui configs)"
	@echo "  make help      - Mostrar esta ajuda"
	@echo ""
	@echo "Requisitos:"
	@echo "  • riscv32-linux-gcc"
	@echo "  • QEMU RISC-V"
	@echo "  • Bootlin sysroot: $(LIBRV32)"
	@echo ""

# Verificar dependências
check-deps:
	@echo "Verificando dependências..."
	@which $(CC) > /dev/null && echo "✓ $(CC) encontrado" || echo "✗ $(CC) não encontrado"
	@which qemu-riscv32 > /dev/null && echo "✓ qemu-riscv32 encontrado" || echo "✗ qemu-riscv32 não encontrado"
	@if [ -d "$(LIBRV32)" ]; then echo "✓ Sysroot encontrado: $(LIBRV32)"; else echo "✗ Sysroot não encontrado: $(LIBRV32)"; fi
	@echo "Dependências verificadas."

# Criar backup
backup:
	@echo "Criando backup do projeto USAC11..."
	@tar -czf usac11_backup_$(shell date +%Y%m%d_%H%M%S).tar.gz \
		*.c *.h \
		$(CONFIG_DIR)/*.txt \
		Makefile
	@echo "Backup criado."

# Listar arquivos
list:
	@echo "Arquivos do projeto USAC11:"
	@echo ""
	@echo "Código C principal:"
	@echo "  • usac11_main.c"
	@echo "  • config_loader.c"
	@echo "  • station_manager.c"
	@echo "  • sensor_handler.c"
	@echo "  • lightsigns_controller.c"
	@echo "  • board_display.c"
	@echo "  • ui_interface.c"
	@echo ""
	@echo "Assembly:"
	@for file in $(ASM_SOURCES); do echo "  • $$file"; done
	@echo ""
	@echo "Headers:"
	@for file in $(HEADERS); do echo "  • $$file"; done
	@echo ""
	@echo "Configurações:"
	@for file in $(CONFIG_FILES); do echo "  • $$file"; done