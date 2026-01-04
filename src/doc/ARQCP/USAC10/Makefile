# MAKEFILE MINIMALISTA PARA USAC10
CC = riscv32-linux-gcc
CFLAGS = -Wall -Wextra -std=c11 -g -O0

all: usac10_main usac10_test

usac10_main: usac10_main.c
	$(CC) $(CFLAGS) -o $@ $<

usac10_test: usac10_test.c
	$(CC) $(CFLAGS) -o $@ $<

run: usac10_main
	./usac10_main

test: usac10_test
	./usac10_test

clean:
	rm -f usac10_main usac10_test *.o

.PHONY: all run test clean