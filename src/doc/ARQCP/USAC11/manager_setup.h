#ifndef MANAGER_SETUP_H
#define MANAGER_SETUP_H

#include <stddef.h>

typedef enum {
    TRACK_FREE = 0,
    TRACK_ASSIGNED = 1,
    TRACK_BUSY = 2,
    TRACK_NONOP = 3
} TrackState;

typedef struct {
    char name[64];
    char username[32];
    char password[64];
    int  key;           /* chave do Caesar Cipher */
} User;

typedef struct {
    int buffer_length;
    int mm_window;
} SensorCfg;

typedef struct {
    SensorCfg temp;
    SensorCfg hum;
} SensorsConfig;

typedef struct {
    int id;         /* 1..99 */
    TrackState state;
    int train_id;  /* -1 se não tiver train */
} Track;

typedef struct {
    int id;
} Train;

typedef struct {
    int  id;
    char username[32];
    char action[128];
    char timestamp[32];
} LogRecord;

typedef struct {
    User  *users;
    int    num_users;

    Track *tracks;
    int    num_tracks;

    Train *trains;
    int    num_trains;

    LogRecord *logs;
    int        num_logs;
    int        logs_capacity;
    int        next_log_id;

    SensorsConfig sensors;
} ManagerData;

/* assembly do Sprint 2 */
int encrypt_data(char *in, int key, char *out);

/* USAC11: lê o ficheiro e faz o setup inicial */
int manager_initial_setup(const char *filename, ManagerData *data);

/* libertar memória dinâmica das estruturas */
void manager_free(ManagerData *data);

#endif
