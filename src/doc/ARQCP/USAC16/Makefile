# USAC16 - Simple Makefile
CC = gcc
CFLAGS = -Wall -Wextra -std=c99

all: usac16

usac16: usac16_main.c usac16_track_manager.c
	$(CC) $(CFLAGS) -o usac16 usac16_main.c usac16_track_manager.c

run: usac16
	./usac16

clean:
	rm -f usac16

test: usac16
	./usac16