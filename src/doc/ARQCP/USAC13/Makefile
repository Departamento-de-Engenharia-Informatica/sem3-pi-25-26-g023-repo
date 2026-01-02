# USAC13 - Simple Makefile
CC = gcc
CFLAGS = -Wall -Wextra -std=c99

all: usac13

usac13: usac13_main.c usac13_sensor_handler.c
	$(CC) $(CFLAGS) -o usac13 usac13_main.c usac13_sensor_handler.c

run: usac13
	./usac13

clean:
	rm -f usac13

test: usac13
	./usac13