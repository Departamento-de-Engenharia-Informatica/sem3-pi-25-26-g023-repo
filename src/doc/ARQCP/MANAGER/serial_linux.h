#ifndef SERIAL_LINUX_H
#define SERIAL_LINUX_H

int serial_open(const char* port_name);
void serial_close(int fd);
int serial_send(int fd, const char* cmd);
int serial_transaction(int fd, const char* cmd, char* response_buffer, int buffer_size);

#endif