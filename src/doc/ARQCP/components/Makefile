# Makefile geral
.PHONY: all board ui clean

all: board ui

board:
	@echo "🔨 Compilando Board..."
	$(MAKE) -C board

ui:
	@echo "🔨 Compilando UI..."
	$(MAKE) -C ui

test: ui
	@echo "🧪 Executando testes..."
	cd ui && ./ui_test

clean:
	@echo "🧹 Limpando..."
	$(MAKE) -C board clean
	$(MAKE) -C ui clean
	rm -f *~

help:
	@echo "Comandos disponíveis:"
	@echo "  make all    - Compila tudo"
	@echo "  make board  - Compila apenas board"
	@echo "  make ui     - Compila apenas ui"
	@echo "  make test   - Executa testes"
	@echo "  make clean  - Limpa arquivos"