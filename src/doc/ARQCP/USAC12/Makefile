# USAC12 - Simple Makefile
CC = gcc
CFLAGS = -Wall -Wextra -std=c99

all: usac12

usac12: usac12_main.c usac12_log_creator.c usac12_utils.c
	$(CC) $(CFLAGS) -o usac12 usac12_main.c usac12_log_creator.c usac12_utils.c

run: usac12
	./usac12

clean:
	rm -f usac12 *.txt backup_*.txt

test: usac12
	./usac12

# Cria alguns logs de teste
create-test-logs: usac12
	./usac12 <<< "1"
	./usac12 <<< "2"
	./usac12 <<< "5"
	echo "Test logs created"