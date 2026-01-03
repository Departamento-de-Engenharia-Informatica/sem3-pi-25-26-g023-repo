# ui/Makefile
CC = gcc
CFLAGS = -Wall -Wextra -std=c99 -I../board

TARGET = ui_test

all: $(TARGET)

$(TARGET): ui.c ui_main.c ../board/board.c
	$(CC) $(CFLAGS) -o $(TARGET) ui.c ui_main.c ../board/board.c

run: $(TARGET)
	./$(TARGET)

clean:
	rm -f $(TARGET)