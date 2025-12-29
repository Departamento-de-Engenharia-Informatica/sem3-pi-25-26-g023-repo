#ifndef DATA_STRUCTURES_H
#define DATA_STRUCTURES_H

#include <time.h>

#define MAX_NAME_LEN 50
#define MAX_USERNAME_LEN 30
#define MAX_PASSWORD_LEN 50
#define MAX_ACTION_LEN 100
#define MAX_TRACKS 99
#define MAX_USERS 20
#define MAX_TRAINS 50
#define MAX_LOGS 1000

typedef enum {
    ROLE_ADMIN,
    ROLE_OPERATOR,
    ROLE_TRAFFIC_MANAGER,
    ROLE_VIEWER
} UserRole;

typedef enum {
    TRACK_FREE = 0,
    TRACK_BUSY = 1,
    TRACK_ASSIGNED = 2,
    TRACK_INOPERATIVE = 3
} TrackState;

typedef struct {
    int id;
    TrackState state;
    int trainId;  // -1 se livre
} Track;

typedef struct {
    char name[MAX_NAME_LEN];
    char username[MAX_USERNAME_LEN];
    char password[MAX_PASSWORD_LEN];  // Encriptada com Caesar
    int cipherKey;
    UserRole role;
} User;

typedef struct {
    int id;
    char username[MAX_USERNAME_LEN];
    char action[MAX_ACTION_LEN];
    time_t timestamp;
} LogEntry;

typedef struct {
    int* buffer;
    int length;
    int window;
    int nelem;
    int head;
    int tail;
} SensorBuffer;

typedef struct {
    int stationId;
    char stationName[MAX_NAME_LEN];
    int maxTracks;
    int maxUsers;

    // Configuração sensores
    int tempBufferSize;
    int tempWindowSize;
    int humidBufferSize;
    int humidWindowSize;

    // Configuração log
    int maxLogEntries;
    char logFile[MAX_NAME_LEN];

    // Segurança
    int maxLoginAttempts;
    int sessionTimeout;
} StationConfig;

#endif