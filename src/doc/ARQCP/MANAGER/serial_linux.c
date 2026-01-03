#include "serial_linux.h"
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <fcntl.h>
#include <termios.h>
#include <time.h>

int serial_open(const char* port_name) {
    int fd = open(port_name, O_RDWR | O_NOCTTY | O_SYNC);
    if (fd < 0) {
        printf("ERRO SERIAL: Nao foi possivel abrir %s\n", port_name);
        return -1;
    }

    struct termios tty;
    if (tcgetattr(fd, &tty) != 0) return -1;

    cfsetospeed(&tty, B9600);
    cfsetispeed(&tty, B9600);

    tty.c_cflag = (tty.c_cflag & ~CSIZE) | CS8;
    tty.c_cflag |= (CLOCAL | CREAD);
    tty.c_cflag &= ~(PARENB | PARODD);
    tty.c_cflag &= ~CSTOPB;
    tty.c_cflag &= ~CRTSCTS;

    // Modo Canónico (lê linha a linha, espera pelo \n)
    tty.c_lflag |= ICANON | ISIG;
    tty.c_iflag &= ~(IXON | IXOFF | IXANY);

    if (tcsetattr(fd, TCSANOW, &tty) != 0) return -1;

    sleep(2); // Esperar que o Arduino reinicie
    return fd;
}

void serial_close(int fd) {
    if(fd > 0) close(fd);
}

int serial_send(int fd, const char* cmd) {
    if (fd < 0) return -1;
    int len = strlen(cmd);
    int n = write(fd, cmd, len);
    write(fd, "\n", 1); // Garante o enter
    return n;
}

int serial_transaction(int fd, const char* cmd, char* response_buffer, int buffer_size) {
    if (fd < 0) return -1;

    tcflush(fd, TCIFLUSH); // Limpar lixo anterior
    serial_send(fd, cmd);

    // Ler resposta
    int n = read(fd, response_buffer, buffer_size - 1);
    if (n > 0) {
        response_buffer[n] = '\0';
        response_buffer[strcspn(response_buffer, "\r\n")] = 0; // Remove quebras de linha
    }
    return n;
}