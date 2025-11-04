# Define the RISC-V cross-compiler
CC = riscv64-unknown-elf-gcc

# Compilation flags (O2 optimization, debug -g)
# rv64gc = 64-bit, General purpose, Compacted instructions
# lp64d = 64-bit Long/Pointer, Double-precision float
CFLAGS = -g -O2 -march=rv64gc -mabi=lp64d

# Name of the final executable
TARGET = usac01_test

# List of source files
C_SRCS = main.c
ASM_SRCS = usac01_encrypt.s

# Generate object file names (.o) from source files (.c, .s)
OBJS = $(C_SRCS:.c=.o) $(ASM_SRCS:.s=.o)

# Main rule (default): build the target
all: $(TARGET)

# Linking rule: joins the .o files to create the executable
$(TARGET): $(OBJS)
	$(CC) $(CFLAGS) -o $(TARGET) $(OBJS)

# Rule to compile C files -> .o
%.o: %.c
	$(CC) $(CFLAGS) -c $< -o $@

# Rule to compile Assembly files (.s) -> .o
%.o: %.s
	$(CC) $(CFLAGS) -c $< -o $@

# "clean" rule: deletes generated files
clean:
	rm -f $(TARGET) $(OBJS)

# "run" rule: compiles and executes with QEMU
run: all
	@echo "--- Running $(TARGET) with qemu-riscv64 ---"
	qemu-riscv64 ./$(TARGET)