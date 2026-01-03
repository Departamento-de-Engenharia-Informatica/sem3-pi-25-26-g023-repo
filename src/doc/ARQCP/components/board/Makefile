# board/Makefile
CC = gcc
CFLAGS = -Wall -Wextra -std=c99

TARGET = board_test

all: $(TARGET)

$(TARGET): board.c
	$(CC) $(CFLAGS) -o $(TARGET) board.c

test: $(TARGET)
	./$(TARGET)

clean:
	rm -f $(TARGET)