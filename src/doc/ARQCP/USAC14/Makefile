# USAC14 - Simple Makefile
CC = gcc
CFLAGS = -Wall -Wextra -std=c99

all: usac14

usac14: usac14_main.c usac14_lightsigns.c
	$(CC) $(CFLAGS) -o usac14 usac14_main.c usac14_lightsigns.c

run: usac14
	./usac14

clean:
	rm -f usac14

test: usac14
	./usac14