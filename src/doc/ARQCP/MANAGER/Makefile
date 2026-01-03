# Makefile para Manager
CC = riscv32-linux-gcc
CFLAGS = -Wall -Wextra -std=c99 -g
AS = riscv32-unknown-elf-as

# Arquivos
SRC = manager.c manager_integration.c ../common/utils.c
ASM_SRC = manager_assembly.s ../assembly/usac03_extract_data.s
HEADERS = manager.h ../common/data_structures.h

# Output
TARGET = station_manager
OBJ = $(SRC:.c=.o) $(ASM_SRC:.s=.o)

all: $(TARGET)

$(TARGET): $(OBJ)
	$(CC) $(CFLAGS) -o $@ $^ -lm

%.o: %.c $(HEADERS)
	$(CC) $(CFLAGS) -c $< -o $@

%.o: %.s
	$(AS) $< -o $@

run: $(TARGET)
	qemu-riscv32 -L ${HOME}/bin/bootlin/riscv32-buildroot-linux-gnu/sysroot ./$(TARGET)

clean:
	rm -f $(OBJ) $(TARGET) *.log

test: $(TARGET)
	./$(TARGET) --test