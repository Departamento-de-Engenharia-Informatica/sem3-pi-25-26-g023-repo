# Define the RISC-V cross-compiler
CC = riscv64-unknown-elf-gcc

# Compilation flags
# -g: Add debug info
# -O2: Optimization level 2
# -march=rv64gc: Target 64-bit RISC-V with General, Atomic, and Compressed instructions
# -mabi=lp64d: Use 64-bit Long/Pointer and 64-bit Double-precision floats
CFLAGS = -g -O2 -march=rv64gc -mabi=lp64d

# Name of the final executable
TARGET = usac02_test

# List of source files
C_SRCS = main.c
ASM_SRCS = usac02_decrypt.s

# Generate object file names from source file names
OBJS = $(C_SRCS:.c=.o) $(ASM_SRCS:.s=.o)

# Default rule: build the target executable
all: $(TARGET)

# Linking rule: combines all object files into the final executable
$(TARGET): $(OBJS)
	$(CC) $(CFLAGS) -o $(TARGET) $(OBJS)

# Compilation rule for C files (.c -> .o)
%.o: %.c
	$(CC) $(CFLAGS) -c $< -o $@

# Compilation rule for Assembly files (.s -> .o)
%.o: %.s
	$(CC) $(CFLAGS) -c $< -o $@

# Clean rule: removes generated files
clean:
	rm -f $(TARGET) $(OBJS)

# Run rule: builds the target and executes it with qemu
run: all
	@echo "--- Running $(TARGET) with qemu-riscv64 ---"
	qemu-riscv64 ./$(TARGET)