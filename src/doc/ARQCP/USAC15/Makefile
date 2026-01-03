# USAC15 - Simple Makefile
CC = gcc
CFLAGS = -Wall -Wextra -std=c99

all: usac15

usac15: usac15_main.c usac15_board_display.c
	$(CC) $(CFLAGS) -o usac15 usac15_main.c usac15_board_display.c

run: usac15
	./usac15

clean:
	rm -f usac15

test: usac15
	./usac15